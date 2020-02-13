package org.ro.org.ro.utils

import org.ro.core.aggregator.DiagramDispatcher
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Argument
import org.ro.to.Link
import org.ro.to.Method

object UmlUtils {

    fun generateDiagram(plantUmlCode: String, elementId: String) {
        // https://github.com/yuzutech/kroki
        val url = "https://kroki.io/"
        val args = mutableMapOf<String, Argument>()
        args.put("diagram_source", Argument("\"diagram_source\"", plantUmlCode))
        args.put("diagram_type", Argument("\"diagram_type\"", "\"plantuml\""))
        args.put("output_format", Argument("\"output_format\"", "\"svg\""))

        val link = Link(href = url, method = Method.POST.operation, args = args)
        val agr = DiagramDispatcher(elementId)
        RoXmlHttpRequest().processAnonymous(link, agr)
    }
}
