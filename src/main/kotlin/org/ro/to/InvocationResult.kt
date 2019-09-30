package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
//TODO structure of json is changed >= 16.2
data class InvocationResult(val links: List<Link> = emptyList(),
                            val resulttype: String = ResultType.LIST.type,
                            val result: Result? = null
) : TransferObject
