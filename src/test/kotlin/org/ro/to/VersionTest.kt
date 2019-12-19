package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.VersionHandler
import org.ro.to.Version
import org.ro.snapshots.ai1_16_0.RESTFUL_VERSION
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
