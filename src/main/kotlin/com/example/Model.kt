package com.example

import com.lightningkite.kotlin.observable.list.ObservableList
import com.lightningkite.kotlin.observable.list.observableListOf
import kotlinx.serialization.Serializable

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
/*        val jsonString = stringify(Address.serializer().list, addresses)
        localStorage["addresses"] = jsonString */
    }

    fun loadAddresses() {
/*       localStorage["addresses"]?.let {
            addresses.clear()
            JSON.parse(Address.serializer().list, it).forEach {
                addresses.add(it)
            }
        }*/
    }
}
