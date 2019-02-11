package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.core.model.Adaptable
import pl.treksoft.kvision.utils.Object

@Serializable
data class TObject(val extensions: Extensions,
                   val members: List<Member> = emptyList(),
                   val links: List<Link> = emptyList(),
                   val title: String = "",
                   val domainType: String = "",
                   val instanceId: Int = 0) : Adaptable {

    fun getId(): String {
        return "$domainType/$instanceId"
    }

    fun getLayoutLink(): Link? {
        var href: String?
        for (l in links) {
            href = l.href
            //TODO can be "object-layout" >= 1.16
            if (href.isNotEmpty()) {
                if (href.endsWith("layout")) {
                    return l
                }
            }
        }
        return null
    }

    fun getProperties(): MutableList<Member> {
        val result = mutableListOf<Member>()
        for (m in members!!) {
            if (m.memberType == MemberType.PROPERTY.type) {
                result.add(m)
            }
        }
        return result
    }

    /**
     * Post-Constructor fun using dynamic nature of class.
     */
    fun addMembersAsProperties(): Unit {
        val members: MutableList<Member> = getProperties()
        for (m in members) {
            if (m.memberType == MemberType.PROPERTY.type) {
                addAsProperty(this, m)
            }
        }
    }

    fun addAsProperty(dynObj: TObject, property: Member) {
        val attribute: Object? = null
        val value: Any? = property.value
        if (value != null) {
          //  val typeSpec: Any? = property.memberType
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