package org.apache.causeway.client.kroviz.core.event

import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.HTMLIFrameElement

// https://stackoverflow.com/questions/33143776/ajax-request-refused-to-set-unsafe-header
class CorsHttpRequest {
    private val scriptStr = """
    function sendWithoutOrigin(url, credentials) {
            console.log("[CHR.script]");
            var xhr = new XMLHttpRequest();
            xhr.open('GET', url);
            xhr.withCredentials = true;
            xhr.setRequestHeader('Authorization', 'Basic '+ credentials);
            xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8')
            xhr.setRequestHeader('Accept', 'image/svg+xml');
            xhr.responseType = 'blob';
            xhr.onloadend = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    console.log('GET succeeded.');
                    console.log(xhr.response);
                    return xhr.response;
                }            
            };
            xhr.send();
        }"""
    private var iframe: HTMLIFrameElement = document.getElementById("iframe") as HTMLIFrameElement
    private var iframeWin: Document? = iframe.contentDocument //?: iframe) as Document?

    init {
        val iframeDoc = iframe.contentDocument!! //?: iframeWin?.ownerDocument
        val script = iframeDoc.createElement("SCRIPT")
        script.append(scriptStr);
        iframeDoc.documentElement?.appendChild(script);
    }

    //https://stackoverflow.com/questions/3076414/ways-to-circumvent-the-same-origin-policy
/*      iframeDoc.domain = "about:blank"
        val newDoc = Document();
        newDoc.domain = "about:blank"
        iframeDoc.append(newDoc)
        val script = newDoc.createElement("SCRIPT")
        newDoc.documentElement?.appendChild(script);
*/
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
