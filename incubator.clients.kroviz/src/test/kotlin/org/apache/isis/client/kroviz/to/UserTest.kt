package org.apache.isis.client.kroviz.to


import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.handler.UserHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.RESTFUL_USER
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class UserTest {

    @Test
    fun testParse() {
        val user = UserHandler().parse(RESTFUL_USER.str) as User
        assertEquals("sven", user.userName)
    }
}
