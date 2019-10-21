package org.ro.core

import org.ro.org.ro.ui.kv.UiManager

/**
 * Keep track of connected server.
 */
//TODO convert to class in order to have multiple sessions in parallel
object Session {
    private var user: String = ""
    private var pw: String = ""
    var url: String = ""

    fun login(url: String, user: String, pw: String) {
        this.user = user
        this.pw = pw
        this.url = url
        UiManager.updateUser(user)
//TODO        UiManager.updatePower("Powered By: Apache Isis")
    }

    fun getCredentials(): String {
        return "$user:$pw".base64encoded
    }

    /**
     * https://discuss.kotlinlang.org/t/kotlin-native-base64-en-decoder-code/10043
     */
    private const val BASE64_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    /**
     * Base64 encode a string.
     */
    val String.base64encoded: String
        get() {
            val pad = when (this.length % 3) {
                1 -> "=="
                2 -> "="
                else -> ""
            }
            var raw = this
            (1..pad.length).forEach { raw += 0.toChar() }
            return StringBuilder().apply {
                (0 until raw.length step 3).forEach {
                    val n: Int = (0xFF.and(raw[it].toInt()) shl 16) +
                            (0xFF.and(raw[it + 1].toInt()) shl 8) +
                            0xFF.and(raw[it + 2].toInt())
                    listOf<Int>((n shr 18) and 0x3F,
                            (n shr 12) and 0x3F,
                            (n shr 6) and 0x3F,
                            n and 0x3F).forEach { append(BASE64_SET[it]) }
                }
            }.dropLast(pad.length)
                    .toString() + pad
        }

}
