package org.apache.isis.client.kroviz.utils

import org.apache.isis.client.kroviz.core.aggregator.DiagramDispatcher
import org.apache.isis.client.kroviz.core.event.RoXmlHttpRequest
import org.apache.isis.client.kroviz.to.Argument
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method

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
