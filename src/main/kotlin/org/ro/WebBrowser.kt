/*
 * Copyright (c) 2018. Robert Jaros
 */

package org.ro

import org.ro.DesktopWindow
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.html.Iframe
import pl.treksoft.kvision.navbar.NavForm.Companion.navForm
import pl.treksoft.kvision.navbar.Navbar.Companion.navbar
import pl.treksoft.kvision.toolbar.ButtonGroup.Companion.buttonGroup
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px

class WebBrowser : DesktopWindow("Web Browser", 800, 400) {

    override var height: CssSize? = null
        set(value) {
            super.height = value
            if (value?.second == UNIT.px) {
                iframe.height = (value.first - 125).px
            }
        }

    val iframe: Iframe
    val urlInput: TextInput

    init {
        caption = "Web Browser - Due to security reasons navigation is limited to the same domain!"
        minWidth = 400.px
        minHeight = 150.px
        padding = 2.px
        iframe = Iframe("https://rjaros.github.io/kvision/api/").apply {
            width = 100.perc
            height = 340.px
        }
        urlInput = TextInput().apply {
            width = 200.px
            marginLeft = 10.px
            setEventListener<TextInput> {
                change = {
                    iframe.location = self.value
                }
            }
        }
        navbar {
            marginBottom = 0.px
            paddingLeft = 0.px
            navForm {
                paddingLeft = 0.px
                buttonGroup {
                    button("", icon = "fa-arrow-left").onClick {
                        iframe.getIframeWindow().history.back()
                    }
                    button("", icon = "fa-arrow-right").onClick {
                        iframe.getIframeWindow().history.forward()
                    }
                }
                add(urlInput)
            }
        }
        add(iframe)
        iframe.setEventListener<Iframe> {
            load = {
                urlInput.value = iframe.location
            }
        }
    }

    companion object {
        fun run(container: Container) {
            container.add(WebBrowser())
        }
    }
}
