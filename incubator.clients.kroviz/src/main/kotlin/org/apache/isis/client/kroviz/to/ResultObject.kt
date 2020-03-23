package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class ResultObject(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.DOMAINOBJECT.type,
        val result: ResultObjectResult? = null
) : IResult
