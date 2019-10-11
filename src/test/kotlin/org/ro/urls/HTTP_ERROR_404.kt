package org.ro.urls

object HTTP_ERROR_404 : Response() {
    override val url = ""
    override val str = """{
    "httpStatusCode": 404,
    "message": "could not determine adapter for OID: 'simple.SimpleObject:86'",
    "detail": null
}
"""
}
