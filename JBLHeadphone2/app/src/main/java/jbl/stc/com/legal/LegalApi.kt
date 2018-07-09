package jbl.stc.com.legal

import android.content.Context
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.support.v4.app.FragmentActivity
import jbl.stc.com.R
import jbl.stc.com.constant.JBLConstant
import jbl.stc.com.fragment.LegalFragment
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

    fun showOpenSource(context: FragmentActivity, isSplash: Boolean) {
        showLegalDialog(context, R.string.open_source, LegalConstants.OPEN_SOURCE_FILE, AnalyticsManager.SCREEN_OPEN_SOURCE,isSplash)
    }

    fun showEula(context: FragmentActivity,isSplash: Boolean) {
        if (isConnectionAvailable(context)) {
            LegalXmlParser.readLegalData(context, LegalConstants.getLegalUrl(context), object : LegalListener {
                override fun onLegalDataReceived(list: List<LegalData>) {
                    for (item in list) {
                        if (item.name.equals(LegalConstants.NAME_EULA, ignoreCase = true)) {
                            showLegalDialog(context, R.string.agree_to_eula, item.file, AnalyticsManager.SCREEN_EULA,isSplash)
                        }
                    }
                }
            })
        } else {
            showLegalDialog(context, R.string.eula, LegalConstants.EULA_FILE, AnalyticsManager.SCREEN_EULA,isSplash)
        }
    }

    fun showPrivacyPolicy(context: FragmentActivity,isSplash: Boolean) {
        if (isConnectionAvailable(context)) {
            LegalXmlParser.readLegalData(context, LegalConstants.getLegalUrl(context), object : LegalListener {
                override fun onLegalDataReceived(list: List<LegalData>) {
                    for (item in list) {
                        if (item.name.equals(LegalConstants.NAME_PRIVACY_POLICY, ignoreCase = true))
                            showLegalDialog(context, R.string.harman_privacy_policy, item.file, AnalyticsManager.SCREEN_EULA,isSplash)
                    }
                }
            })
        } else {
            showLegalDialog(context, R.string.harman_privacy_policy, LegalConstants.PRIVACY_POLICY_FILE, AnalyticsManager.SCREEN_EULA,isSplash)
        }
    }

    private fun showLegalDialog(activity: FragmentActivity, titleId: Int, file: String, screenName: String,isSplash: Boolean) {
        var legalFragment = LegalFragment()
        legalFragment.setTitle(titleId)
        legalFragment.setFile(file)
        legalFragment.setScreenName(screenName)
        switchFragment(activity,legalFragment,JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT,isSplash)
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

    fun switchFragment(activity: FragmentActivity, baseFragment: LegalFragment, type: Int, isSplash: Boolean) {
        try {
            val ft = activity.supportFragmentManager.beginTransaction()
            if (type == JBLConstant.SLIDE_FROM_DOWN_TO_TOP) {
                ft.setCustomAnimations(R.anim.enter_from_down, R.anim.exit_to_up, R.anim.enter_from_up, R.anim.exit_to_down)
            } else if (type == JBLConstant.SLIDE_FROM_LEFT_TO_RIGHT) {
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
            } else if (type == JBLConstant.SLIDE_FROM_RIGHT_TO_LEFT) {
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            }
            if (isSplash) {
                if (activity.supportFragmentManager.findFragmentById(R.id.relative_layout_splash) == null) {
                    ft.add(R.id.relative_layout_splash, baseFragment)
                } else {
                    ft.replace(R.id.relative_layout_splash, baseFragment, baseFragment.tag)
                }
            }else{
                if (activity.supportFragmentManager.findFragmentById(R.id.containerLayout) == null) {
                    ft.add(R.id.containerLayout, baseFragment)
                } else {
                    ft.replace(R.id.containerLayout, baseFragment, baseFragment.tag)
                }
            }
            ft.addToBackStack(null)
            ft.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}