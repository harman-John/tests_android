/*
 * LightX.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 6/27/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * The LightX class implements Avnera's LightX Protocol and is used to facilitate communication
 * between an Android device and hardware utilizing Avnera's LightX chip.
 *
 * @author Brian Doyle on 6/27/15. Darren Lu update on 2/3/1017
 */
public final class LightX {
    private static final String TAG = LightX.class.getSimpleName();
    // Keep this up to date:
    public static final String kLightXLibraryVersion = "1.9.12";

    private final Delegate mDelegate;
    private int mFirmwareBlockCurrent;
    private int mFirmwareBlockLast;
    private byte[] mFirmwareData;
    private Command mFirmwareEraseCommand;
    private static boolean mFirmwareIsUpdating;
    private Command mFirmwareReadCommand;
    private int mFirmwareReadFrom;
    private int mFirmwareReadTo;
    private FirmwareRegion mFirmwareRegion;
    private Command mFirmwareWriteCommand;
    private boolean mIsAuthenticated;
    private boolean mIsInBootloader;
    private final ReadThread mReadThread;
    private Packet mRequest;
    private final Socket mSocket;
    private final Object mSocketLock;
    private final ModuleId mSourceModuleId;
    private final WriteThread mWriteThread;

    /**
     * kMaximumRegionRequestSize is the maximum number of bytes that may be sent to the device
     * during any flash region read or write call.  The SDHM host interface spec v1.26 says
     * the device supports a 256 byte read/write buffer, but the D0 hardware I have seems to have
     * problems if I send a packet larger than 256 bytes.  As such I'm backing the buffer size off
     * such that when combined with the packet headers the entire size does not exceed 256 bytes.
     */
    private static final int kMaximumRegionRequestSize = Packet.kMaximumBufferSize;

    private static final int kResource1Size = 256 * 1024;
    private static final int kResource2Size = 1024 * 1024;

    private static final int kSectorSize = 4096;
    private static final int kReadBlockSize = kMaximumRegionRequestSize;
    private static final int kWriteBlockSize = kMaximumRegionRequestSize;
    private static final int kTimeoutMsDefault = 2000;

    // these values are returned by AppGraphicEQLimits
    private static final int kGraphicEQNumBands = 10;        // GEQ_NUM_BANDS from app_common/geq/geq.h
    //	private static final int				kGraphicEQNumSettings = 21;		// GEQ_NUM_SETTINGS from app_common/geq/geq.h
    private static final int kGraphicEQSettingMax = 10;        // GEQ_SETTING_MAX from app_common/geq/geq.h
    private static final int kGraphicEQSettingMin = -10;        // GEQ_SETTING_MIN from app_common/geq/geq.h

    private static final int kSizeofUInt32 = 4;

    /**
     * When true, sEnablePacketDumps will cause packet data contents to be written to the debug
     * Logger in hexadecimal format.  This is useful for debugging but should be disabled for
     * production builds.
     * <p>
     * The default is disabled (false).
     */
    public static boolean sEnablePacketDumps;

    /**
     * kApplicationSize is the maximum number of bytes that may be written to the
     * application flash region.  Attempting to write more than this size will cause
     * an exception.
     * <p>
     * Currently the application region supports a maximum data size of 512KB
     */
    public static final int kApplicationSize = 512 * 1024;
    public static final int kApplicationSize750 = 536 * 1024;

    // I'm combining resource 1 and 2 for android clients (it's what SDHDiag seems to do)

    /**
     * kResourceSize is the maximum number of bytes that may be written to the
     * resource flash region.  Attempting to write more than this size will cause
     * an exception.
     * <p>
     * Currently the resource region supports a maximum data size of 1280KB
     */
    public static final int kResourceSize = kResource1Size + kResource2Size;
    public static final int kResourceSize750 = 1000 * 1024;

    public static final int kVersionFileAppAddress = 0x040000;
    public static final int kVersionFileBootloaderAddress = 0x000000;

    public static final int kVersionFileResourceAddress = 0x0c0000;
    public static final int kVersionFileResourceAddress750 = 0x0c6000;

    private int mVersionFileResourceAddress = kVersionFileResourceAddress;
    public static boolean mIs750Device = false;
    private int mBaseAddress = 0;

    /**
     * For firmware read and write calls, a FirmwareRegion defines the region to access.
     */
    public enum FirmwareRegion {
        Application,
        //		Resource1,
//		Resource2,
        Resource
    }

    /**
     * During the firmware write operation, the LightX delegate will be called back via the
     * lightXFirmwareUpdateProgress() callback and passed a FirmwareWriteOperation to indicate
     * the current firmware write operation.
     */
    public enum FirmwareWriteOperation {
        /**
         * All firmware writes begin with an erase of the applicable FirmwareRegion.
         */
        Erase,
        /**
         * After Erase is complete, the FirmwareRegion is written.
         */
        Write,
        /**
         * When writing the Resource FirmwareRegion, after Write is complete the data written
         * to flash is read back and verified (the application region uses a checksum verification
         * strategy calculated by the hardware thus Verify is unnecessary).
         */
        Verify,
        /**
         * When writing the Application FirmwareRegion, after Write is complete the
         * LightX hardware calculates a checksum over the region.
         */
        Checksum,
        /**
         * When the firmware write operation has finished, a final Complete message
         * is delivered.  If the write operation failed, the lightXFirmwareUpdateProgress()'s
         * exception parameter will be non-null.
         */
        Complete
    }

    /**
     * Instantiate a LightX object.
     *
     * @param sourceModuleId The source ModuleId (probably one of ModuleId.Bluetooth or ModuleId.USB)
     * @param delegate       The receiver of LightX.Delegate messages.  This parameter cannot be null.
     * @param socket         A socket opened for reading and writing.  Note that this need not be backed by
     *                       an actual socket, merely an object that conforms to the
     *                       com.avnera.smartdigitalheadset.Socket protocol (such as an Android BluetoothSocket
     *                       wrapped in a BluetoothSocketWrapper).  This parameter cannot be null.
     * @throws IOException Thrown when getInputStream() or getOutputStream() fails for socket
     */
    public LightX(ModuleId sourceModuleId, Delegate delegate, Socket socket) throws IOException {

        if (delegate == null) throw new IllegalArgumentException("delegate cannot be null");
        if (socket == null) throw new IllegalArgumentException("socket cannot be null");

        mDelegate = delegate;
        mSocket = socket;
        mSocketLock = new Object();
        mSourceModuleId = sourceModuleId;

        mReadThread = new ReadThread(socket.getInputStream());
        mWriteThread = new WriteThread(socket.getOutputStream());

        mWriteThread.start();
        mReadThread.start();
    }

    /**
     * The LightX.Delegate interface defines methods that will be called from a LightX
     * instance to inform an interested object (typically the entity that creates the
     * LightX class) of changes to the LightX instance's state.
     * <p>
     * Delegate callbacks are made on an arbitrary, non-main thread.
     */
    public interface Delegate {
        /**
         * A LightX instance calls lightXAppReadResult() to return the result of read command
         * initiated by calling readApp*()
         *
         * @param lightX  The LightX instance calling lightXAppReadResult()
         * @param command The command for which data is being returned
         * @param success true if the read request succeeded, false otherwise. When false, buffer is null.
         * @param buffer  The raw buffer returned by the application firmware.  The content of this
         *                buffer depends on the associated app command.
         */
        void lightXAppReadResult(LightX lightX, Command command, boolean success, byte[] buffer);

        /**
         * A LightX instance calls lightXAppReceivedPush() when a "push" packet is received from the
         * application firmware.  A push is any packet with a command value in the range 0xC0 &lt;= value &lt;= 0xFF,
         * and are typically generated asynchronously by sensors (e.g. accelerometer, etc.).
         *
         * @param lightX  The LightX instance calling lightXAppReceivedPush()
         * @param command The push command for which data is being returned
         * @param data    A buffer containing data associated with the push command.  The content
         *                of this buffer depends on the associated push command.
         */
        void lightXAppReceivedPush(LightX lightX, Command command, byte[] data);

        /**
         * A LightX instance calls lightXAppWriteResult() to return the result of a write command initiated
         * by calling one of the writeApp*() methods.
         *
         * @param lightX  The LightX instance calling lightXWriteResult()
         * @param command The command for which data is being returned
         * @param success true if the write request succeeded, false otherwise
         */
        void lightXAppWriteResult(LightX lightX, Command command, boolean success);

        /**
         * lightXAwaitingReply() is an advisory method that indicates the socket-read thread has
         * not received a reply from the LightX hardware since the socket-write thread last sent a packet.
         * We have observed that when multiple Bluetooth profiles are in use simultaneously, long delays
         * (upwards of several seconds) can sometimes elapse between the time the LightX
         * library sends a packet (via SPP) and the hardware actually responds.  SPP is a guaranteed
         * delivery protocol, but we cannot assume the LightX hardware will always be able to respond.
         * <p>
         * Consequently, this library will attempt to send a packet to the LightX hardware up to
         * three times, with an interval between resends of two seconds.  After it has sent a packet
         * three times it will wait, potentially indefinitely, for a response from the hardware.
         * <p>
         * The purpose of the lightXAwaitingReply() method is to notify the application that a delay
         * has been observed and allow the application to take some action if it chooses.  The
         * recommended reaction to this function being called is to passively observe until
         * the totalElapsedMsSinceFirstTransmission parameter indicates a value likely to cause
         * dissatisfaction to an end-user (for example, 30,000, indicating 30 seconds have elapsed
         * since the first time the packet was sent).  At that point, tell the user that there
         * seems to be some trouble communicating with the LightX hardware/headset and ask them
         * if the application should continue to wait for a reply or if it should stop whatever it is
         * trying to do.
         * <p>
         * If you elect to take no action, return false from this function and the LightX instance
         * will continue waiting for a reply from the hardware.  Otherwise, return true and the
         * packet-sending thread will abort with a TimeoutException.  When this occurs,
         * the write-side of the Bluetooth socket will be closed and you will receive a lightXError()
         * message.  In response, you should call close() on the LightX instance and the Bluetooth
         * instance, reconnect to the LightX device, and then restart whatever operation failed as
         * a result of the TimeoutException.
         *
         * @param lightX                               The LightX instance calling lightXAwaitingReply()
         * @param command                              The command we sent that has not yet been ACK'ed.
         * @param totalElapsedMsSinceFirstTransmission The total time in milliseconds that has
         *                                             elapsed since the packet was first sent.
         * @return return true to generate a TimeoutException and close the write side of the
         * Bluetooth connection, false to continue waiting for a reply.
         */
        boolean lightXAwaitingReply(LightX lightX, Command command, int totalElapsedMsSinceFirstTransmission);

        /**
         * A LightX instance calls lightXError() when an error occurs during command
         * processing.  Errors can be generated by a variety of conditions, examples
         * of which include (but are not limited to) socket failure errors, loss of
         * connectivity, retransmit errors, etc.
         * <p>
         * The LightX class is designed to be as resilient as possible to transitory
         * errors.  For this reason when lightXError() is called there is a more than
         * good chance the error is unrecoverable.  Typically the delegate receiving
         * this message will close() the lightX instance and allow it to be deallocated,
         * then create another instance when resources again become available (such
         * as a Bluetooth device coming into range and regaining a connection).
         *
         * @param lightX    The LightX instance calling lightXError()
         * @param exception An exception with associated message that comes closest to the
         *                  problem condition. The exception is meant to be advisory only.
         */
        void lightXError(LightX lightX, Exception exception);

        /**
         * lightXFirmwareReadStatus() delivers data initiated by a call to readFirmware().
         * This callback will be called repeatedly with small chunks of data until the entire
         * size requested readFirmware() is delivered.
         *
         * @param lightX    The LightX instance calling lightXReadRegion()
         * @param region    The firmware region from which the delivered data comes.
         * @param offset    The offset in bytes from the start of the firmware region where buffer starts.
         * @param buffer    The firmware data that starts at region[ offset ].
         * @param exception During normal operation this parameter will be null.  The read operation is
         *                  complete when either the requested number of bytes have been read (offset +
         *                  buffer.length is equal to the number of bytes requested in the call to
         *                  readFirmware()), or when the exception parameter is non-null.  If the
         *                  exception is non-null, the exception indicates the failure reason and the
         *                  firmware read operation is over.
         * @return Return true to abort reading of the data region, false otherwise.
         */
        boolean lightXFirmwareReadStatus(LightX lightX, FirmwareRegion region, int offset, byte[] buffer, Exception exception);

        /**
         * During a firmware write operation, the LightX delegate is called back with
         * progress updates of each operation (see the documentation for LightX.FirmwareWriteOperation).
         *
         * @param lightX    The LightX instance calling lightXFirmwareWriteStatus()
         * @param region    The firmware region being updated (either Application or Resource)
         * @param operation The current firmware write operation (e.g. Erase, Write, etc.)
         * @param progress  A value between 0 and 1.0 indicating the progress of the current operation.
         * @param exception During normal operation this parameter will be null.  When the write completes,
         *                  lightXFirmwareWriteStatus() will be called with an operation of
         *                  FirmwareWriteOperation.Complete.  At that time, if the exception parameter
         *                  is non-null, then the firmware write operation failed, and the exception
         *                  will indicate the failure reason.
         * @return Return true to abort the current firmware write operation.  Note that doing so may leave
         * the firmware region in an incomplete and inconsistent state.  Aborting is probably only
         * useful during debugging and testing as firmware writing over Bluetooth can be a slow operation.
         * In almost all cases you should return false from lightXFirmwareWriteStatus() which
         * allows the firmware write operation to continue.
         */
        boolean lightXFirmwareWriteStatus(LightX lightX, FirmwareRegion region, FirmwareWriteOperation operation, double progress, Exception exception);
/*
        /**
		 * lightXGetAuthenticationPassword() is called to request the password to be used for
		 * the bootloader authentication phase.
		 *
		 * @param lightX The LightX instance calling lightXGetAuthenticationPassword()
		 * @return The authentication password.
		 * /
		String lightXGetAuthenticationPassword( LightX lightX );
*/

        /**
         * A LightX instance calls lightXIsInBootloader() to indicate whether the device is
         * executing bootloader code.  This method will be called whenever the LightX connection
         * state changes to connected and when the state transitions to bootloader or application
         * mode.
         *
         * @param lightX         The LightX instance calling lightXIsInBootloader()
         * @param isInBootloader A boolean value indicating whether the hardware is currently
         *                       executing bootloader code (true) or application code (false).
         */
        void lightXIsInBootloader(LightX lightX, boolean isInBootloader);

        /**
         * A LightX instance calls lightXReadBootResult() to return the result of a read command initiated
         * by calling one of the readBoot* methods
         *
         * @param lightX  The LightX instance calling lightXReadBootResult()
         * @param command The command for which data is being returned
         * @param success true if the write request succeeded, false otherwise. When false, buffer is null.
         * @param address Ancillary command data
         * @param buffer  The value of the requested boot data
         */
        void lightXReadBootResult(LightX lightX, Command command, boolean success, int address, byte[] buffer);

        /**
         * A LightX instance calls lightXReadConfigResult() to return the result of a read command initiated
         * by calling one of the readConfig*.
         *
         * @param lightX  The LightX instance calling lightXReadConfigResult()
         * @param command The command for which data is being returned
         * @param success true if the write request succeeded, false otherwise. When false, buffer is null.
         * @param value   The value of the requested config data
         */
        void lightXReadConfigResult(LightX lightX, Command command, boolean success, String value);
    }

    /**
     * Returns the socket passed to the LightX constructor
     *
     * @return the socket passed to the LightX constructor
     */
    public Socket getSocket() {
        return mSocket;
    }

    public static boolean getFirmwareIsUpdating() {
        return mFirmwareIsUpdating;
    }

    /**
     * Closes the socket passed to the LightX constructor and releases resources.
     * Once close() is called this LightX instance is invalid and cannot be reused.
     */
    public void close() {
        mReadThread.close();
        mWriteThread.close();

        try {
            mSocket.close();
        } catch (IOException e) {
        }
    }

    private int consume(byte[] data) {
        if (mSourceModuleId == ModuleId.Bluetooth) return consumeBluetooth(data);

        int[] consumed = new int[1];
        int dataRemaining, from, to;
        Packet packet;

//		Log.d( "consume received " + data.length + " bytes from socket:\n" + Debug.hexify( data ) );

        from = 0;
        dataRemaining = to = data.length;

        while (dataRemaining >= Packet.kPacketHeaderSize) {
            if (data[from] != (byte) mSourceModuleId.value() || data[from + 1] != (byte) ModuleId.Application.value()) {
                ++from;
                --dataRemaining;
                continue;
            }

            while ((packet = Packet.assemble(data, from, to, consumed)) != null) {
                if (sEnablePacketDumps) {
                    Logger.d(TAG, "read packet " + packet.opcode + " " + packet.command() +
                            " (" + consumed[0] + " bytes):\n" +
                            Debug.hexify(data, 0, from, from + consumed[0])
                    );
                }
                processPacket(packet);
                from += consumed[0];
                dataRemaining -= consumed[0];
            }
        }

        return from;
    }

    /**
     * Processes raw data received from the read socket and converts it to LightX Packets which
     * are then handed off to processPacket()
     *
     * @param data An array of raw data from the read socket.
     * @return An integer indicating the number of bytes from the front of the data parameter that
     * were consumed.  The data buffer should be updated and consumed bytes should not be
     * sent back in for processing.
     */
    private int consumeBluetooth(byte[] data) {
        int consumed, i, length, minimumPacketSize, n, start, state;
        Packet packet;

        minimumPacketSize = Packet.kBluetoothTransportHeaderSize + Packet.kPacketHeaderSize + Packet.kBluetoothChecksumSize;

        if ((n = data.length) < minimumPacketSize) return 0;

        consumed = 0;
        length = 0;
        start = 0;
        state = 0;

        findPackets:

        for (i = 0; i < n; ++i) {
            switch (state) {
                case 0: {            // state 0 means we are searching for the start of a transport header
                    for (; i < n; ++i) {
                        if (data[i] == (byte) 0xff) {
                            start = i;
                            state = 1;
                            break;
                        }
                    }
                }
                break;

                case 1: {            // state 1 means we are searching for the second transport header byte
                    if (data[i] == 0x5a) {
                        state = 2;
                    } else {
                        state = 0;
                        --i;
                    }
                }
                break;

                case 2:
                case 3: {            // states 2, 3 are additional transport header bytes
                    if (data[i] == (byte) 0x00) {
                        ++state;
                    } else {
                        state = 0;
                        --i;
                    }
                }
                break;

                case 4: {            // state 4 is MSB of packet length
                    length = (data[i] & 0xff) << 8;
                    state = 5;
                }
                break;

                case 5: {            // state 5 is LSB of packet length
                    length |= (int) data[i] & 0xff;

                    // we have a valid header, so determine if we have the remainder of
                    // the packet in our buffer.  if so, process and consume it.  if not,
                    // consume everything before the start of the packet and return to
                    // wait for more data.

                    if (start + length <= n) {
                        consumed = start + length;

                        if (Packet.verifyChecksum(data, start, consumed)) {
                            if ((packet = Packet.assemble(data, i + 1, consumed - 1, null)) != null) {
                                if (sEnablePacketDumps) {
                                    int from = i + 1;
                                    int to = consumed - 1;
                                    int checksumSize = packet.checksumSize();
                                    int transportHeaderSize = packet.transportHeaderSize();
                                    int transportSize = transportHeaderSize + checksumSize;

                                    // Logger entire packet content including transport header and checksum
                                    Logger.d(TAG, "received " + packet.opcode + " " + packet.command() + " (" +
                                            (to - from + transportSize) + " bytes from socket):\n" +
                                            Debug.hexify(data, 0, from - transportHeaderSize, to + checksumSize)
                                    );
                                }

                                processPacket(packet);
                            }
                        } else {
                            Logger.w("LightX", "received packet failed checksum verification, ignoring");
                        }

                        i = consumed;
                        state = 0;
                    } else {
                        consumed = start;
                        break findPackets;
                    }
                }
                break;
            }
        }

        return consumed;
    }

    /**
     * When in bootloader mode, issue enterApplication() to jump back to application mode.
     */
    public void enterApplication() {
        writeBootJumpToApplication();
    }

    /**
     * When in application mode, issue enterBootloader() to jump back to bootloader mode.
     */
    public void enterBootloader() {
        // bootloader entry sequence:
        // phase 1: initiate auth challenge
        // phase 2: issue firmware app shutdown
        // phase 3: issue jump to bootloader command

        // begin bootloader entry phase 1:
        readBootAuthChallenge();
    }

    // bootloader mode only commands

//	#mark - read commands -

    private void read(Command command) {
        read(command, kTimeoutMsDefault, 0, null);
    }

    private void read(Command command, int timeoutMs) {
        read(command, timeoutMs, 0, null);
    }

    private void read(Command command, int timeoutMs, int address, byte[] buffer) {
        write(Packet.readRequest(mSourceModuleId, command, timeoutMs, address, buffer));
    }

    private void readRegion(Command command, int offset, int size) {
        int regionSize;

        switch (command) {
            case BootReadImage:
                regionSize = mIs750Device ? kApplicationSize750 : kApplicationSize;
                break;
            case BootReadRsrc1:
                regionSize = mIs750Device ? kResourceSize750 : kResourceSize;
                break;
//			case BootReadRsrc1:		regionSize = kResource1Size;		break;
//			case BootReadRsrc2:		regionSize = kResource2Size;		break;

            default:
                throw new IllegalArgumentException("invalid command");
        }

        if (size < 0) throw new IndexOutOfBoundsException("size cannot be negative");
        if (offset < 0) throw new IndexOutOfBoundsException("offset cannot be negative");
        if (size > kMaximumRegionRequestSize)
            throw new IndexOutOfBoundsException("request size exceeds " + kMaximumRegionRequestSize + " bytes");
        if (offset + size > regionSize)
            throw new IndexOutOfBoundsException("request exceeds data region");

        read(command, kTimeoutMsDefault, offset, new byte[size]);
    }

//	#mark - read app commands -

    /**
     * A convenience method that calls readApp( command, timeoutMs ) with the default timeout of 250ms
     *
     * @param command The application read command to issue.  Must be within the range
     *                Command.AppFirst &lt;= command &lt;= Command.AppLast.  Note that not all commands
     *                may be implemented by the application firmware.
     */
    public void readApp(Command command) {
        readApp(command, kTimeoutMsDefault);
    }

    /**
     * readApp() requests an application parameter from the LightX hardware.  When complete,
     * the value of the read parameter will be returned via the delegate's lightXReadApp() callback.
     *
     * @param command   The application read command to issue.  Must be within the range
     *                  Command.AppFirst &lt;= command &lt;= Command.AppLast.  Note that not all commands
     *                  may be implemented by the application firmware.
     * @param timeoutMs The timeout in milliseconds to wait before retransmitting the write request.
     *                  Generally this value should be &gt;= 250.
     */
    public void readApp(Command command, int timeoutMs) {
        if (command.value() < Command.AppFirst.value() || command.value() > Command.AppLast.value()) {
            throw new IndexOutOfBoundsException("command out of range");
        }

        read(command, timeoutMs);
    }

    public void readApp(Command command, byte[] buffer, int timeoutMs) {
        if (command.value() < Command.AppFirst.value() || command.value() > Command.AppLast.value()) {
            throw new IndexOutOfBoundsException("command out of range");
        }

        read(command, timeoutMs, 0, buffer);
    }

    public void readAppWithUInt32Argument(Command command, long argument, int timeoutMs) {
        byte[] buffer = new byte[kSizeofUInt32];

        Utility.putUnsignedInt(argument, buffer, 0);

        readApp(command, buffer, timeoutMs);
    }

//	#mark - read app convenience methods -

    public void readAppANCAwarenessPreset() {
        readApp(Command.AppANCAwarenessPreset);
    }

    public void readAppANCEnable() {
        readApp(Command.AppANCEnable);
    }

    public void readAppAudioEQPreset() {
        readApp(Command.AppAudioEQPreset);
    }

    public void readAppOnEarDetectionWithAutoOff() {
        readApp(Command.AppOnEarDetectionWithAutoOff);
    }

    public void readAppAwarenessRawLeft() {
        readApp(Command.AppAwarenessRawLeft);
    }

    public void readAppAwarenessRawRight() {
        readApp(Command.AppAwarenessRawRight);
    }

    public void readAppAwarenessRawSteps() {
        readApp(Command.AppAwarenessRawSteps);
    }

    public void readAppBatteryLevel() {
        readApp(Command.AppBatteryLevel);
    }

    public void readAppFirmwareVersion() {
        readApp(Command.AppFirmwareVersion);
    }

    public void readAppGraphicEQBand(GraphicEQPreset preset, int band) {
        if (band < 0 || band >= kGraphicEQNumBands)
            throw new IllegalArgumentException("band out of range");

        byte[] buffer = new byte[kSizeofUInt32 * 3];

        Utility.putUnsignedInt(preset.value(), buffer, 0);
        Utility.putUnsignedInt(band, buffer, 4);

        readApp(Command.AppGraphicEQBand, buffer, kTimeoutMsDefault);
    }

    public void readAppGraphicEQBandFreq() {
        byte[] buffer = new byte[(1 + kGraphicEQNumBands) * kSizeofUInt32];

        Utility.putUnsignedInt(kGraphicEQNumBands, buffer, 0);

        readApp(Command.AppGraphicEQBandFreq, buffer, kTimeoutMsDefault);
    }

    public void readAppGraphicEQCurrentPreset() {
        readApp(Command.AppGraphicEQCurrentPreset);
    }

    public void readAppGraphicEQLimits() {
        readApp(Command.AppGraphicEQLimits);
    }

    public void readAppGraphicEQPresetBandSettings(GraphicEQPreset preset) {
        byte[] buffer = new byte[(2 + kGraphicEQNumBands) * kSizeofUInt32];

        Utility.putUnsignedInt(preset.value(), buffer, 0);
        Utility.putUnsignedInt(kGraphicEQNumBands, buffer, 4);

        readApp(Command.AppGraphicEQPresetBandSettings, buffer, kTimeoutMsDefault);
    }

    public void readAppIsHeadsetOn() {
        readApp(Command.AppIsHeadsetOn);
    }

    public void readAppSensorStatus() {
        readApp(Command.AppSensorStatus);
    }

    public void readAppSmartButtonFeatureIndex() {
        readApp(Command.AppSmartButtonFeatureIndex);
    }

    public void readAppTapDebounce() {
        readApp(Command.AppTapDebounce);
    }

    public void readAppTapIdleDebounce() {
        readApp(Command.AppTapIdleDebounce);
    }

    public void readAppTapPulseWindow() {
        readApp(Command.AppTapPulseWindow);
    }

    public void readAppTapThresholdHigh() {
        readApp(Command.AppTapThresholdHigh);
    }

    public void readAppTapThresholdLow() {
        readApp(Command.AppTapThresholdLow);
    }

    public void readAppTapTimeout() {
        readApp(Command.AppTapTimeout);
    }

    public void readAppVoicePromptEnable() {
        readApp(Command.AppVoicePromptEnable);
    }

//	#mark - read boot commands -

    private void readBootAuthChallenge() {
        read(Command.BootAuthChallenge);
    }

    private void readBootAuthStatus() {
        read(Command.BootAuthStatus);
    }

    public void readBootImageType() {
        read(Command.BootImageType);
    }

    public void readBootVersionFileApp() {
        read(Command.BootReadVersionFile, kTimeoutMsDefault, kVersionFileAppAddress, null);
    }

    public void readBootVersionFileBootloader() {
        read(Command.BootReadVersionFile, kTimeoutMsDefault, kVersionFileBootloaderAddress, null);
    }

    public void readBootVersionFileResource() {
        if (mIs750Device) {
            mVersionFileResourceAddress = kVersionFileResourceAddress750;
        } else {
            mVersionFileResourceAddress = kVersionFileResourceAddress;
        }
        read(Command.BootReadVersionFile, kTimeoutMsDefault, mVersionFileResourceAddress, null);
    }

//	#mark - read config commands -

    public void readConfigBTDisplayName() {
        read(Command.ConfigBTDisplayName);
    }

    public void readConfigFirmwareVersion() {
        read(Command.ConfigFirmwareVersion);
    }

    public void readConfigHardwareVersion() {
        read(Command.ConfigHardwareVersion);
    }

    public void readConfigManufacturerName() {
        read(Command.ConfigManufacturerName);
    }

    public void readConfigModelNumber() {
        read(Command.ConfigModelNumber);
    }

    public void readConfigProductName() {
        read(Command.ConfigProductName);
    }

    public void readConfigSDHMBootVersion() {
        read(Command.ConfigSDHMBootVersion);
    }

    public void readConfigSDHMHardwareVersion() {
        read(Command.ConfigSDHMHardwareVersion);
    }

    public void readConfigSDHMManufacturerName() {
        read(Command.ConfigSDHMManufacturerName);
    }

    public void readConfigSDHMModelNumber() {
        read(Command.ConfigSDHMModelNumber);
    }

    public void readConfigSDHMProductName() {
        read(Command.ConfigSDHMProductName);
    }

    public void readConfigSDHMSerialNumber() {
        read(Command.ConfigSDHMSerialNumber);
    }

    public void readConfigSerialNumber() {
        read(Command.ConfigSerialNumber);
    }

//	#mark - read firmware commands -

    /**
     * readFirmware() allows the caller to read bytes from the application or resource
     * region.  Data is returned via the lightXFirmwareReadStatus() delegate callback.
     *
     * @param region The LightX firmware region (application or resource) to write to
     * @param from   The offset in bytes from the start of the region to begin reading
     * @param to     The offset in bytes from the start of the region to read to stop reading (exclusive)
     */
    public void readFirmware(FirmwareRegion region, int from, int to) {
        firmwareOperationPrepare(region, null, from, to);
    }

//	#mark - write commands -

    private void write(Packet packet) {
        try {
            //Log.d(TAG, "LightX write command is " + packet.command());
            mWriteThread.write(packet);
        } catch (IOException e) {
            mDelegate.lightXError(this, e);
        }
    }

    private void write(Command command) {
        write(command, null, 0, kTimeoutMsDefault);
    }

    private void write(Command command, byte[] buffer) {
        write(command, buffer, 0, kTimeoutMsDefault);
    }

    private void write(Command command, int timeoutMs) {
        write(command, null, 0, timeoutMs);
    }

    private void write(Command command, byte[] buffer, int address, int timeoutMs) {
        write(Packet.writeRequest(mSourceModuleId, command, timeoutMs, address, buffer));
    }

    /*
        private void writeConfigString( Command command, String string, int size ) {
            byte[]						bytes;

            if ( ( bytes = terminatedUTF8StringThatFits( string, size ) ) != null ) {
                write( command, bytes, 0, kTimeoutMsDefault );
            }
        }
    */
    private void writeRegion(Command command, int offset, byte[] buffer) {
        int regionSize, size;

        switch (command) {
            case BootWriteImage:
                regionSize = mIs750Device ? kApplicationSize750 : kApplicationSize;
                mBaseAddress = kVersionFileAppAddress;
                break;
            case BootWriteRsrc1:
                regionSize = mIs750Device ? kResourceSize750 : kResourceSize;
                mBaseAddress = kVersionFileResourceAddress750;
                break;
//			case BootWriteRsrc1:	regionSize = kResource1Size;		break;
//			case BootWriteRsrc2:	regionSize = kResource2Size;		break;

            default:
                throw new IllegalArgumentException("invalid command");
        }

        if (buffer == null || (size = buffer.length) == 0)
            throw new IllegalArgumentException("buffer cannot be empty");

        if (offset < 0) throw new IndexOutOfBoundsException("offset cannot be negative");
        if (size > kMaximumRegionRequestSize)
            throw new IndexOutOfBoundsException("request size exceeds " + kMaximumRegionRequestSize + " bytes");
        if (offset + size > regionSize)
            throw new IndexOutOfBoundsException("request exceeds data region");
        if (mIs750Device) {
            write(command, buffer, offset + mBaseAddress, kTimeoutMsDefault);
        } else {
            write(command, buffer, offset, kTimeoutMsDefault);
        }
    }

    private void writeWithUInt32Argument(Command command, long uint32Arg) {
        writeWithUInt32Arguments(command, new long[]{uint32Arg}, kTimeoutMsDefault);
    }

    private void writeWithUInt32Arguments(Command command, long[] arguments, int timeoutMs) {
        byte[] buffer;
        int i, n;

        final int kSizeofUInt32 = 4;

        n = arguments.length;
        buffer = new byte[kSizeofUInt32 * n];

        for (i = 0; i < n; ++i) {
            Utility.putUnsignedInt(arguments[i], buffer, kSizeofUInt32 * i);
        }
        // add 750nc support
        if (command == Command.BootEraseImageSector || command == Command.BootEraseRsrc1Sector) {
            if (mIs750Device) {
                write(command, buffer, mBaseAddress, timeoutMs);
            } else {
                write(command, buffer, 0, timeoutMs);
            }
        } else {
            write(command, buffer, 0, timeoutMs);
        }
    }

//	#mark - write app commands -

    /**
     * A convenience method that calls <pre>writeApp( command, buffer, timeoutMs )</pre> with the
     * default timeout of 250ms.
     *
     * @param command The application write command to issue.  Must be within the range
     *                Command.AppFirst &lt;= command &lt;= Command.AppLast.  Note that not all commands
     *                may be implemented by the application firmware.
     * @param buffer  The buffer to write to include with the command packet.  Buffer contents are
     *                implementation dependent.
     */
    public void writeApp(Command command, byte[] buffer) {
        writeApp(command, buffer, kTimeoutMsDefault);
    }

    /**
     * writeApp() writes an application parameter to the LightX hardware.  When complete,
     * success or failure will be returned to the delegate via the lightXWriteApp() callback.
     *
     * @param command   The application write command to issue.  Must be within the range
     *                  Command.AppFirst &lt;= command &lt;= Command.AppLast.  Note that not all commands
     *                  may be implemented by the application firmware.
     * @param timeoutMs The timeout in milliseconds to wait before retransmitting the write request.
     *                  Generally this value should be &gt;= 250.
     * @param buffer    The buffer to write to include with the command packet.  Buffer contents are
     *                  implementation dependent.
     */
    public void writeApp(Command command, byte[] buffer, int timeoutMs) {
        if (command.value() < Command.AppFirst.value() || command.value() > Command.AppLast.value()) {
            throw new IndexOutOfBoundsException("command out of range");
        }
        if (buffer.length > kMaximumRegionRequestSize) {
            throw new IndexOutOfBoundsException("buffer size must not exceed " + kMaximumRegionRequestSize + " bytes");
        }

        write(command, buffer, 0, timeoutMs);
    }

    /**
     * Issue an app-command request to the LightX hardware with a boolean argument.  When the
     * hardware responds the LightX delegate's lightXWriteResult() callback will be called to
     * acknowledge success or failure.
     *
     * @param command   The Command.App* command to send to the LightX hardware.
     * @param argument  The boolean argument to send with the command.
     * @param timeoutMs The timeout in milliseconds to wait before retransmitting the write request.
     *                  Generally this value should be &gt;= 250.
     */
    public void writeAppWithBooleanArgument(Command command, boolean argument, int timeoutMs) {
        writeAppWithUInt32Arguments(command, new long[]{argument ? 1 : 0}, timeoutMs);
    }

    /**
     * Issue an app-command request to the LightX hardware with a boolean argument.  When the
     * hardware responds the LightX delegate's lightXWriteResult() callback will be called to
     * acknowledge success or failure.  This command uses the default timeout value of 250ms.
     *
     * @param command  The Command.App* command to send to the LightX hardware.
     * @param argument The boolean argument to send with the command.
     */
    public void writeAppWithBooleanArgument(Command command, boolean argument) {
        writeAppWithBooleanArgument(command, argument, kTimeoutMsDefault);
    }

    /**
     * Issue an app-command request to the LightX hardware with a float argument.  When the
     * hardware responds the LightX delegate's lightXWriteResult() callback will be called to
     * acknowledge success or failure.
     *
     * @param command   The Command.App* command to send to the LightX hardware.
     * @param argument  The float argument to send with the command.
     * @param timeoutMs The timeout in milliseconds to wait before retransmitting the write request.
     *                  Generally this value should be &gt;= 250.
     */
    public void writeAppWithFloatArgument(Command command, float argument, int timeoutMs) {
        byte[] buffer = new byte[4];

        Utility.putFloat(argument, buffer, 0);
        writeApp(command, buffer, timeoutMs);
    }

    /**
     * Issue an app-command request to the LightX hardware with a UInt32 argument.  When the
     * hardware responds the LightX delegate's lightXWriteResult() callback will be called to
     * acknowledge success or failure.
     *
     * @param command  The Command.App* command to send to the LightX hardware.
     * @param argument The UInt32 argument to send with the command (argument will be ANDed with 0xFFFFFFFFL)
     */
    public void writeAppWithUInt32Argument(Command command, long argument) {
        writeAppWithUInt32Arguments(command, new long[]{argument}, kTimeoutMsDefault);
    }

    /**
     * A convenience method that calls
     * <pre>writeAppWithUInt32Arguments( Command command, long[] arguments, int timeoutMs )</pre>
     * with the default timeout of 250ms.
     *
     * @param command   The Command.App* command to send to the LightX hardware.
     * @param arguments The UInt32 arguments to send with the command (each argument will be ANDed with 0xFFFFFFFFL)
     */
    public void writeAppWithUInt32Arguments(Command command, long[] arguments) {
        writeAppWithUInt32Arguments(command, arguments, kTimeoutMsDefault);
    }

    /**
     * Issue an app-command request to the LightX hardware with one or more UInt32 arguments.  When the
     * hardware responds the LightX delegate's lightXWriteResult() callback will be called to
     * acknowledge success or failure.
     *
     * @param command   The Command.App* command to send to the LightX hardware.
     * @param arguments The UInt32 arguments to send with the command (each argument will be ANDed with 0xFFFFFFFFL)
     * @param timeoutMs The timeout in milliseconds to wait for a reply.  If no reply is received within this
     *                  window, the LightX delegate's lightXWillRetransmit() method will be called to
     *                  request permission to continue with the command.
     */
    public void writeAppWithUInt32Arguments(Command command, long[] arguments, int timeoutMs) {
        if (command.value() < Command.AppFirst.value() || command.value() > Command.AppLast.value()) {
            throw new IndexOutOfBoundsException("command out of range");
        }

        writeWithUInt32Arguments(command, arguments, timeoutMs);
    }

//	#mark - write app convenience methods -

    public void writeAppANCAwarenessPreset(ANCAwarenessPreset preset) {
        writeAppWithUInt32Argument(Command.AppANCAwarenessPreset, preset.value());
    }

    public void writeAppANCEnable(boolean enable) {
        writeAppWithBooleanArgument(Command.AppANCEnable, enable);
    }

    public void writeAppANCLevel(float level) {
        if (level < 0 || level > 1)
            throw new IllegalArgumentException("level must be between 0 and 1");

        writeAppWithFloatArgument(Command.AppANCLevel, level, kTimeoutMsDefault);
    }

    public void writeAppAudioEQPreset(AudioEQPreset preset) {
        writeAppWithUInt32Argument(Command.AppAudioEQPreset, preset.value());
    }

    /**
     * writeAppOnEarDetectionWithAutoOff() enables or disables headphone proximity sensing
     * and auto-off behavior when enabled.
     *
     * @param enable If true, headphone proximity detection is enabled and when the hardware
     *               detects that the headphone has been removed for a period of time the
     *               headphones will automatically power off.  Otherwise this feature is disabled.
     */
    public void writeAppOnEarDetectionWithAutoOff(boolean enable) {
        writeAppWithBooleanArgument(Command.AppOnEarDetectionWithAutoOff, enable);
    }

    public void writeAppGraphicEQBand(GraphicEQPreset preset, int band, int value) {
        if (band < 0 || band >= kGraphicEQNumBands)
            throw new IllegalArgumentException("band out of range");
        if (value < kGraphicEQSettingMin || value > kGraphicEQSettingMax)
            throw new IllegalArgumentException("value out of range");

        byte[] buffer = new byte[kSizeofUInt32 * 3];

        Utility.putUnsignedInt(preset.value(), buffer, 0);
        Utility.putUnsignedInt(band, buffer, 4);
        Utility.putInt(value, buffer, 8);

        writeApp(Command.AppGraphicEQBand, buffer, kTimeoutMsDefault);
    }

    public void writeAppGraphicEQCurrentPreset(GraphicEQPreset preset) {
        writeAppWithUInt32Argument(Command.AppGraphicEQCurrentPreset, preset.value());
    }

    public void writeAppGraphicEQDefaultPreset(GraphicEQPreset preset) {
        writeAppWithUInt32Argument(Command.AppGraphicEQDefaultPreset, preset.value());
    }

    public void writeAppGraphicEQFactoryResetPreset(GraphicEQPreset preset) {
        writeAppWithUInt32Argument(Command.AppGraphicEQFactoryResetPreset, preset.value());
    }

    public void writeAppGraphicEQPersistPreset(GraphicEQPreset preset) {
        writeAppWithUInt32Argument(Command.AppGraphicEQPersistPreset, preset.value());
    }

    public void writeAppGraphicEQPresetBandSettings(GraphicEQPreset preset, int[] bands) {
        if (bands.length != kGraphicEQNumBands)
            throw new IllegalArgumentException("invalid band count (!= " + kGraphicEQNumBands + ")");

        byte[] buffer = new byte[(2 + kGraphicEQNumBands) * kSizeofUInt32];
        int i, offset;

        Utility.putUnsignedInt(preset.value(), buffer, 0);
        Utility.putUnsignedInt(kGraphicEQNumBands, buffer, 4);

        for (i = 0, offset = 8; i < kGraphicEQNumBands; ++i, offset += kSizeofUInt32) {
            if (bands[i] < kGraphicEQSettingMin || bands[i] > kGraphicEQSettingMax) {
                throw new IllegalArgumentException("value out of range");
            }
            Utility.putInt(bands[i], buffer, offset);
        }

        writeApp(Command.AppGraphicEQPresetBandSettings, buffer, kTimeoutMsDefault);
    }

    public void writeAppSmartButtonFeatureIndex(boolean isANC) {
        writeAppWithBooleanArgument(Command.AppSmartButtonFeatureIndex, isANC);
    }

    public void writeAppTapCommit() {
        writeApp(Command.AppTapCommit, null);
    }

    public void writeAppTapDebounce(long uint32Value) {
        writeAppWithUInt32Argument(Command.AppTapDebounce, uint32Value);
    }

    public void writeAppTapIdleDebounce(long uint32Value) {
        writeAppWithUInt32Argument(Command.AppTapIdleDebounce, uint32Value);
    }

    public void writeAppTapPulseWindow(long uint32Value) {
        writeAppWithUInt32Argument(Command.AppTapPulseWindow, uint32Value);
    }

    public void writeAppTapThresholdHigh(long uint32Value) {
        writeAppWithUInt32Argument(Command.AppTapThresholdHigh, uint32Value);
    }

    public void writeAppTapThresholdLow(long uint32Value) {
        writeAppWithUInt32Argument(Command.AppTapThresholdLow, uint32Value);
    }

    public void writeAppTapTimeout(long uint32Value) {
        writeAppWithUInt32Argument(Command.AppTapTimeout, uint32Value);
    }

    public void writeAppVoicePromptEnable(boolean enable) {
        writeAppWithBooleanArgument(Command.AppVoicePromptEnable, enable);
    }

    // mandatory app command to prep for jump to bootloader
    private void writeAppShutdown() {
        write(Command.AppShutdown);
    }

//	#mark - write boot commands (authentication required) -

    // commands that require authentication before succeeding

    private void writeBootCommitImage(int sectorCount) {
        writeWithUInt32Arguments(Command.BootCommitImage, new long[]{sectorCount}, 8000);
    }

    private void writeBootJumpToApplication() {
        write(Command.BootJumpToApplication, -8000);
    }

    // test code, jump to app and auto-power-on?
    private void jumpToApplicationAndSendPowerOn() {
        write(Command.BootJumpToApplication, -100);

        byte[] buffer = new byte[kSizeofUInt32];

        Utility.putUnsignedInt(0x80008, buffer, kSizeofUInt32);

        Packet packet = Packet.request(
                mSourceModuleId,
                Packet.Opcode.Set,
                Command.AppIsHeadsetOn,
                1000,
                0,
                buffer
        );

        write(packet);
    }

    private void writeBootJumpToBootloader() {
        write(Command.BootJumpToBootloader, -8000);
    }

    private void writeBootVerifyCRC(int sectorCount, long crc32, FirmwareRegion firmwareRegion) {
        long address;
        long[] args;

        switch (firmwareRegion) {
            case Application: {
                args = new long[]{sectorCount, crc32};
            }
            break;

            case Resource: {
                if (mIs750Device) {
                    mVersionFileResourceAddress = kVersionFileResourceAddress750;
                } else {
                    mVersionFileResourceAddress = kVersionFileResourceAddress;
                }
                address = mVersionFileResourceAddress;
                args = new long[]{sectorCount, crc32, address};
            }
            break;

            default:
                throw new IllegalArgumentException("unsupported firmware region " + firmwareRegion);
        }

        writeWithUInt32Arguments(Command.BootVerifyCRC, args, 8000);
    }

/*
    /**
	 * writeBootResetAppIntegrity() sets the application image upgrade status flag and resets the
	 * application firmware image integrity.  This will cause the bootloader to remain in bootloader
	 * mode while not affecting the actual contents of the application image.
	 * /
	public void writeBootResetAppIntegrity() { write( Command.BootResetAppIntegrity ); }

	/**
	 * writeBootDeveloperMode() configures the firmware to enable developer mode.  In developer mode the
	 * user can force the firmware back into the bootloader by pressing and holding button S3.
	 * Host authentication is required to use this command.
	 *
	 * @param enable true to enable developer mode, false to disable
	 * /
	public void writeBootDeveloperMode( boolean enable ) { writeWithUInt32Argument( Command.BootDeveloperMode, enable ? 1 : 0 ); }
*/

//	#mark - write firmware commands (authentication required) -

    /**
     * writeFirmware() allows the caller to write data into the firmware's application or resource
     * region.  The LightX delegate's lightXFirmwareWriteStatus() method will be called back repeatedly
     * with progress and status information (see the lightXFirmwareWriteStatus() documentation for
     * further detail).  Authentication is required.
     *
     * @param region The LightX firmware region (application or resource) to write to
     * @param data   The data to write to the selected region
     */
    public void writeFirmware(FirmwareRegion region, byte[] data) {
        firmwareOperationPrepare(region, data, 0, 0);
    }

//	#mark - writeConfig commands -


/*
    private void writeConfigFirmwareVersion( String name ) { writeConfigString( Command.ConfigFirmwareVersion, name, 32 ); }
	private void writeConfigHardwareVersion( String name ) { writeConfigString( Command.ConfigHardwareVersion, name, 32 ); }
	private void writeConfigManufacturerName( String name ) { writeConfigString( Command.ConfigManufacturerName, name, 32 ); }
	private void writeConfigModelNumber( String name ) { writeConfigString( Command.ConfigModelNumber, name, 32 ); }
	private void writeConfigProductName( String name ) { writeConfigString( Command.ConfigProductName, name, 32 ); }
	private void writeConfigSDHMHardwareVersion( String name ) { writeConfigString( Command.ConfigSDHMHardwareVersion, name, 16 ); }
	private void writeConfigSDHMSerialNumber( String name ) { writeConfigString( Command.ConfigSDHMSerialNumber, name, 16 ); }
	private void writeConfigSerialNumber( String name ) { writeConfigString( Command.ConfigSerialNumber, name, 32 ); }
*/
    // private methods

//	#mark - firmware state machine -

    private void firmwareCommitImage(boolean success) {
        Exception exception = null;
        int sectors;

        try {
            if (success) {
                sectors = (mFirmwareData.length + kSectorSize - 1) / kSectorSize;

                writeBootCommitImage(sectors);
            } else {
                throw new IOException("image failed checksum verification");
            }
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) firmwareUpdateEnd(exception, true);
    }

    private void firmwareEraseNextBlock() {
        boolean complete = false;
        Exception exception = null;
        double progress;

        try {
            // there is currently no method for tracking which block was successfully
            // erased, so just assume success and move on to the next one.
            ++mFirmwareBlockCurrent;
            // Log.d(TAG, "firmwareEraseNextBlock, mFirmwareBlockCurrent=" + mFirmwareBlockCurrent + ",mFirmwareBlockLast=" + mFirmwareBlockLast);
            progress = (double) mFirmwareBlockCurrent / mFirmwareBlockLast;

            if (!(complete = mDelegate.lightXFirmwareWriteStatus(this, mFirmwareRegion, FirmwareWriteOperation.Erase, progress, null))) {
                if (mFirmwareBlockCurrent < mFirmwareBlockLast) {
                    //Add 750 support
                    int data;
                    if (mIs750Device) {
                        data = mBaseAddress + (mFirmwareBlockCurrent << 12);
                    } else {
                        data = mFirmwareBlockCurrent;
                    }
                    writeWithUInt32Argument(mFirmwareEraseCommand, data);
                } else {
                    mFirmwareBlockCurrent = -1;
                    mFirmwareBlockLast = (mFirmwareData.length + kWriteBlockSize - 1) / kWriteBlockSize;

                    firmwareWriteNextBlock();
                }
            }
        } catch (Exception e) {
            complete = true;
            exception = e;
        }

        if (complete) firmwareUpdateEnd(exception, true);
    }

    private void firmwareOperationPrepare(FirmwareRegion region, byte[] data, int from, int to) {
        int dataSize, regionSize;

        Logger.d(TAG,"firmwareOperationPrepare mFirmwareIsUpdating = " +mFirmwareIsUpdating+",region ="+region);
        firmwareUpdateBegin();

        try {
            switch (region) {
                case Application: {
                    mFirmwareEraseCommand = Command.BootEraseImageSector;
                    mFirmwareReadCommand = Command.BootReadImage;
                    mFirmwareWriteCommand = Command.BootWriteImage;
                    regionSize = mIs750Device ? kApplicationSize750 : kApplicationSize;
                    mBaseAddress = kVersionFileAppAddress;
                }
                break;


                case Resource: {
                    mFirmwareEraseCommand = Command.BootEraseRsrc1Sector;
                    mFirmwareReadCommand = Command.BootReadRsrc1;
                    mFirmwareWriteCommand = Command.BootWriteRsrc1;
                    regionSize = mIs750Device ? kResourceSize750 : kResourceSize;
                    mBaseAddress = kVersionFileResourceAddress750;
                }
                break;
    /*/
                case Resource1: {
					mFirmwareEraseCommand = Command.BootEraseRsrc1Sector;
					mFirmwareReadCommand = Command.BootReadRsrc1;
					mFirmwareWriteCommand = Command.BootWriteRsrc1;
					regionSize = kResource1Size;
				} break;

				case Resource2: {
					mFirmwareEraseCommand = Command.BootEraseRsrc2Sector;
					mFirmwareReadCommand = Command.BootReadRsrc2;
					mFirmwareWriteCommand = Command.BootWriteRsrc2;
					regionSize = kResource2Size;
				} break;
	/*/

                default:
                    throw new IllegalArgumentException("unsupported region");
            }

            mFirmwareRegion = region;

            if (data != null) {
                if (data.length > regionSize)
                    throw new IndexOutOfBoundsException("request exceeds data region");

                mFirmwareBlockCurrent = -1;
                mFirmwareBlockLast = data.length >> 12;
                mFirmwareReadFrom = -1;
                mFirmwareReadTo = -1;

                dataSize = mFirmwareBlockLast * kSectorSize;
                mFirmwareData = new byte[dataSize];

                System.arraycopy(data, 0, mFirmwareData, 0, data.length);

                firmwareEraseNextBlock();
            } else {
                mFirmwareBlockCurrent = from / kMaximumRegionRequestSize - 1;
                mFirmwareBlockLast = (to + kReadBlockSize - 1) / kReadBlockSize;
                mFirmwareData = null;
                mFirmwareReadFrom = from;
                mFirmwareReadTo = to;

                firmwareReadNextBlock(null);
            }
        } catch (Exception e) {
            firmwareUpdateEnd(null, false);
            throw e;
        }
    }

    private void firmwareReadNextBlock(byte[] currentBlockContent) {
        boolean complete = false;
        Exception exception = null;
        int from, offset, size, to;

        try {
            if (currentBlockContent != null) {
                offset = mFirmwareBlockCurrent * kReadBlockSize;

                // this firmware operation is a read, so return the result
                // the first block returned may contain data before the start offset caller asked for
                if (offset + currentBlockContent.length > mFirmwareReadTo) {
                    to = mFirmwareReadTo - offset;
                } else {
                    to = currentBlockContent.length;
                }
                if (offset < mFirmwareReadFrom) {
                    from = mFirmwareReadFrom - offset;
                    offset = mFirmwareReadFrom;
                } else {
                    from = 0;
                }
                if (to - from < currentBlockContent.length) {
                    currentBlockContent = Arrays.copyOfRange(currentBlockContent, from, to);
                }

                complete = mDelegate.lightXFirmwareReadStatus(this, mFirmwareRegion, offset, currentBlockContent, null);
            }

            if (!complete) complete = ++mFirmwareBlockCurrent >= mFirmwareBlockLast;

            if (!complete) {
                offset = mFirmwareBlockCurrent * kReadBlockSize;
                size = Math.min(mFirmwareReadTo - offset, kReadBlockSize);

                readRegion(mFirmwareReadCommand, offset, size);
            }
        } catch (Exception e) {
            complete = true;
            exception = e;
        }

        if (complete) firmwareUpdateEnd(exception, true);
    }

    private synchronized void firmwareUpdateBegin() {
        if (mFirmwareIsUpdating)
            throw new ConcurrentModificationException("a firmware update is already in progress");
        Logger.d(TAG,"firmwareUpdateBegin mFirmwareIsUpdating = " +mFirmwareIsUpdating);
        mFirmwareIsUpdating = true;
    }

    private synchronized void firmwareUpdateEnd(Exception exception, boolean notifyDelegate) {
        Logger.d(TAG,"firmwareUpdateEnd mFirmwareIsUpdating= "+mFirmwareIsUpdating);
        if (mFirmwareIsUpdating) {
            if (notifyDelegate) {
                Logger.d(TAG,"firmwareUpdateEnd notifyDelegate= "+notifyDelegate);
                if (mFirmwareData != null) {        // write operation
                    Logger.d(TAG,"firmwareUpdateEnd mFirmwareData is not null");
                    mDelegate.lightXFirmwareWriteStatus(this, mFirmwareRegion, FirmwareWriteOperation.Complete, exception == null ? 1.0 : 0, exception);
                } else if (exception != null) {    // read operation with error
                    Logger.d(TAG,"firmwareUpdateEnd exception is null ");
                    mDelegate.lightXFirmwareReadStatus(this, mFirmwareRegion, mFirmwareBlockCurrent * kReadBlockSize, null, exception);
                }else {
                    Logger.d(TAG,"firmwareUpdateEnd else");
                }
            }

            mFirmwareData = null;
            mFirmwareIsUpdating = false;
        }
    }

    private void firmwareWriteCRC() {
        long crc32;
        Exception exception = null;
        int sectors;

        final long kCRC32Seed = 0xffffffffL;

        try {
            crc32 = CRC32.calculate(kCRC32Seed, mFirmwareData);
            sectors = (mFirmwareData.length + kSectorSize - 1) / kSectorSize;

            writeBootVerifyCRC(sectors, crc32, mFirmwareRegion);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) firmwareUpdateEnd(exception, true);
    }

    private void firmwareWriteNextBlock() {
        byte[] buffer;
        boolean complete = false;
        Exception exception = null;
        int offset, size;
        double progress;

        try {
            // there is currently no method for tracking which block was successfully
            // written, so just assume success and move on to the next one.
            ++mFirmwareBlockCurrent;

            //Log.d(TAG, "firmwareWriteNextBlock, mFirmwareBlockCurrent " + mFirmwareBlockCurrent + ",mFirmwareBlockLast=" + mFirmwareBlockLast);

            progress = (double) mFirmwareBlockCurrent / mFirmwareBlockLast;

            if (!(complete = mDelegate.lightXFirmwareWriteStatus(this, mFirmwareRegion, FirmwareWriteOperation.Write, progress, null))) {
                if (mFirmwareBlockCurrent < mFirmwareBlockLast) {
                    offset = mFirmwareBlockCurrent * kWriteBlockSize;
                    size = Math.min(mFirmwareData.length - offset, kWriteBlockSize);
                    buffer = new byte[size];

                    System.arraycopy(mFirmwareData, offset, buffer, 0, size);

                    writeRegion(mFirmwareWriteCommand, offset, buffer);
                } else {
                    firmwareWriteCRC();
                }
            }
        } catch (Exception e) {
            complete = true;
            exception = e;
        }

        if (complete) firmwareUpdateEnd(exception, true);
    }

//	#mark - packet processing

    private void processPacket(Packet packet) {
        Command command;
        int value;

        command = packet.command();

        if (!(packet.source == mSourceModuleId || packet.source == ModuleId.Application)) {
            Logger.w(TAG, "Unexpected packet source " + packet.source + " (should be " + mSourceModuleId + " or " + ModuleId.Application + "), ignoring");
            return;
        }

        if (!packet.isPush()) {
            synchronized (mSocketLock) {
                if (mRequest != null && packet.param0() == mRequest.param0()) {
                    // this is the ack of the current write request, mark received and continue
                    mRequest = null;
                    mSocketLock.notify();
                } else {
                    Logger.w(TAG, "received packet is not a push command and was received outside of request loop, ignoring");
                    return;
                }
            }
        } else {
            mDelegate.lightXAppReceivedPush(this, command, packet.buffer);
            return;
        }

        value = command.value();

        switch (packet.status) {
            case Ok:
                break;

            default:
            case CommFailure:
            case ExceededLimits:
            case Fail:
            case NotFound:
            case NotReady:
            case NotSupported: {
                Logger.e(TAG, "receive " + command + " " + packet.status);

                switch (packet.opcode) {
                    case Read: {
                        if (value >= Command.ConfigFirst.value() && value <= Command.ConfigLast.value()) {
                            mDelegate.lightXReadConfigResult(this, command, false, null);
                        } else if (value >= Command.BootFirst.value() && value <= Command.BootLast.value()) {
                            mDelegate.lightXReadBootResult(this, command, false, packet.address(), null);
                        } else if (value >= Command.AppFirst.value() && value <= Command.AppLast.value()) {
                            switch (command) {
                                default: {
                                    mDelegate.lightXAppReadResult(this, command, false, null);
                                }
                                break;
                            }
                        }
                    }
                    break;

                    case Write: {
                        if (value >= Command.BootFirst.value() && value <= Command.BootLast.value()) {
                            switch (command) {
                                case BootCommitImage: {
                                    firmwareUpdateEnd(new IOException("boot commit image failed, cannot jump to application"), true);
                                }
                                break;
                            }
                        } else if (value >= Command.AppFirst.value() && value <= Command.AppLast.value()) {
                            mDelegate.lightXAppWriteResult(this, command, false);
                        }
                    }
                    break;
                }
            }
            return;

            case Pending: {
                Logger.d(TAG, "receive " + command + " " + packet.status);
            }
            return;
        }

        switch (packet.opcode) {
            case Read: {
                if (value >= Command.ConfigFirst.value() && value <= Command.ConfigLast.value()) {
                    mDelegate.lightXReadConfigResult(this, command, true, packet.bufferString());
                } else if (value >= Command.BootFirst.value() && value <= Command.BootLast.value()) {
                    switch (command) {
                        case BootAuthChallenge: {
                            write(Command.BootAuthResponse, calculateBootAuthChallengeResponse(packet.buffer));
                        }
                        break;

                        case BootAuthStatus: {
                            mIsAuthenticated = packet.buffer[0] == 1;

                            if (mIsAuthenticated && !mIsInBootloader) {
                                // begin bootloader entry phase 2
                                writeAppShutdown();
                            }
                        }
                        break;

                        case BootImageType: {
                            mIsInBootloader = packet.buffer[0] == 0;

                            mDelegate.lightXIsInBootloader(this, mIsInBootloader);
                        }
                        break;

                        case BootReadImage:
                        case BootReadRsrc1:
                        case BootReadRsrc2: {
                            // advance firmwareRead()
                            firmwareReadNextBlock(packet.buffer);
                        }
                        break;

                        default: {
                            mDelegate.lightXReadBootResult(this, command, true, packet.address(), packet.buffer);
                        }
                        break;
                    }
                } else if (value >= Command.AppFirst.value() && value <= Command.AppLast.value()) {
                    switch (command) {
                        default: {
                            mDelegate.lightXAppReadResult(this, command, true, packet.buffer);
                        }
                        break;
                    }
                }
            }
            break;

            case Write: {
                if (value >= Command.AppFirst.value() && value <= Command.AppLast.value()) {
                    mDelegate.lightXAppWriteResult(this, command, true);
                } else {
                    switch (command) {
                        case AppShutdown: {
                            // begin bootloader entry phase 3
                            writeBootJumpToBootloader();
                        }
                        break;

                        case BootAuthResponse: {
                            readBootAuthStatus();
                        }
                        break;

                        case BootCommitImage: {
                            firmwareUpdateEnd(null, true);
                        }
                        break;

                        case BootJumpToBootloader: {
                            // jump to bootloader sequence is complete, test bootloader status
                            readBootImageType();
                        }
                        break;

                        case BootEraseImageSector:
                        case BootEraseRsrc1Sector:
                        case BootEraseRsrc2Sector: {
                            firmwareEraseNextBlock();
                        }
                        break;

                        case BootWriteImage:
                        case BootWriteRsrc1:
                        case BootWriteRsrc2: {
                            firmwareWriteNextBlock();
                        }
                        break;

                        case BootVerifyCRC: {
                            switch (mFirmwareRegion) {
                                case Application:
                                    firmwareCommitImage(true);
                                    break;
                                case Resource:
                                    firmwareUpdateEnd(null, true);
                                    break;
                            }
                        }
                        break;
                    }
                }
            }
            break;
        }
    }

//	#mark - Write Config Commands -

    private byte[] calculateBootAuthChallengeResponse(byte[] challenge) {
        byte[] result = new byte[16];

        if (challenge.length >= result.length) {
/**/
            // insecure method
            for (int i = 0; i < 16; ++i) {
                result[i] = (byte) (challenge[i] ^ 0x5d);
            }
/*/
            // secure method
			String					password;
			byte[]					passwordHash;

//			Log.d( "Challenge:\n" + Debug.hexify( challenge, 0, 16 ) );

			if ( ( password = mDelegate.lightXGetAuthenticationPassword( this ) ) != null ) {
				try {
					// hash here to make sure we have at least 16 bytes of data to use against the challenge.
					passwordHash = MessageDigest.getInstance( "SHA-256" ).digest( password.getBytes( "UTF-8" ) );

					// verification algorithm on board is challenge[ i ] ^ 0x5d = response[ i ]
					//
					// we secure this as follows:
					//
					// 1.  generate a password to give to the client
					//
					// 2.  uncomment the loop below and precompute for challenge[ i ] a constant that
					//	   is the solution to this equation:
					//
					//     challenge[ i ] ^ 0x5d = ( passwordHash[ i ] + constant[ i ] ) mod 256
					//
					//     i.e.
					//
					//     constant = ( ( challenge[ i ] ^ 0x5d ) - passwordHash[ i ] ) mod 256
					//
					// 3.  run the code once and let the loop below generate the appropriate constants.
					//     as of 7/11/21 the constants correspond to a challenge of:
					//
					//     A7 F1 D9 2A 82 C8 D8 FE 43 4D 98 55 8C E2 B3 47
					//
					//     Which the B0 and D0 boards always return immediately after boot.
					//
					// 4.  Copy the code generated by the loop below from the console into this method.
					//
					// 5.  Comment out the for loop and build a .jar for the client.

					// COMMENT OUT THIS LOOP BEFORE SHIPPING!
//					for ( int i = 0; i < 16; ++i ) {
//						result[ i ] = (byte)(challenge[ i ] ^ 0x5d);
//						Log.d( String.format( "result[ %d ] = (byte)(passwordHash[ %d ] + (byte)0x%02x);",
//							i, i, (byte)(result[ i ] - passwordHash[ i ])
//						));
//					}

					// these constants map to the first 16 bytes of SHA256( "AvneraLightX2015" ) vs.
					// a challenge of A7 F1 D9 2A 82 C8 D8 FE 43 4D 98 55 8C E2 B3 47
					result[ 0 ] = (byte)(passwordHash[ 0 ] + (byte)0xf6);
					result[ 1 ] = (byte)(passwordHash[ 1 ] + (byte)0x6f);
					result[ 2 ] = (byte)(passwordHash[ 2 ] + (byte)0xd1);
					result[ 3 ] = (byte)(passwordHash[ 3 ] + (byte)0x9a);
					result[ 4 ] = (byte)(passwordHash[ 4 ] + (byte)0x37);
					result[ 5 ] = (byte)(passwordHash[ 5 ] + (byte)0xf3);
					result[ 6 ] = (byte)(passwordHash[ 6 ] + (byte)0xd8);
					result[ 7 ] = (byte)(passwordHash[ 7 ] + (byte)0x46);
					result[ 8 ] = (byte)(passwordHash[ 8 ] + (byte)0xb2);
					result[ 9 ] = (byte)(passwordHash[ 9 ] + (byte)0xc2);
					result[ 10 ] = (byte)(passwordHash[ 10 ] + (byte)0x26);
					result[ 11 ] = (byte)(passwordHash[ 11 ] + (byte)0x94);
					result[ 12 ] = (byte)(passwordHash[ 12 ] + (byte)0x65);
					result[ 13 ] = (byte)(passwordHash[ 13 ] + (byte)0x70);
					result[ 14 ] = (byte)(passwordHash[ 14 ] + (byte)0xb4);
					result[ 15 ] = (byte)(passwordHash[ 15 ] + (byte)0xdb);
				} catch ( Exception e ) { }
			}
/**/
        }

        return result;
    }

/*
    // I think it's true that the device config string max lengths, say
	// 32 bytes, are buffer sizes + terminating null byte.  Since the
	// card accepts UTF-8 however, a character may be more than one byte.
	// So this function does a binary search over the input string to
	// find the null-terminated UTF-8 variant that fits in size bytes.
	private byte[] terminatedUTF8StringThatFits( String string, int size ) {
		byte[]						b, best;
		int							high, l, low, middle;
		String						s;

		try {
			best = null;
			low = 0;
			high = string.length() - 1;
			size--;                        // account for zero termination

			while ( low <= high ) {
				middle = low + ( high - low ) / 2;

				s = string.substring( 0, middle );
				b = s.getBytes( "UTF-8" );
				l = b.length;

				if ( size < l ) {
					high = middle - 1;
				} else if ( size > l ) {
					best = b;
					low = middle + 1;
				} else {
					best = b;
					break;
				}
			}

			if ( best != null ) {
				b = new byte[ best.length + 1 ];
				System.arraycopy( best, 0, b, 0, best.length );
				b[ best.length ] = 0;
				best = b;
			}
		} catch ( UnsupportedEncodingException e ) {
			best = null;
		}

		return best;
	}
*/

    // The read thread runs concurrently to the write thread in order to support
    // asynchronous arrival of unexpected packets (e.g. "push" packets from sensor data).
    // Most communication however is serialized to one write followed by a wait for
    // an acknowledgement packet.
    private class ReadThread extends Thread {
        private ByteArrayOutputStream mBuffer;
        private boolean mDone;
        private final InputStream mInputStream;

        public ReadThread(InputStream inputStream) {
            mBuffer = new ByteArrayOutputStream();
            mInputStream = inputStream;
        }

        public void close() {
            mDone = true;
            try {
                mInputStream.close();
            } catch (IOException e) {
            }
        }

        public void run() {
            byte[] buffer = new byte[4096], data;
            int consumed, count, dataLength;

            try {
                while (!mDone) {
                    if ((count = mInputStream.read(buffer)) > 0) {
                        mBuffer.write(buffer, 0, count);
                        if ((dataLength = mBuffer.size()) > 0) {
                            data = mBuffer.toByteArray();
                            if ((consumed = consume(data)) > 0) {
                                mBuffer = new ByteArrayOutputStream();

                                if (consumed < dataLength) {
                                    mBuffer.write(data, consumed, dataLength - consumed);
                                }
                            }
                        }
                    } else if (count < 0) {
                        break;
                    }
                }
            } catch (Exception e) {
                Logger.e(TAG, "LightX exception on read thread: " + e.getLocalizedMessage() + ", backtrace:\n" + Debug.stackTrace(e));

                firmwareUpdateEnd(e, true);

                mDelegate.lightXError(LightX.this, e);
            } finally {
                close();
            }
        }
    }

    private class WriteThread extends Thread {
        private boolean mDone;
        private final OutputStream mOutputStream;
        private List<Packet> mPacketQueue;

        public WriteThread(OutputStream outputStream) {
            mPacketQueue = new ArrayList<>();
            mOutputStream = outputStream;
        }

        public void close() {
            synchronized (this) {
                mDone = true;
                notify();
            }
            synchronized (mSocketLock) {
                mSocketLock.notify();
            }
        }

        public synchronized void write(Packet packet) throws IOException {
            packet.timeoutMs = packet.timeoutMs >= 0 ? Math.max(packet.timeoutMs, 2000) : Math.min(packet.timeoutMs, -2000);
            mPacketQueue.add(packet);
            notify();
        }

        public void run() {
            try {
                runLoop:
                for (; ; ) {
                    byte[] buffer;
                    List<Packet> packetQueue;
                    int sendCount, totalElapsedMs;

                    final int kMaxTimesToSend = 1;
//					final int		kMaxTimesToSend = 5;

                    for (; ; ) {
                        synchronized (this) {
                            if (mDone) break runLoop;

                            if (!mPacketQueue.isEmpty()) {
                                packetQueue = mPacketQueue;
                                mPacketQueue = new ArrayList<>();
                                break;
                            } else {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }

                    for (Packet packet : packetQueue) {
                        if (mDone) break runLoop;

                        synchronized (mSocketLock) {
                            // set the request so we know what type of response to look for.
                            // any packets that come in while request is set that don't match
                            // will be either queued to the push queue (e.g. sensor readings)
                            // or discarded (anything else).
                            mRequest = packet;
                        }

                        buffer = packet.packetize();

                        for (sendCount = totalElapsedMs = 0; ; totalElapsedMs += packet.timeoutMs) {
                            if (mDone) break runLoop;

                            if (sendCount < kMaxTimesToSend) {
                                if (sendCount > 0) {
                                    Logger.w(TAG, "timeout waiting for reply, retransmitting " + packet.command() + " packet (retransmit #" + sendCount + ")");
                                }

                                //Log.d(TAG, "mOutputStream.write() command is " + packet.command());
                                mOutputStream.write(buffer);

                                if (sEnablePacketDumps) {
                                    Logger.d(TAG, "sent " + packet.opcode + " " + packet.command() + " (" + buffer.length + " bytes to socket):\n" + Debug.hexify(buffer));
                                }

                                ++sendCount;
                            }

                            // we sent the packet, now wait for a response or retransmit timeout
                            synchronized (mSocketLock) {
                                // the read queue sets mRequest to null when the packet is ack'ed
                                if (mRequest == null) break;

                                // otherwise wait for the reply or timeout
                                try {
                                    mSocketLock.wait(Math.abs(packet.timeoutMs));
                                } catch (InterruptedException e) {
                                }

                                if (mRequest == null) break;

                                // a negative timeoutMs interval means don't retransmit
                                if (packet.timeoutMs < 0) {
                                    mRequest = null;
                                    break;
                                }
                                if (mDelegate.lightXAwaitingReply(LightX.this, packet.command(), totalElapsedMs)) {
                                    throw new TimeoutException("no reply received for " + packet.command() + "command in " + totalElapsedMs + "ms");
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.e(TAG, "LightX exception on write thread: " + e.getLocalizedMessage() + ", " + e.getStackTrace());

                firmwareUpdateEnd(e, true);

                mDelegate.lightXError(LightX.this, e);
            } finally {
                try {
                    mOutputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
