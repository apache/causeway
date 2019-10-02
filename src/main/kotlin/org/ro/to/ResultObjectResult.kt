package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class ResultObjectResult(
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null,
        val title: String = "",
        val domainType: String = "",
        val instanceId: Int,
        val members: Map<String, Member> = emptyMap()
) : IResult
