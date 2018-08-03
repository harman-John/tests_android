package jbl.stc.com.legal

import android.content.Context
import com.avnera.smartdigitalheadset.Log
import jbl.stc.com.logger.Logger
import jbl.stc.com.storage.PreferenceKeys
import jbl.stc.com.storage.PreferenceUtils

/**
 * Created by Johngan on 17/04/2018.
 */

object LegalConstants {

    const val LEGAL_INIT_VER = "1.0.0"

    const val EULA_FILE = "EULA_JBLHeadphone_Android.txt"
    const val PRIVACY_POLICY_FILE = "PrivacyPolicy.txt"
    const val OPEN_SOURCE_FILE = "OpenSource.txt"

    const val NAME_EULA = "EULA_JBLHeadphone"
    const val NAME_PRIVACY_POLICY = "PrivacyPolicy"

    private const val LEGAL_URL_TEST = "http://storage.harman.com/Testing/LegalFiles/Android/Legal_Resource_Test_Index.xml"
    private const val LEGAL_URL_PRODUCT = "http://storage.harman.com/LegalFiles/Android/Legal_Resource_Index.xml"

    fun getLegalUrl(context: Context): String {
        var isTestUrl = PreferenceUtils.getBoolean(PreferenceKeys.LEGAL_TEST_URL, context)
        Logger.d("LegalConstants","LegalConstants = $isTestUrl")
        return if (isTestUrl) {
            LEGAL_URL_TEST
        } else {
            LEGAL_URL_PRODUCT
        }
    }
}