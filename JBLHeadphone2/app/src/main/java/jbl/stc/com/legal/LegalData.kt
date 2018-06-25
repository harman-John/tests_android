package jbl.stc.com.legal

/**
 * Created by Johngan on 17/04/2018.
 */
data class LegalData(var name: String, var version:String, var url: String, var file: String){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        val value = other as LegalData
        if (name != value.name) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + file.hashCode()
        return result
    }
}
