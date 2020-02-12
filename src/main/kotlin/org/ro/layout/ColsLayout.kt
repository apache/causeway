package org.ro.layout

import kotlinx.serialization.Serializable

@Serializable
data class ColsLayout(val col: MutableList<ColLayout> = mutableListOf<ColLayout>())

