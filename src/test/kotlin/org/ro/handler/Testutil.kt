package org.ro.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ro.core.Session
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.ACTIONS_RUN_FIXTURE_SCRIPT
import org.w3c.xhr.XMLHttpRequest

class TestUtil() {

    //Most Handler Tests are IntegrationTests
    fun isSimpleAppAvailable(): Boolean {
        val xhr = XMLHttpRequest();
        val url = "http://sven:pass@localhost:8080/restful/"
        login()
        val credentials: String = Session.getCredentials()
        xhr.open("GET", url, false, "sven", "pass");
        xhr.setRequestHeader("Authorization", "Basic $credentials")
        xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
        xhr.setRequestHeader("Accept", "application/json")

        try {
            xhr.send(); // there will be a 'pause' here until the response to come.
        } catch (e: Throwable) {
            return false
        }
        console.log("[$url status: ${xhr.status}]")

        val answer = xhr.status.equals(200)

        return answer
    }

    fun login() {
        Session.login("http://localhost:8080/restful/", "sven", "pass")
    }
    
    fun wait(milliseconds: Long)  {
        GlobalScope.launch {
            delay(milliseconds)
            console.log("[TestUtil.wait] $milliseconds")
        }
    }
    
}