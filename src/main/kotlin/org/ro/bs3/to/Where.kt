package org.ro.to.bs3

//@XmlType(name = "where", namespace = "http://isis.apache.org/applib/layout/component")

enum class Where {
    EVERYWHERE,
    ANYWHERE,
    OBJECT_FORMS,
    REFERENCES_PARENT,
    PARENTED_TABLES,
    STANDALONE_TABLES,
    ALL_TABLES,
    ALL_EXCEPT_STANDALONE_TABLES,
    NOWHERE,
    NOT_SPECIFIED;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): Where {
            return valueOf(v)
        }
    }

}
