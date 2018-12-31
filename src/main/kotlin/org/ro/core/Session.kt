package org.ro.core

import kotlinx.io.ByteArrayOutputStream
import kotlinx.serialization.toUtf8Bytes

/**
 * - keeps track of connected server,
 */
object Session {
    private var user: String = ""
    private var pw: String = ""
    var url: String = ""
    
    fun login(url: String, user: String, pw: String) {
        this.user = user
        this.pw = pw
        this.url = url
        Globals.view?.statusBar?.user?.title = user
    }

    fun getCredentials(): String {
        var credentials = "$user : $pw"
        val ba = credentials.toUtf8Bytes().encodeBase64()
        credentials = ba.toString()
        return credentials
    }

    /**
     * https://gist.github.com/hrules6872/e2d4d02a1e8d3c6328ae5aeabc430b96#file-base64-kt
     */
    private fun ByteArray.encodeBase64(): ByteArray {
        val table = (CharRange('A', 'Z') + CharRange('a', 'z') + CharRange('0', '9') + '+' + '/').toCharArray()
        val output = ByteArrayOutputStream()
        var padding = 0
        var position = 0
        while (position < this.size) {
            var b = this[position].toInt() and 0xFF shl 16 and 0xFFFFFF
            if (position + 1 < this.size) b = b or (this[position + 1].toInt() and 0xFF shl 8) else padding++
            if (position + 2 < this.size) b = b or (this[position + 2].toInt() and 0xFF) else padding++
            for (i in 0 until 4 - padding) {
                val c = b and 0xFC0000 shr 18
                output.write(table[c].toInt())
                b = b shl 6
            }
            position += 3
        }
        for (i in 0 until padding) {
            output.write('='.toInt())
        }
        return output.toByteArray()
    }

}