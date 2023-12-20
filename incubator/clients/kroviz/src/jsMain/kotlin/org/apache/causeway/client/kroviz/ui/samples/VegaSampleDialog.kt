package org.apache.causeway.client.kroviz.ui.samples

import io.kvision.core.StringPair
import io.kvision.form.select.Select
import org.apache.causeway.client.kroviz.core.event.ResourceSpecification
import org.apache.causeway.client.kroviz.to.Link
import org.apache.causeway.client.kroviz.to.ValueType
import org.apache.causeway.client.kroviz.ui.core.FormItem
import org.apache.causeway.client.kroviz.ui.core.RoDialog
import org.apache.causeway.client.kroviz.ui.core.SessionManager
import org.apache.causeway.client.kroviz.ui.core.ViewManager
import org.apache.causeway.client.kroviz.ui.dialog.Controller
import org.apache.causeway.client.kroviz.ui.dialog.VegaPanel

class VegaSampleDialog : Controller() {
    private val prefix = "https://vega.github.io/vega/examples/"
    private val postfix = ".vg.json"
    private var url = prefix + "clock" + postfix
    override fun open() {
        val formItems = mutableListOf<FormItem>()
        val urlList = mutableListOf<StringPair>()

        urlList.add(buildOption("bar-chart"))
        urlList.add(buildOption("job-voyager"))
        urlList.add(buildOption("donut-chart"))
        urlList.add(buildOption("violin-plot"))

        formItems.add(FormItem("Url", ValueType.SIMPLE_SELECT, urlList))
        dialog = RoDialog(caption = "Select chart", items = formItems, controller = this, heightPerc = 27)
        val at = ViewManager.position!!
        dialog.open(at)
    }

    private fun buildOption(chartName: String): StringPair {
        val chartUrl = prefix + chartName + postfix
        return StringPair(chartUrl, chartName)
    }

    override fun execute(action: String?) {
        extractUserInput()
        val link = Link(href = url)
        invoke(link)
    }

    private fun extractUserInput() {
        val formPanel = dialog.formPanel
        val kids = formPanel!!.getChildren()
        for (i in kids) {
            when (i) {
                is Select -> {
                    url = i.getValue()!!
                }
            }
        }
    }

}