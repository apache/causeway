package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.utils.Utils

@Serializable
data class Link(val rel: String = "",
                val method: String = Method.GET.operation,
                val href: String,
                val type: String = "",
        //TODO "args" should be changed to "arguments" - RO spec or SimpleApp?
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
        console.log("[Link.argMap]")
        console.log(this)
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
        return rel.endsWith("/property")
    }

    fun isAction(): Boolean {
        return rel.endsWith("/action")
    }

    fun name(): String {
        return href.split("/").last()
    }

}
