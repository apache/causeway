package org.ro.handler

import org.w3c.xhr.XMLHttpRequest

class TestUtil() {

    //Most Handler Tests are IntegrationTests
    fun isSimpleAppAvailable(): Boolean {
        val xhr = XMLHttpRequest();
        val url = "http://localhost:8080/restful/"
        xhr.open("GET", url, false, "sven", "pass");
        // xhr.setRequestHeader("Authorization", "Basic $credentials")
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


}