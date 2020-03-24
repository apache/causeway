package org.apache.isis.client.kroviz.to

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.handler.VersionHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.RESTFUL_VERSION
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class VersionTest {

    @Test
    fun testParse() {
        val version = VersionHandler().parse(RESTFUL_VERSION.str) as Version
        assertEquals("1.0.0", version.specVersion)
        assertEquals("formal", version.optionalCapabilities["domainModel"])
    }

}
