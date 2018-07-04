package jbl.stc.com.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import jbl.stc.com.R
import jbl.stc.com.legal.LegalApi
import jbl.stc.com.listener.DismissListener
import jbl.stc.com.storage.PreferenceKeys
import jbl.stc.com.storage.PreferenceUtils
import jbl.stc.com.utils.AppUtils

import kotlinx.android.synthetic.main.dialog_legal_landing.view.*

class LegalLandingDialog : android.support.v4.app.DialogFragment() {
    private var mDismissListener: DismissListener? = null

    private var isBothChecked = false

    fun setOnDismissListener(dismissListener: DismissListener) {
        mDismissListener = dismissListener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mDismissListener?.onDismiss(0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_legal_landing, container, false)
        view.background.alpha = 0xCC
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
            LegalApi.showEula(activity)
        }
        // link text privacy
        view.textPrivacyLink.movementMethod = LinkMovementMethod.getInstance()
        view.textPrivacyLink.highlightColor = Color.TRANSPARENT
        view.textPrivacyLink.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        view.textPrivacyLink.setOnClickListener{
            LegalApi.showPrivacyPolicy(activity)
        }
        // text start button
        view.txtEulaButton!!.setOnClickListener {
            if (isBothChecked) {
                AppUtils.mLegalPage = false
                PreferenceUtils.setBoolean(PreferenceKeys.FIRST_TIME_ENTER_APP, true, activity)
                dismiss()
            }
        }
        this.dialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar)
        val d = ColorDrawable(ContextCompat.getColor(context,R.color.dialogBackGround))
        dialog.window.setBackgroundDrawable(d)
        dialog.window.attributes.windowAnimations = R.style.DialogAnimation
        val params = dialog.window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.gravity = Gravity.CENTER
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    companion object {
        val TAG = LegalLandingDialog::class.java.simpleName!!
    }
}
