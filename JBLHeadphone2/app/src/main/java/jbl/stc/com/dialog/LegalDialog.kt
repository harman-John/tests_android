package jbl.stc.com.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import jbl.stc.com.R
import jbl.stc.com.R.*
import jbl.stc.com.legal.LegalConstants
import jbl.stc.com.manager.AnalyticsManager
import kotlinx.android.synthetic.main.dialog_legal.view.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

class LegalDialog : android.support.v4.app.DialogFragment(), View.OnClickListener {
    private var mFile = LegalConstants.EULA_FILE
    private var mTitleResId = string.eula_title
    private var mScreenName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layout.dialog_legal,
                container, false)
        view.text_title.setText(mTitleResId)
        view.back_btn.setOnClickListener(this)

        var content: String? = null
        try {
            content = readFile(context.openFileInput(mFile))
        } catch (e: Exception) {
            Log.e("LegalDialog", e.message)
        }
        if (content == null) {
            Log.e("LegalDialog", "Read legal content from assets $mFile")
            content = readFile(activity.assets.open(mFile))
        }else{
            Log.e("LegalDialog", "Read legal content from data/data/package name/$mFile")
        }
        view.text_view.text = content
        return view
    }

    private fun readFile(inputStream: InputStream): String {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var len: Int = inputStream.read(buf)
        while (len != -1) {
            outputStream.write(buf, 0, len)
            len = inputStream.read(buf)
        }
        outputStream.close()
        inputStream.close()
        return outputStream.toString()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsManager.getInstance(activity).setScreenName(mScreenName)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar)
        val d = ColorDrawable(ContextCompat.getColor(context,R.color.dialogBackGround))
        dialog.window.setBackgroundDrawable(d)
        dialog.window.attributes.windowAnimations = style.DialogAnimation
        val params = dialog.window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.gravity = Gravity.CENTER
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.agree_btn -> dismissAllowingStateLoss()
            R.id.back_btn -> dismissAllowingStateLoss()
        }
    }

    fun setFile(file: String) {
        mFile = file
    }

    fun setTitle(resId: Int) {
        mTitleResId = resId
    }

    fun setScreenName(screenName: String) {
        mScreenName = screenName
    }

    companion object {
        val TAG = LegalDialog::class.java.simpleName!!
    }
}
