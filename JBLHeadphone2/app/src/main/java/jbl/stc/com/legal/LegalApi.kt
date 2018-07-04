package jbl.stc.com.legal

import android.content.Context
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import jbl.stc.com.R
import jbl.stc.com.dialog.LegalDialog
import jbl.stc.com.manager.AnalyticsManager

/**
 * Created by Johngan on 17/04/2018.
 */
object LegalApi {
    fun eulaInit(context: Context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getString(LegalConstants.NAME_EULA, null) == null) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(LegalConstants.NAME_EULA, LegalConstants.LEGAL_INIT_VER).apply()
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getString(LegalConstants.NAME_PRIVACY_POLICY, null) == null) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(LegalConstants.NAME_PRIVACY_POLICY, LegalConstants.LEGAL_INIT_VER).apply();
        }
        LegalXmlParser.readLegalData(context, LegalConstants.getLegalUrl(context), null)
    }

    fun showOpenSource(context: FragmentActivity) {
        showLegalDialog(context, R.string.open_source, LegalConstants.OPEN_SOURCE_FILE, AnalyticsManager.SCREEN_OPEN_SOURCE)
    }

    fun showEula(context: FragmentActivity) {
        if (isConnectionAvailable(context)) {
            LegalXmlParser.readLegalData(context, LegalConstants.getLegalUrl(context), object : LegalListener {
                override fun onLegalDataReceived(list: List<LegalData>) {
                    for (item in list) {
                        if (item.name.equals(LegalConstants.NAME_EULA, ignoreCase = true)) {
                            showLegalDialog(context, R.string.agree_to_eula, item.file, AnalyticsManager.SCREEN_EULA)
                        }
                    }
                }
            })
        } else {
            showLegalDialog(context, R.string.eula, LegalConstants.EULA_FILE, AnalyticsManager.SCREEN_EULA)
        }
    }

    fun showPrivacyPolicy(context: FragmentActivity) {
        if (isConnectionAvailable(context)) {
            LegalXmlParser.readLegalData(context, LegalConstants.getLegalUrl(context), object : LegalListener {
                override fun onLegalDataReceived(list: List<LegalData>) {
                    for (item in list) {
                        if (item.name.equals(LegalConstants.NAME_PRIVACY_POLICY, ignoreCase = true))
                            showLegalDialog(context, R.string.harman_privacy_policy, item.file, AnalyticsManager.SCREEN_EULA)
                    }
                }
            })
        } else {
            showLegalDialog(context, R.string.harman_privacy_policy, LegalConstants.PRIVACY_POLICY_FILE, AnalyticsManager.SCREEN_EULA)
        }
    }

    private fun showLegalDialog(activity: FragmentActivity, titleId: Int, file: String, screenName: String) {
        var legalDialog: Fragment? = activity.supportFragmentManager.findFragmentByTag(LegalDialog.TAG)
        if (legalDialog != null && (legalDialog as LegalDialog).dialog != null)
            return
        legalDialog = LegalDialog()
        legalDialog.setTitle(titleId)
        legalDialog.setFile(file)
        legalDialog.setScreenName(screenName)
        legalDialog.show(activity.supportFragmentManager, LegalDialog.TAG)
    }

    private fun isConnectionAvailable(context: Context): Boolean {
        return try {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }
}