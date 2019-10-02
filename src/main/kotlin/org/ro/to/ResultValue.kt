package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class ResultValue(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.SCALARVALUE.type,
        val result: ResultValueResult? = null
) : TransferObject
