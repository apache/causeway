package kotlinx.serialization

@Serializable
data class Sample(val sample: List<Outer>)

@Serializable
data class Outer(val inner: Inner)

@Serializable
data class Inner(val value: String)
