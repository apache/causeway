package org.ro.bs3.to

//@XmlType(name = "position", namespace = "http://isis.apache.org/applib/layout/component")
enum class Position {

    BELOW,
    RIGHT,
    PANEL,
    PANEL_DROPDOWN;

    fun value(): String {
        return "name()"
    }

    companion object {

        fun fromValue(v: String): Position {
            return valueOf(v)
        }
    }

}
