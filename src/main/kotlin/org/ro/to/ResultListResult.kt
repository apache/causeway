package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class ResultListResult(
        val value: List<Link> = emptyList(),
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null
) : IResult
