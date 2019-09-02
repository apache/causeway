package org.ro.core.model

import kotlinx.serialization.UnstableDefault
import org.ro.handler.TObjectHandler
import org.ro.to.TObject
import org.ro.to.Value
import org.ro.urls.CFG_1
import org.ro.urls.SO_0
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ExposerTest {
    /*
    isis.reflector.facet.cssClassFa.patterns
new.*:fa-plus,add.*:fa-plus-square,create.*:fa-plus,update.*:fa-edit,delete.*:fa-trash,save.*:fa-floppy-o,change.*:fa-edit,edit.*:fa-pencil-square-o,maintain.*:fa-edit,remove.*:fa-minus-square,copy.*:fa-copy,move.*:fa-exchange,first.*:fa-star,find.*:fa-search,lookup.*:fa-search,search.*:fa-search,view.*:fa-search,clear.*:fa-remove,previous.*:fa-step-backward,next.*:fa-step-forward,list.*:fa-list, all.*:fa-list, download.*:fa-download, upload.*:fa-upload, export.*:fa-download,switch.*:fa-exchange,import.*:fa-upload,execute.*:fa-bolt, run.*:fa-bolt, calculate.*:fa-calculator, verify.*:fa-check-circle, refresh.*:fa-refresh, install.*:fa-wrench,stop.*:fa-stop,terminate.*:fa-stop,cancel.*:fa-stop,discard.*:fa-trash-o,pause.*:fa-pause,suspend.*:fa-pause,resume.*:fa-play,renew.*:fa-repeat,reset.*:fa-repeat,categorise.*:fa-folder-open-o,assign.*:fa-hand-o-right,approve.*:fa-thumbs-o-up,decline.*:fa-thumbs-o-down
     */

    @Test
    fun testConfiguration() {
        val jsonStr = CFG_1.str
        val to = TObjectHandler().parse(jsonStr) as TObject

        val properties = to.getProperties()
        console.log(properties)

        val exposer = Exposer(to).dynamise()

        val actKey = exposer["key"]
        assertEquals("isis.appManifest", actKey)

        val actValue = exposer["value"]
        assertEquals("domainapp.application.manifest.DomainAppAppManifest", actValue)

        val members = to.members
        for (m in members) {
            //TODO iterate over members?
        }
    }

    @Test
    fun testSimpleObject() {
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr) as TObject
        val exposer = Exposer(to)

        //TODO datanucleus will likely be gone with Apache Isis 2.0.x
        val actualDnId = exposer.get("datanucleusIdLong") as Value
        assertEquals(0, actualDnId.content as Int)

        val actualDnvers = exposer.get("datanucleusVersionTimestamp") as Value
        assertEquals(1514897074953L, actualDnvers.content as Long)

        val actualNotes = exposer.get("notes")
        assertEquals(null, actualNotes)

        val dynEx = exposer.dynamise()
        val actKey = dynEx["name"]
        assertEquals("Foo", actKey)

        val actValue = dynEx["notes"]
        assertEquals(null, actValue)

        val members = to.members
        for (m in members) {
        }
    }
}
