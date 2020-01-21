package org.ro.snapshots.simpleapp1_16_0

import org.ro.snapshots.Response

object HTTP_ERROR_404 : Response() {
    override val url = ""
    override val str = """{
    "httpStatusCode": 404,
    "message": "could not determine adapter for OID: 'simple.SimpleObject:86'",
    "detail": null
}
"""
}
