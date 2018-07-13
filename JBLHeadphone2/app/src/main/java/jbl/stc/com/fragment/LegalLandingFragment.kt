package jbl.stc.com.fragment

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jbl.stc.com.R
import jbl.stc.com.legal.LegalApi
import jbl.stc.com.listener.DismissListener
import jbl.stc.com.storage.PreferenceKeys
import jbl.stc.com.storage.PreferenceUtils

import kotlinx.android.synthetic.main.fragment_legal_landing.view.*

class LegalLandingFragment : Fragment() {
    private var mDismissListener: DismissListener? = null

    private var isBothChecked = false

    fun setOnDismissListener(dismissListener: DismissListener) {
        mDismissListener = dismissListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_legal_landing, container, false)
//        view.background.alpha = 0xCC
        // link checkbox eula
        view.checkboxEula!!.setOnCheckedChangeListener { _, isChecked ->
            if (view.checkboxPrivacy.isChecked && isChecked) {
//                view.txtEulaButton.setTextColor(ContextCompat.getColor(activity,R.color.orange_discovery))
//                view.txtEulaButton.background = ContextCompat.getDrawable(context,R.drawable.rectangle_with_round_corner)
                isBothChecked = true
                view.txtEulaButton.alpha = 1.0F
            } else {
                view.txtEulaButton.alpha = 0.7F
//                view.txtEulaButton.setTextColor(ContextCompat.getColor(context,R.color.light_white))
//                view.txtEulaButton.background = ContextCompat.getDrawable(context,R.drawable.rectangle_with_round_corner_grey)
                isBothChecked = false
            }
        }
        view.ll_check_eula!!.setOnClickListener {
            view.checkboxEula.isChecked = !view.checkboxEula.isChecked
        }
        // link checkbox privacy
        view.checkboxPrivacy.setOnCheckedChangeListener { _, isChecked ->
            if (view.checkboxEula.isChecked && isChecked) {
//                view.txtEulaButton.setTextColor(ContextCompat.getColor(activity,R.color.orange_discovery))
//                view.txtEulaButton.background = ContextCompat.getDrawable(context,R.drawable.rectangle_with_round_corner)
                view.txtEulaButton.alpha = 1.0F
                isBothChecked = true
            } else {
                view.txtEulaButton.alpha = 0.7F
//                view.txtEulaButton.setTextColor(ContextCompat.getColor(context,R.color.light_white))
//                view.txtEulaButton.background = ContextCompat.getDrawable(context,R.drawable.rectangle_with_round_corner_grey)
                isBothChecked = false
            }
        }
        view.ll_check_privacy!!.setOnClickListener {
            view.checkboxPrivacy!!.isChecked = !view.checkboxPrivacy!!.isChecked
        }
        // link text eula
        view.textEulaLink.movementMethod = LinkMovementMethod.getInstance()
        view.textEulaLink.highlightColor = Color.TRANSPARENT
        view.textEulaLink.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        view.textEulaLink.setOnClickListener{
            LegalApi.showEula(activity,true)
        }
        // link text privacy
        view.textPrivacyLink.movementMethod = LinkMovementMethod.getInstance()
        view.textPrivacyLink.highlightColor = Color.TRANSPARENT
        view.textPrivacyLink.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        view.textPrivacyLink.setOnClickListener{
            LegalApi.showPrivacyPolicy(activity,true)
        }
        // text start button
        view.txtEulaButton!!.setOnClickListener {
            if (isBothChecked) {
                PreferenceUtils.setBoolean(PreferenceKeys.LEGAL_PERSIST, true, activity)
                mDismissListener?.onDismiss(0)
            }
        }
        return view
    }

    companion object {
        val TAG = LegalLandingFragment::class.java.simpleName!!
    }
}
