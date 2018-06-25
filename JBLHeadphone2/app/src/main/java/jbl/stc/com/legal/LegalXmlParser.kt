package jbl.stc.com.legal

import android.content.Context
import android.util.Log
import jbl.stc.com.storage.PreferenceKeys
import jbl.stc.com.storage.PreferenceUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.concurrent.thread

/**
 * 1. Download xml from server.
 * 2. Parse xml
 * 3. Version compare,if there is new version and no exist file, will download this file(/data/data/package name/xxx).
 */

object LegalXmlParser {

    private val TAG = LegalXmlParser::class.java.simpleName
    private var mListData = mutableListOf<LegalData>()

    /**
     *  Read legal data structure and then return it through callback.
     */
    fun readLegalData(context: Context, url: String, listener: LegalListener?) { //update ui
        if (mListData.isEmpty()) {
            thread {
                initListData()
                var list = parseXml(url)
                if (list != null) {
                    versionCompare(context, list)
                }
                listener?.onLegalDataReceived(mListData)
            }   
        } else {
            listener?.onLegalDataReceived(mListData)
        }
    }

    private fun initListData() {
        mListData.clear()
        mListData.add(LegalData(LegalConstants.NAME_EULA, LegalConstants.LEGAL_INIT_VER, "", LegalConstants.EULA_FILE))
        mListData.add(LegalData(LegalConstants.NAME_PRIVACY_POLICY, LegalConstants.LEGAL_INIT_VER, "", LegalConstants.PRIVACY_POLICY_FILE))
    }

    private fun parseXml(url: String): List<LegalData>? {
        var list = mutableListOf<LegalData>()
        try {
            val mUrl = URL(url)
            Log.i(TAG, "URL:$mUrl")
            val httpURLConnection = mUrl.openConnection() as HttpURLConnection
            httpURLConnection.connectTimeout = 10 * 1000
            httpURLConnection.connect()
            try {
                val parser = XmlPullParserFactory.newInstance().newPullParser()
                parser.setInput(httpURLConnection.inputStream, null)
                var event = parser.eventType
                val builder = StringBuilder()
                var xmlData = LegalData("", "", "", "")
                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name.equals("Release", ignoreCase = true)) {
                                xmlData = LegalData("", "", "", "")
                                xmlData.name = parser.getAttributeValue(0)
                                xmlData.version = parser.getAttributeValue(1)
                                Log.i(TAG, "START_TAG: name:" + xmlData.name + ", version:" + xmlData.version)
                            }
                            builder.setLength(0)
                        }
                        XmlPullParser.TEXT -> {
                            builder.append(parser.text.trim { it <= ' ' })
                            Log.i(TAG, "TEXT:" + builder.toString())
                        }
                        XmlPullParser.END_TAG -> if (parser.name == "Release") {
                            xmlData.url = builder.toString()
                            list.add(xmlData)
                            Log.i(TAG, "END_TAG:" + builder.toString())
                        }
                    }
                    event = parser.next()
                }
                return list
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(TAG, "Parse xml exception: " + e.message)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i(TAG, "Parse xml IO exception: " + e.message)
        }
        return null
    }

    private fun versionCompare(context: Context, list: List<LegalData>?) {
        if (list == null) {
            Log.i(TAG, "versionCompare list is null ")
            return
        }
        for (item: LegalData in list) {
            when (item.name) {
                LegalConstants.NAME_EULA -> {
                    var eulaVer = PreferenceUtils.getString(LegalConstants.NAME_EULA, context, "")
                    val verChanged = isVerChanged(item.version, eulaVer)
                    Log.i(TAG, "eula version changed =$verChanged")
                    if (verChanged) {
                        PreferenceUtils.setBoolean(PreferenceKeys.TUTORIAL_PERSIST, false, context)
                        PreferenceUtils.setString(LegalConstants.NAME_EULA, item.version, context)
                        saveFile(item.url, context.filesDir.path + File.separator + LegalConstants.EULA_FILE)
                        item.url = context.filesDir.path + File.separator + LegalConstants.EULA_FILE
                    }
                    item.file = LegalConstants.EULA_FILE
                    if (!mListData.contains(item))
                        mListData.add(item)
                    else{
                        mListData.remove(item)
                        mListData.add(item)
                    }

                }
                LegalConstants.NAME_PRIVACY_POLICY -> {
                    var privacyVer = PreferenceUtils.getString(LegalConstants.NAME_PRIVACY_POLICY, context, "")
                    val verChanged = isVerChanged(item.version, privacyVer)
                    Log.i(TAG, "privacy policy version changed =$verChanged")
                    if (verChanged) {
                        PreferenceUtils.setBoolean(PreferenceKeys.TUTORIAL_PERSIST, false, context)
                        PreferenceUtils.setString(LegalConstants.NAME_PRIVACY_POLICY, item.version, context)
                        saveFile(item.url, context.filesDir.path + File.separator + LegalConstants.PRIVACY_POLICY_FILE)
                        item.url = context.filesDir.path + File.separator + LegalConstants.PRIVACY_POLICY_FILE
                    }
                    item.file = LegalConstants.PRIVACY_POLICY_FILE
                    if (!mListData.contains(item))
                        mListData.add(item)
                    else{
                        mListData.remove(item)
                        mListData.add(item)
                    }
                }
            }
        }
    }

    private fun isVerChanged(liveVersion: String, currentVersion: String): Boolean {
        val liveArray = arrayOfNulls<String>(3)
        val currentArray = arrayOfNulls<String>(3)
        var counter = 0
        var st = StringTokenizer(liveVersion, ".")

        while (st.hasMoreTokens()) {
            val x = st.nextToken()
            liveArray[counter++] = x
        }
        counter = 0
        st = StringTokenizer(currentVersion, ".")
        while (st.hasMoreTokens()) {
            val x = st.nextToken()
            currentArray[counter++] = x
        }
        try {
            return when {
                Integer.parseInt(liveArray[0]) > Integer
                        .parseInt(currentArray[0]) -> true
                Integer.parseInt(liveArray[0]) == Integer
                        .parseInt(currentArray[0]) -> // Checking for second index
                    when {
                        Integer.parseInt(liveArray[1]) > Integer
                                .parseInt(currentArray[1]) -> true
                        Integer.parseInt(liveArray[1]) == Integer.parseInt(currentArray[1]) -> // Checking for third Index
                            Integer.parseInt(liveArray[2]) > Integer
                                    .parseInt(currentArray[2])
                        else -> false
                    }
                else -> false
            }

        } catch (e: Exception) {
            Log.i(TAG, "Version compare Exception " + e.message)
            return false
        }
    }

    private fun saveFile(surl: String, strFile: String) {
        Log.i("saveFile =", "$surl, $strFile")
        var connection: HttpURLConnection? = null
        val inputStream: InputStream

        val file = File(strFile)
        val fos: FileOutputStream

        try {
            val url = URL(surl)
            connection = url.openConnection() as HttpURLConnection

            val code = connection.responseCode
            if (HttpURLConnection.HTTP_OK == code) {
                connection.connect()
                inputStream = connection.inputStream
                fos = FileOutputStream(file)

                var i: Int = inputStream.read()
                while (i != -1) {
                    fos.write(i)
                    i = inputStream.read()
                }

                inputStream.close()
                fos.close()
                Log.i(TAG, "saveFile success")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i(TAG, "save file IO Exception " + e.message)
        } finally {
            if (connection != null) {
                connection.disconnect()
            }
        }
    }
}
