package org.apache.isis.client.kroviz.core.event

import kotlinx.browser.document
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.w3c.dom.Document
import org.w3c.dom.HTMLIFrameElement

// https://stackoverflow.com/questions/33143776/ajax-request-refused-to-set-unsafe-header
class CorsHttpRequest {
    private val scriptStr = """
    function sendWithoutOrigin(url, credentials) {
            console.log("[CHR.script]");
            var request = new XMLHttpRequest();
            request.open('GET', url);
            request.setRequestHeader('Authorization', 'Basic '+ credentials)
            request.onreadystatechange = function() {
                console.log(request.readyState);
                if (request.readyState === XMLHttpRequest.DONE) {
                    if (request.status === 200) {
                        console.log('GET succeeded.');
                        console.log(request.responseText);
                        return request.responseText;
                    }
                    else {
                        console.log(request);
                        console.warn('GET failed.');
                    }
                }
            }
            request.send();
        }"""
    private var iframe: HTMLIFrameElement = document.getElementById("iframe") as HTMLIFrameElement
    private var iframeWin: Document? = iframe.contentDocument //?: iframe) as Document?

    init {
        val iframeDoc = iframe.contentDocument //?: iframeWin?.ownerDocument
        val script = iframeDoc?.createElement("SCRIPT")
        script?.append(scriptStr);
        iframeDoc?.documentElement?.appendChild(script!!);
    }

    fun invoke(url: String, credentials: String): String {
        val answer = js("""
            var iframe = document.getElementById('iframe'); 
            var iframeWin = iframe.contentWindow;
            return iframeWin.sendWithoutOrigin(url, credentials);
            """)
        console.log("[CHR]")
        console.log(answer as String)
        return answer
    }

}