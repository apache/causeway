package org.ro.org.ro.ui

import org.ro.core.aggregator.DiagramDispatcher
import org.ro.core.event.RoXmlHttpRequest
import org.ro.to.Argument
import org.ro.to.Link
import org.ro.to.Method
import org.ro.to.ValueType
import org.ro.ui.Command
import org.ro.ui.FormItem
import org.ro.ui.kv.RoDialog
import kotlin.js.Date

class ImageAlert() : Command {

    private val uuid:String = Date().toTimeString() //IMPROVE

    fun open() {
        val fi = FormItem("svg", ValueType.IMAGE.type)
        val formItems = mutableListOf<FormItem>()
        formItems.add(fi)
        val label = "Image Sample"
        RoDialog(caption = label, items = formItems, command = this).open()

        //FIXME extract code into SvgUtils ?? -> see PlantUmlPanel
        val plantUmlCode: String = "\"Bob -> Pita : sometimes is a\""
        // https://github.com/yuzutech/kroki
        val url = "https://kroki.io/"
        val args = mutableMapOf<String, Argument>()
        args.put("diagram_source", Argument("\"diagram_source\"", plantUmlCode))
        args.put("diagram_type", Argument("\"diagram_type\"", "\"plantuml\""))
        args.put("output_format", Argument("\"output_format\"", "\"svg\""))

        val link = Link(href = url, method = Method.POST.operation, args = args)
        val agr = DiagramDispatcher(uuid)
        RoXmlHttpRequest().processAnonymous(link, agr)

    }

    override fun execute() {
        //do nothing
    }

}

