package org.ro.ui.table

import org.ro.core.model.Exposer
import org.ro.to.Extensions
import org.ro.to.Member
import org.ro.to.MemberType
import org.ro.to.TObject
import pl.treksoft.kvision.utils.ObservableList
import pl.treksoft.kvision.utils.observableListOf

class TableFactoryTest {

    //TODO how can this be tested?
    fun testData(): ObservableList<Exposer> {
        val answer = observableListOf<Exposer>()
        val m1 = Member(id = "m1", memberType = MemberType.PROPERTY.type)
        val m2 = Member(id = "m2", memberType = MemberType.PROPERTY.type)
        val map = mapOf("m1" to m1, "m2" to m2)
        val t1 = TObject(extensions = Extensions(), members = map)
        val o1 = Exposer(t1)
        val d1 = o1.dynamise()
        d1["var1"] = "string1"
        d1["var2"] = 1
        answer.add(d1)

        val t2 = TObject(extensions = Extensions())
        val o2 = Exposer(t2)
        val d2 = o2.asDynamic()
        d2["var1"] = "string2"
        d2["var2"] = 2
        answer.add(d2)

        return answer
    }

    fun testMap() : Map<String, String>    {
        val map = mapOf<String,String>(
                "Col_0" to "iconName",
                "Col 1" to "var1",
                "Col 2" to "var2",
                "Col 3" to "m1",
                "Col 4" to "m2"
        )
        return map
    }
}
