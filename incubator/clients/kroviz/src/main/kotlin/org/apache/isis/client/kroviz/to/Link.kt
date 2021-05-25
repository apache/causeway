/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.utils.Utils

@Serializable
data class Link(val rel: String = "",
                val method: String = Method.GET.operation,
                val href: String,
                val type: String = "",
        //RO SPEC OR ISIS IMPL? can "args" be folded into "arguments"
                val args: Map<String, Argument> = emptyMap(),
        /* arguments can either be:
         * -> empty Map {}
         * -> Map with "value": null (cf. SO_PROPERTY)
         * -> Map with empty key "" (cf. ACTIONS_DOWNLOAD_META_MODEL)
         * -> Map with key,<VALUE> (cf. ACTIONS_RUN_FIXTURE_SCRIPT, ACTIONS_FIND_BY_NAME, ACTIONS_CREATE) */
                val arguments: Map<String, Argument?> = emptyMap(),
                val title: String = "")
    : TransferObject {

    fun argMap(): Map<String, Argument?>? {
        return when {
            arguments.isNotEmpty() -> arguments
            args.isNotEmpty() -> args
            else -> null
        }
    }

    fun setArgument(key: String, value: String?) {
        val k = Utils.enCamel(key)
        val arg = argMap()!!.get(k)
        arg!!.key = k
        arg.value = value
    }

    fun hasArguments(): Boolean {
        return !argMap().isNullOrEmpty()
    }

    fun isProperty(): Boolean {
        return relation() == Relation.PROPERTY
    }

    fun isAction(): Boolean {
        return relation() == Relation.ACTION
    }

    fun name(): String {
        return href.split("/").last()
    }

    fun relation(): Relation {
        val roPrefix = "urn:org.restfulobjects:rels/"
        val isisPrefix = "urn:org.apache.isis.restfulobjects:rels/"
        var raw = rel.replace(roPrefix, "")
        raw = raw.replace(isisPrefix, "")
        if (raw.contains(";")) {
            raw = raw.split(";").first()  //TODO handle args=value separated by ;
        }
        return Relation.find(raw)!!
    }

    fun representation(): Represention {
        val roPrefix = "application/json;profile=\"urn:org.restfulobjects:repr-types/"
        val isisPrefix = "application/jsonprofile=\"urn:org.restfulobjects:repr-types/"
        var raw = type.replace(roPrefix, "")
        raw = raw.replace(isisPrefix, "")
        raw = raw.replace("\"", "")
        return Represention.find(raw)!!
    }

}

/**
 * RO SPEC restfulobject-spec.pdf §2.7.1
 * extends ->
 * IANA SPEC http://www.iana.org/assignments/link-relations/link-relations.xml
 */
enum class Relation(val type: String) {
    ACTION("action"),
    CLEAR("clear"),
    DESCRIBED_BY("describedby"), //ISIS. IANA:"describedBy"
    DETAILS("details"),
    DOMAIN_TYPE("domain-type"),
    DOMAIN_TYPES("domain-types"),
    ELEMENT("element"),
    HELP("help"),               //IANA
    ICON("icon"),               //IANA
    INVOKE("invoke"),
    LAYOUT("layout"),
    LOGOUT("logout"),
    MENU_BARS("menuBars"),
    MODIFY("modify"),
    NEXT("next"),               //IANA
    OBJECT_ICON("object-icon"),
    OBJECT_LAYOUT("object-layout"),
    PREVIOUS("previous"),       //IANA
    PROPERTY("property"),
    RETURN_TYPE("return-type"),
    SELF("self"),               //IANA
    SERVICE("service"),         //specified in both IANA & RO
    SERVICES("services"),
    UP("up"),                   //IANA
    UPDATE("update"),
    USER("user"),
    VALUE("value"),
    VERSION("version");

    companion object {
        fun find(value: String): Relation? = Relation.values().find { it.type == value }
    }
}

/**
 * RO SPEC restfulobject-spec.pdf §2.4.1
 */
enum class Represention(val type: String) {
    ACTION("action"),                      // missing in RO SPEC ???
    ACTION_DESCRIPTION("action-description"),
    ACTION_RESULT("action-result"),
    ACTION_PARAM_DESCRIPTION("action-param-description"),
    COLLECTION_DESCRIPTION("collection-description"),
    DOMAIN_TYPE("domain-type"),
    ERROR("error"),
    HOMEPAGE("homepage"),
    IMAGE_PNG("image/png"),
    LAYOUT_MENUBARS("layout-menubars"),
    LAYOUT_BS3("layout-bs3"),
    LIST("list"),
    OBJECT("object"),
    OBJECT_ACTION("object-action"),
    OBJECT_COLLECTION("object-collection"),
    OBJECT_LAYOUT_BS3("object-layout-bs3"), // missing in RO SPEC ???
    OBJECT_PROPERTY("object-property"),
    PROPERTY_DESCRIPTION("property-description"),
    SELF("self"),
    TYPE_LIST("type-list"),
    TYPE_ACTION_RESULT("type-action-result"),
    USER("user"),
    VERSION("version");

    companion object {
        fun find(value: String): Represention? = Represention.values().find { it.type == value }
    }

}
