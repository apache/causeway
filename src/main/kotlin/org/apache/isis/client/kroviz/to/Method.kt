package org.apache.isis.client.kroviz.to

enum class Method(val operation: String) {
    GET("GET"),
    PUT("PUT"),
    POST("POST")
//    DELETE("DELETE")  not used - Apache Isis defines delete operations on DomainObjects
}
