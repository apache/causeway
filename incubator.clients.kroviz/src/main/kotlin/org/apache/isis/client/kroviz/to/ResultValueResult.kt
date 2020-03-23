package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class ResultValueResult(
        val value: Value? = null,
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null
) : IResult
