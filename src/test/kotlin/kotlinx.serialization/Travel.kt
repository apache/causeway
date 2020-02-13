package kotlinx.serialization

@Serializable
data class Travel(
        val route: MutableList<Route> = mutableListOf<Route>()) {
}

@Serializable
data class Route(
        val location: MutableList<Location> = mutableListOf<Location>(),
        val role:String?) {
}

@Serializable
data class Location(
        val latitude: Double,
        val longitude: Double
)
