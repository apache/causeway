package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class ResultList(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.LIST.type,
        val result: ResultListResult? = null
) : TransferObject
