package jbl.stc.com.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jbl.stc.com.R
import jbl.stc.com.R.*
import jbl.stc.com.legal.LegalConstants
import jbl.stc.com.manager.AnalyticsManager
import kotlinx.android.synthetic.main.fragment_legal.view.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

class LegalFragment : Fragment(), View.OnClickListener {
    private var mFile = LegalConstants.EULA_FILE
    private var mTitleResId = string.eula_title
    private var mScreenName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layout.fragment_legal,
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.agree_btn -> activity.onBackPressed()
            R.id.back_btn -> activity.onBackPressed()
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
        val TAG = LegalFragment::class.java.simpleName!!
    }
}
