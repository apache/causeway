package org.apache.isis.client.kroviz.utils

import org.w3c.xhr.XMLHttpRequest

/* see: https://stackoverflow.com/questions/13709482/how-to-read-text-file-in-javascript */
class FileHelper {

    fun readStringFromFileAtPath(pathOfFileToReadFrom: String): String {
        val request = XMLHttpRequest();
        request.open("GET", pathOfFileToReadFrom, false)
        request.send(null)
        return request.responseText
    }

}
