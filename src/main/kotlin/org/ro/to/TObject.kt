package org.ro.to

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import org.ro.core.model.Adaptable
import pl.treksoft.kvision.utils.Object

//FIXME dynamic 
class TObject(jsonObj: JsonObject? = null) : TitledTO(jsonObj), Adaptable {
    private var domainType: String = ""
    private var instanceId: Int = 0

    init {
        if (jsonObj != null) {
            domainType = jsonObj["domainType"].toString()
            instanceId = jsonObj["instanceId"].int
        }
    }

    fun getId(): String {
        return "$domainType/$instanceId"
    }

    fun getProperties(): MutableList<Invokeable> {
        val result = mutableListOf<Invokeable>()
        for (m in memberList) {
            if ((m as Member).memberType == Member().PROPERTY) {
                result.add(m)
            }
        }
        return result
    }

    /**
     * Post-Constructor fun using dynamic nature of class.
     */
    fun addMembersAsProperties(): Unit {
        val members: MutableList<Invokeable> = getProperties()
        for (m in members) {
            if ((m as Member).memberType == Member().PROPERTY) {
                addAsProperty(this, m)
            }
        }
    }

    fun addAsProperty(dynObj: TObject, property: Member) {
        val attribute: Object? = null
        val value: Any? = property.getValue()
        if (value != null) {
            val typeSpec: Any? = property.memberType
            //FIXME attribute = typeSpec(value)
        }
        // if value={} (ie. of class Object), 
        // it is represented as [object Object] 
        if (value == "[object Object]") {
            //FIXME var link = Link(value)
            //here the magic of recursive OA's take place
            //FIXME attribute = ObjectAdapter(link, link.title, "Link")
        }
        val key: String = property.id
        //FIXME dynObj[key] = attribute
    }

}

