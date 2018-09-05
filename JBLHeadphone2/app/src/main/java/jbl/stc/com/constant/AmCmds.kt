package jbl.stc.com.constant


/**
 * Defined commands and related to commands.
 */
object AmCmds {
    /**
     * REQ_ANC
     * Noise cancel or enable.
     * Implement as toggle button.
     */
    const val CMD_ANC = "ANC"
    /**
     *  Removed by firmware, do not use it
     */
    const val CMD_ANCNotification = "ANCNotification"
    /**
     * Ambient voice
     * Aware of ambient voice.
     * Level: off,low,medium,high
     */
    const val CMD_AmbientLeveling = "AmbientLeveling"
    /**
     * Aware of left/right ambient voice.
     * Values must be in 0-7.
     */
    const val CMD_RawLeft = "RawLeft"
    const val CMD_RawRight = "RawRight"
    /**
     * The max step of ambient voice.
     * 150NC max step is 8 (0-7)
     */
    const val CMD_RawSteps = "RawSteps"

    const val CMD_ResourceVersion = "ResourceVersion"
    /**
     * Use this command to get firmware version from accessory.
     */
    const val CMD_FirmwareVersion = "FirmwareVersion"
    /**
     * Use this command to get battery level from accessory.
     */
    const val CMD_BatteryLevel = "BatteryLevel"
    /**
     * 150NC do not support calibration.
     */
    const val CMD_CalibrationStatus = "CalibrationStatus"
    /**
     * If "AutoOffEnable" sends with param "true", accessory will shutdown automatically when it is in idle about 5 minutes.
     * if "false", accessory will not.
     * UI uses toggle button, "AMBIENT_AWARE" means "true", "TALK_THRU" means "false"
     */
    const val CMD_AutoOffEnable = "AutoOffEnable"
    /**
     * Headphone has voice prompt when using command to send with a param "true"
     * With a param "false", headphone has no other voice.
     */
    const val CMD_VoicePrompt = "VoicePrompt"
    /**
     * 150NC do not support calibration.
     */
    const val CMD_StartAutoCalibration = "StartAutoCalibration"
    /**
     * There is a button on accessory which can be programmable.
     */
    const val CMD_SmartButton = "SmartButton"
    const val CMD_AppParam_AudioEQ = "AppParam_AudioEQ"

    const val CMD_GraphicEqLimits = "GraphicEqLimits"
    const val CMD_GraphicEqPresetBandSettings = "GraphicEqPresetBandSettings"
    /**
     * Set or Get EQ preset
     * Get EQ preset
     * params: preset, band
     * In "receiveResponse" callback, get this value.
     *
     * Set EQ preset
     * params: preset band value
     */
    const val CMD_GrEqBandGains = "GrEqBandGains"

    const val CMD_GraphicEqBandFreq = "GraphicEqBandFreq"
    const val CMD_GraphicEqDefaultPreset = "GraphicEqDefaultPreset"
    /**
     * Current EQ preset.
     * Default EQ preset are Off,Jazz,Vocal,Bass.
     * User can add custom EQ.
     * Use this command to get current preset,one of default EQ presets and custom EQ.
     */
    const val CMD_Geq_Current_Preset = "Geq_Current_Preset"

    const val CMD_GraphicEqFactoryResetPreset = "GraphicEqFactoryResetPreset"
    const val CMD_GraphicEqPersistPreset = "GraphicEqPersistPreset"
    const val CMD_Special1 = "Special1"
    /**
     * The commands special 1-8 are not use in current.
     */
    const val CMD_Special2 = "Special2"
    const val CMD_Special3 = "Special3"
    const val CMD_Special4 = "Special4"
    const val CMD_Special5 = "Special5"
    const val CMD_Special6 = "Special6"
    const val CMD_Special7 = "Special7"
    const val CMD_Special8 = "Special8"
    const val CMD_AmbientLevelingNotification = "AmbientLevelingNotification"
    const val CMD_FWInfo = "FWInfo"

    const val CMD_FIRMWARE_UPDATE_STATE= "FirmwareUpdateState"
    const val CMD_UPDATE_IMAGE = "UpdateImage"

    /**
     * This is for OTA.
     * The address of first flash partition.
     */

    const val address0_Parameter = 0x38000
    const val address0_Firmware = 0x48000
    const val address0_Data = 0x10c000

    /**
     * This is for OTA.
     * The address of second flash partition.
     */

    const val address1_Parameter = 0x21C000
    const val address1_Firmware = 0x22C000
    const val address1_Data = 0x2F0000

}