package org.ro.view

import com.lightningkite.kotlin.observable.list.ObservableList
import com.lightningkite.kotlin.observable.list.observableListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.browser.localStorage

@Serializable
data class Address(
        val firstName: String? = null,
        val lastName: String? = null,
        val email: String? = null,
        val favourite: Boolean? = false
)

fun Address.match(search: String?): Boolean {
    return search?.let {
        firstName?.contains(it, true) ?: false ||
                lastName?.contains(it, true) ?: false ||
                email?.contains(it, true) ?: false
    } ?: true
}

object Model {

    val addresses: ObservableList<Address> = observableListOf(
            Address("John", "Smith", "john.smith@mail.com", true),
            Address("Karen", "Kowalsky", "kkowalsky@mail.com", true),
            Address("William", "Gordon", "w.gordon@mail.com", false)
    )

    fun storeAddresses() {
        val jsonString = Json.stringify(Address.serializer().list, addresses)
        localStorage["addresses"] = jsonString
    }

    fun loadAddresses() {
        localStorage["addresses"]?.let {
            addresses.clear()
            Json.parse(Address.serializer().list, it).forEach {
                addresses.add(it)
            }
        }
    }
}
