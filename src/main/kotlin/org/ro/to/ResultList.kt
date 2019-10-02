package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
//TODO structure of json is changed >= 16.2
data class ResultList(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.LIST.type,
        val result: ResultListResult? = null
) : TransferObject
