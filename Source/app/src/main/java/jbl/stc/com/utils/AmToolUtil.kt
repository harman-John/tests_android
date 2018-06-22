package jbl.stc.com.utils

import android.content.Context
import android.content.res.AssetManager
import java.io.IOException
import java.io.InputStream

/**
 * Created by Johngan on 20/04/2018.
 */

object AmToolUtil {
    private const val TAG = "ToolUtil"
    const val COMMAND_FILE = "commands.json"
    fun readAssertResource(context: Context, strAssertFileName: String ): ByteArray {
        val assetManager: AssetManager = context.assets
        var bytes: ByteArray = byteArrayOf()
        try {
            val ims: InputStream = assetManager.open(strAssertFileName)
            bytes = ims.readBytes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes
    }

    fun transferCurrentVersion(version: String): String {
        val tempString = version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return String.format("%02x%02x%02x", Integer.valueOf(tempString[0]), Integer.valueOf(tempString[1]), Integer.valueOf(tempString[2]))
    }
}