package com.harman.bluetooth.ota;

public class BesCommand {
    /**
     * Send command.
     * Firstly, request MTU, then begin OTA, send below data.
     * 80		       Command
     * 42 45 53 54     Magic code
     * xx yy mm nn	   Image size, upgrade file size, 4 bytes
     * aa bb cc dd	   4bytes CRC32 result
     * Last 8bytes need to calculate from OTA file.
     */
    public static final byte[] OTA_START_HEAD = {(byte) 0x80,0x42,0x45,0x53,0x54};

    /**
     * Response command
     * After send data 'OTA_START_HEAD' to device, wait for 2s, will receive below command
     * 81		       Command
     * 42 45 53 54     magic code
     * aa bb cc dd	   Software Version：0xbbaa, Hardware Version: 0xddcc
     * xx yy		   MTU length after exchange, every one of package's length must be MTU length in the data transferring.
     */
    public static final byte[] OTA_START_HEAD_RESPONSE = {(byte) 0x81,0x42,0x45,0x53,0x54};


    /**
     * Send command
     * 86		        Command
     * xx yy mm nn	    Length of following data，include the length of CRC32 at tail
     * aa bb cc dd	    Flash offset of the image，read last 4bytes from upgrade file to generate
     *                  Eg: last 4bytes - 0x00 0x80 0x01 0x6C, take the first three bytes to fill in 'aa bb cc', and dd set tp 0x00
     *                  so generated: 0x00 0x80 0x01 0x00.
     * ee ff gg hh	    Enable, only first byte 'ee' use 'shift operation' to enable or disable.
     *                  bit 0- isToClearUserData   ee - 0000 0001
     *                  bit 1- isToRenameBT        ee - 0000 0010
     *                  bit 2- isToRenameBLE       ee - 0000 0100
     *                  bit 3- isToUpdateBTAddr    ee - 0000 1000
     *                  bit 4- isToUpdateBLEAddr   ee - 0001 0000
     * 32bytes' BT name	          All bytes set zero when isToRenameBT is unchecked
     * 32bytes' BLE name          All bytes set zero when isToRenameBLE is unchecked
     * 6bytes' BT mac address     All bytes set zero when isToUpdateBTAddr is unchecked
     * 6bytes' BLE mac address    All bytes set zero when isToUpdateBLEAddr is unchecked
     * Crc32			          The CRC32 value of all the bytes above
     *
     */
    public static final byte[] OTA_SEND_CONFIGE = {(byte) 0x86};

    /**
     * Receive command
     * 87		        Command
     * aa               0x01 - Config Success
     *                  0x00 - Config failed
     */
    public static final byte[] OTA_SEND_CONFIGE_RESPONSE = {(byte) 0x87};

    /**
     * Send command
     * 85		               Command
     * segment{
     *   data                  The maximum size is (MTU -1) bytes, one segment is one percent of image size,
     *   data                  Use this segment for CRC check, the package of segment n = (segment size + (MTU -1 )-1/(MTU -1)
     *   ...                   And the last package size is segment size - (n -1) * ( MTU -1)
     * }
     * CRC32{aa bb cc dd}      4bytes of CRC32
     * eg: {0x85, ..., 0x85, ..., 0x85, ..., 0x82, 0x42, 0x45, 0x53, 0x54, 4bytes of CRC}
     */
    public static final byte[] OTA_SEND_SEGMENT = {(byte) 0x85};

    /**
     * Receive command
     * 83		               Command
     * xx                      0x01 - Check received data successful
     *                         0x00 - Check received failed
     */
    public static final byte[] OTA_SEND_SEGMENT_RESPONSE = {(byte) 0x83};

    /**
     * Send command
     * 88                      Command
     * After all data have sent to device, this is the last byte to send.
     */
    public static final byte[] OTA_SEND_OTA_OVER = {(byte) 0x88};

    /**
     * Receive command
     * 84                      Command
     * xx                      0x01 - update successful
     *                         0x00 - update failed
     */
    public static final byte[] OTA_OTA_OVER_RESPONSE = {(byte) 0x84};
}
