package org.ro.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *  Custom data structure to handle "args" and "arguments" used in Link.
 *  "arguments" can either be:
 *  @Item a map of Arguments (key/value pairs), eg. name: xxx, script: yyy, style: zzz or
 *  @Item a single Argument ('value')
 *  ==> "arguments": {
can either be:
empty Map {}					==>
Map with "value": null		==> SO_PROPERTY
Map with empty key "": 		==> ACTIONS_DOWNLOAD_META_MODEL
(regular) Map with key,<VALUE> ==> ACTIONS_RUN_FIXTURE_SCRIPT, ACTIONS_FIND_BY_NAME, ACTIONS_CREATE
 */
@Serializable
data class Arguments(
        @SerialName("value") val argument: Value? = null,
        //TODO this needs refactoring
        val name: Argument? = null,
        val script: Argument? = null,
        val style: Argument? = null,
        val parameters: Argument? = null,
        val fileName: Argument? = null,
        val type: Argument? = null,
        @SerialName(".csvFileName") val csvFileName: Argument? = null,
        @SerialName("") val empty: Argument? = null,
        val filename: Argument? = null,
        val visibility: Argument? = null,
        val format: Argument? = null
) : TransferObject {

    fun isNotEmpty(): Boolean {
        return (argument != null
                || name != null
                || script != null
                || style != null
                || fileName != null
                || type != null
                || csvFileName != null
                || empty != null
                || filename != null
                || visibility != null
                || format != null
                )
    }

    fun asMap(): Map<String, Argument> {
        val map = mutableMapOf<String, Argument>()
        if (argument != null) {
            map["single"] = Argument(value = argument.toString())
        }
        if (name != null) {
            map["name"] = name
        }
        if (script != null) {
            map["script"] = script
        }
        if (parameters != null) {
            map["parameters"] = parameters
        }
        if (style != null) {
            map["style"] = style
        }
        if (fileName != null) {
            map["fileName"] = fileName
        }
        if (type != null) {
            map["type"] = type
        }
        if (csvFileName != null) {
            map["csvFileName"] = csvFileName
        }
        if (empty != null) {
            map["empty"] = empty
        }
        if (filename != null) {
            map["filename"] = filename
        }
        if (visibility != null) {
            map["visibility"] = visibility
        }
        if (format != null) {
            map["format"] = format
        }
        return map
    }

}

