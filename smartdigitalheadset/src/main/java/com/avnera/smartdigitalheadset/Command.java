/*
 * LightXCommand.java
 * Smart Digital Headset
 *
 * Created by Brian Doyle on 7/16/15
 * Copyright (c) 2015 Avnera Corporation
 *
 */
package com.avnera.smartdigitalheadset;

/**
 * @author Brian Doyle on 7/16/15.
 */
public enum Command {
	Reserved						( 0x00 ),

	// config parameters, bootloader mode only

	ConfigProductName				( 0x01 ),
	ConfigManufacturerName			( 0x02 ),
	ConfigModelNumber				( 0x03 ),
	ConfigSerialNumber				( 0x04 ),
	ConfigHardwareVersion			( 0x05 ),
	ConfigFirmwareVersion			( 0x06 ),
//	ConfigEAProtocolName			( 0x07 ),
//	ConfigAppBundleId				( 0x08 ),
//	ConfigHIDDescriptor				( 0x09 ),
//	ConfigReserved					( 0x0a - 0x0f ),
//	ConfigCommitChanges				( 0x10 ),
	ConfigBTDisplayName				( 0x11 ),
//	ConfigReserved					( 0x12 - 0x19 ),
	ConfigSDHMProductName			( 0x1a ),
	ConfigSDHMManufacturerName		( 0x1b ),
	ConfigSDHMModelNumber			( 0x1c ),
	ConfigSDHMSerialNumber			( 0x1d ),
	ConfigSDHMBootVersion			( 0x1e ),
	ConfigSDHMHardwareVersion		( 0x1f ),

	// boot parameters

	BootImageType					( 0x20 ),
//	BootReserved					( 0x21 ),
	BootAuthStatus					( 0x22 ),
	BootAuthChallenge				( 0x23 ),
	BootAuthResponse				( 0x24 ),
	BootJumpToBootloader			( 0x25 ),
	BootEraseImageSector			( 0x26 ),
	BootWriteImage					( 0x27 ),
	BootReadImage					( 0x28 ),
	BootCommitImage					( 0x29 ),
	BootJumpToApplication			( 0x2a ),
//	BootDeveloperMode				( 0x2b ),
	BootVerifyCRC					( 0x2c ),
	BootEraseRsrc1Sector			( 0x2d ),
	BootWriteRsrc1					( 0x2e ),
	BootReadRsrc1					( 0x2f ),
	BootEraseRsrc2Sector			( 0x30 ),
	BootWriteRsrc2					( 0x31 ),
	BootReadRsrc2					( 0x32 ),
//	BootResetAppIntegrity			( 0x33 ),
//	BootReserved					( 0x34 - 0x39 ),
	BootReadVersionFile				( 0x3a ),
//	BootReserved					( 0x3b - 0x3f ),

	// diagnostic parameters
/*
	DiagDvmChannel					( 0x40 ),	// moved to 0x65?
	DiagDvmRead						( 0x41 ),	// moved to 0x66?
	DiagGpioSelect					( 0x42 ),
	DiagGpioPinConfig				( 0x43 ),
	DiagGpioValue					( 0x44 ),
	DiagAudioSourceSelect			( 0x45 ),
	DiagMicInputMuxSelect			( 0x46 ),
	DiagMicrophoneEnableCount		( 0x47 ),
	DiagHeadphoneEnableMask			( 0x48 ),
	DiagHeadphoneVolume				( 0x49 ),
	DiagMicrophoneVolume			( 0x4a ),
	DiagMicrophonePreampGain		( 0x4b ),
//	DiagInputDetect					( 0x4c ),	// reserved?
	DiagOutputStreamStatus			( 0x4d ),
	DiagInputStreamStatus			( 0x4e ),
	DiagMicrophoneGain				( 0x4f ),
	DiagDspElectricFlat				( 0x50 ),
	DiagDspMicLoopback				( 0x51 ),
	DiagDspPeakCycles				( 0x52 ),
	DiagDspGeneratorEnable			( 0x53 ),
	DiagDspGeneratorLevel			( 0x54 ),
	DiagDspGeneratorFrequency		( 0x55 ),
	DiagDspGeneratorRouting			( 0x56 ),
	DiagVcpEnable					( 0x57 ),
	DiagVcpAudioRouting				( 0x58 ),
//	DiagReserved					( 0x59 - 0x5f ),
//	DiagReservedForCalibration		( 0x60 - 0x6f ),
	DiagGpioInputEnable				( 0x60 ),
	DiagGpioOutputEnable			( 0x61 ),
	DiagGpioPure					( 0x62 ),
	DiagGpioPdre					( 0x63 ),
	DiagGpioState					( 0x64 ),
//	DiagDvmChannel					( 0x65 ),
//	DiagDvmRead						( 0x66 ),
	DiagAssertTest					( 0x67 ),
	DiagAncEnable					( 0x70 ),
	DiagAncStartCalibration			( 0x71 ),
	DiagAncStoreCalibrationData		( 0x72 ),
	DiagAncFfrPreampGain			( 0x73 ),
//	DiagAncFbrPreampGain			( 0x74 ),
//	DiagAncFflPreampGain			( 0x75 ),
//	DiagAncFblPreampGain			( 0x76 ),
	DiagAppSetTerminalState			( 0x74 ),
	DiagBDAddr						( 0x75 ),
	DiagBtLinkKey					( 0x76 ),
	DiagBtTestMode					( 0x77 ),
	DiagBtLinkServiceControl		( 0x78 ),
	DiagBtHfpServiceControl			( 0x79 ),
	DiagBtAvrcpServiceControl		( 0x7a ),
	DiagBtHfpServiceControl1		( 0x7b ),
	DiagBtHfpServiceControl2		( 0x7c ),
	DiagBtFccTx						( 0x7d ),
	DiagBtFccPacketTxRx				( 0x7e ),
*/

	AppShutdown						( 0x7f ),		// this parameter is mandatory for the app to implement

	App_0x80						( 0x80 ),		// read-only
	AppAudioEQPreset				( 0x81 ),		// read-write
	AppMicEQ						( 0x82 ),		// read-write
	AppSysState						( 0x83 ),		// read-only
	AppIsHeadsetOn					( 0x84 ),
	AppANCEnable					( 0x85 ),		// read-write
	AppANCAwarenessPreset			( 0x86 ),		// read-write
	AppFirmwareVersion				( 0x87 ),		// read-only
	AppANCLevel						( 0x88 ),
	AppAwarenessRawLeft				( 0x89 ),		// read-only
	AppAwarenessRawRight			( 0x8a ),		// read-only
	AppAwarenessRawSteps			( 0x8b ),		// read-only
	AppSpatialPreset				( 0x8c ),
//	App_0x8D						( 0x8d ),		// reserved
	AppGraphicEQSetting				( 0x8e ),		// read-write
//	App_0x8F						( 0x8f ),		// reserved
	AppGraphicEQLimits				( 0x90 ),		// read-only
	AppGraphicEQDefaultPreset		( 0x91 ),		// write-only
	AppGraphicEQCurrentPreset		( 0x92 ),		// read-write
	AppGraphicEQFactoryPresetReset	( 0x93 ),		// read-only
	AppGraphicEQPresetBandSettings	( 0x94 ),		// read-write
	AppGraphicEQBand				( 0x95 ),		// read-write
	AppGraphicEQPersistPreset		( 0x96 ),		// write-only
	AppGraphicEQBandFreq			( 0x97 ),
	AppGraphicEQFactoryResetPreset	( 0x98 ),		// write-only
	App_0x99						( 0x99 ),		// write-only
	App_0x9A						( 0x9a ),
	App_0x9B						( 0x9b ),
	App_0x9C						( 0x9c ),
	App_0x9D						( 0x9d ),
	App_0x9E						( 0x9e ),
	App_0x9F						( 0x9f ),
	AppSensorStatus					( 0xa0 ),		// read-only
	AppTapThresholdHigh				( 0xa1 ),		// read-write
	AppTapThresholdLow				( 0xa2 ),		// read-write
	AppTapPulseWindow				( 0xa3 ),		// read-write
	AppTapTimeout					( 0xa4 ),		// read-write
	AppTapDebounce					( 0xa5 ),		// read-write
	AppTapIdleDebounce				( 0xa6 ),		// read-write
	AppTapCommit					( 0xa7 ),		// write-only
	App_0xA8						( 0xa8 ),		// read-write
	App_0xA9						( 0xa9 ),		// read-write
	App_0xAA						( 0xaa ),		// read-write
	App_0xAB						( 0xab ),		// read-write
	App_0xAC						( 0xac ),		// read-write
	App_0xAD						( 0xad ),		// read-write
	App_0xAE						( 0xae ),		// read-write
	App_0xAF						( 0xaf ),		// read-write
	AppBatteryLevel					( 0xb0 ),		// read-only
	App_0xB1						( 0xb1 ),		// read-write
	App_0xB2						( 0xb2 ),		// read-write
	App_0xB3						( 0xb3 ),		// read-write
	App_0xB4						( 0xb4 ),		// read-write
	AppSmartButtonFeatureIndex		( 0xb5 ),		// read-write
	AppOnEarDetectionWithAutoOff	( 0xb6 ),		// read-write
	AppVoicePromptEnable			( 0xb7 ),		// read-write
	App_0xB8						( 0xb8 ),		// read-write
	App_0xB9						( 0xb9 ),		// read-write
	App_0xBA						( 0xba ),		// read-write
	App_0xBB						( 0xbb ),		// read-write
	App_0xBC						( 0xbc ),		// read-write
	App_0xBD						( 0xbd ),		// read-write
	App_0xBE						( 0xbe ),		// read-write
	App_0xBF						( 0xbf ),		// read-write
	AppPushHeartrateSensorData		( 0xc0 ),		// read-only
//	App_0xC1						( 0xc1 ),		// reserved
//	App_0xC2						( 0xc2 ),		// reserved
//	App_0xC3						( 0xc3 ),		// reserved
//	App_0xC4						( 0xc4 ),		// reserved
	AppPushANCEnable				( 0xc5 ),		// read-only
	AppPushANCAwarenessPreset		( 0xc6 ),		// read-only
	AppPush9AxisRawData				( 0xc7 ),		// read-only
	AppPushTapDetect				( 0xc8 ),		// read-only
	App_0xC9						( 0xc9 ),		// read-only
	App_0xCA						( 0xca ),		// read-only
	App_0xCB						( 0xcb ),		// read-only
	App_0xCC						( 0xcc ),		// read-only
	App_0xCD						( 0xcd ),		// read-only
	App_0xCE						( 0xce ),		// read-only
	App_0xCF						( 0xcf ),		// read-only
	App_0xD0						( 0xd0 ),		// read-only
	App_0xD1						( 0xd1 ),		// read-only
	App_0xD2						( 0xd2 ),		// read-only
	App_0xD3						( 0xd3 ),		// read-only
	App_0xD4						( 0xd4 ),		// read-only
	App_0xD5						( 0xd5 ),		// read-only
	App_0xD6						( 0xd6 ),		// read-only
	App_0xD7						( 0xd7 ),		// read-only
	App_0xD8						( 0xd8 ),		// read-only
	App_0xD9						( 0xd9 ),		// read-only
	App_0xDA						( 0xda ),		// read-only
	App_0xDB						( 0xdb ),		// read-only
	App_0xDC						( 0xdc ),		// read-only
	App_0xDD						( 0xdd ),		// read-only
	App_0xDE						( 0xde ),		// read-only
	App_0xDF						( 0xdf ),		// read-only
	App_0xE0						( 0xe0 ),		// read-only
	App_0xE1						( 0xe1 ),		// read-only
	App_0xE2						( 0xe2 ),		// read-only
	App_0xE3						( 0xe3 ),		// read-only
	App_0xE4						( 0xe4 ),		// read-only
	App_0xE5						( 0xe5 ),		// read-only
	App_0xE6						( 0xe6 ),		// read-only
	App_0xE7						( 0xe7 ),		// read-only
	App_0xE8						( 0xe8 ),		// read-only
	App_0xE9						( 0xe9 ),		// read-only
	App_0xEA						( 0xea ),		// read-only
	App_0xEB						( 0xeb ),		// read-only
	App_0xEC						( 0xec ),		// read-only
	App_0xED						( 0xed ),		// read-only
	App_0xEE						( 0xee ),		// read-only
	App_0xEF						( 0xef ),		// read-only
	App_0xF0						( 0xf0 ),		// read-only
	App_0xF1						( 0xf1 ),		// read-only
	App_0xF2						( 0xf2 ),		// read-only
	App_0xF3						( 0xf3 ),		// read-only
	App_0xF4						( 0xf4 ),		// read-only
	App_0xF5						( 0xf5 ),		// read-only
	App_0xF6						( 0xf6 ),		// read-only
	App_0xF7						( 0xf7 ),		// read-only
	App_0xF8						( 0xf8 ),		// read-only
	App_0xF9						( 0xf9 ),		// read-only
	App_0xFA						( 0xfa ),		// read-only
	App_0xFB						( 0xfb ),		// read-only
	App_0xFC						( 0xfc ),		// read-only
	App_0xFD						( 0xfd ),		// read-only
	App_0xFE						( 0xfe ),		// read-only
	App_0xFF						( 0xff ),		// read-only

	ConfigFirst						( 0x01 ),
	ConfigLast						( 0x1f ),
	BootFirst						( 0x20 ),
	BootLast						( 0x3f ),
//	DiagFirst						( 0x40 ),
//	DiagLast						( 0x7f ),
	AppFirst						( 0x80 ),
	AppLast							( 0xff ),
	PushFirst						( 0xc0 ),
	PushLast						( 0xff ),

	LastEntry						( 0x00 );		// the last entry in the list, unused otherwise

	private final int				value;

	Command( int value ) {
		this.value = value;
	}

	public static Command from( int value ) {
		for ( Command v : Command.values() ) {
			if ( value == v.value ) {
				return v;
			}
		}

		throw new RuntimeException( String.format( "invalid Command value 0x%x", value ) );
	}

	public int value() { return value; }
}
