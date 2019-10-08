package org.ro.to

import kotlinx.serialization.Serializable
import org.ro.core.Utils

@Serializable
data class Link(val rel: String = "",
                val method: String = Method.GET.operation,
                val href: String = "",
                val type: String = "",
        //TODO "args" should be changed to "arguments" - RO spec or SimpleApp?
                val args: Map<String, Argument> = emptyMap(),
        /* arguments can either be:
         * -> empty Map {}
         * -> Map with "value": null (cf. SO_PROPERTY)
         * -> Map with empty key "" (cf. ACTIONS_DOWNLOAD_META_MODEL)
         * -> Map with key,<VALUE> (cf. ACTIONS_RUN_FIXTURE_SCRIPT, ACTIONS_FIND_BY_NAME, ACTIONS_CREATE) */
                val arguments: Map<String, Argument?> = emptyMap(),
                val title: String = "") : TransferObject {

    fun argMap(): Map<String, Argument?>? {
        if (args.isNotEmpty()) {
            return args
        }
        if (arguments.isNotEmpty()) {
            return arguments
        }
        return null
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

}
