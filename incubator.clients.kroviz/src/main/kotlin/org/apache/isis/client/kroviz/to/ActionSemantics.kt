package org.apache.isis.client.kroviz.to

enum class ActionSemantics(val type: String) {
    IDEMPOTENT("idempotent"),
    NON_IDEMPOTENT("nonIdempotent"),
    NON_IDEMPOTENT_ARE_YOU_SURE("nonIdempotentAreYouSure")
}
