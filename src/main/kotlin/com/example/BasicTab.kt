package com.example

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Div.Companion.div
import pl.treksoft.kvision.html.Iframe.Companion.iframe
import pl.treksoft.kvision.html.Image.Companion.image
import pl.treksoft.kvision.html.ImageShape
import pl.treksoft.kvision.html.Label.Companion.label
import pl.treksoft.kvision.html.Link.Companion.link
import pl.treksoft.kvision.html.ListTag.Companion.listTag
import pl.treksoft.kvision.html.ListType
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.Tag.Companion.tag
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.table.Cell.Companion.cell
import pl.treksoft.kvision.table.Row.Companion.row
import pl.treksoft.kvision.table.Table.Companion.table
import pl.treksoft.kvision.table.TableType
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px

class BasicTab : SimplePanel() {
    init {
        this.marginTop = 10.px
        this.minHeight = 400.px
        vPanel(spacing = 3) {
            label(tr("A simple label"))
            label(tr("A label with custom CSS styling")) {
                fontFamily = "Times New Roman"
                fontSize = 32.px
                fontStyle = FontStyle.OBLIQUE
                fontWeight = FontWeight.BOLDER
                fontVariant = FontVariant.SMALLCAPS
                textDecoration = TextDecoration(TextDecorationLine.UNDERLINE, TextDecorationStyle.DOTTED, Col.RED)
            }
            label(tr("A list:"))
            listTag(ListType.UL, listOf(tr("First list element"), tr("Second list element"), tr("Third list element")))
            label(tr("An image:"))
            image(require("img/dog.jpg"), shape = ImageShape.CIRCLE)
            tag(TAG.CODE, tr("Some text written in <code></code> HTML tag."))
            tag(
                TAG.DIV,
                tr(
                    "Rich <b>text</b> <i>written</i> with <span style=\"font-family: Verdana; font-size: 14pt\">" +
                            "any <strong>forma</strong>tting</span>."
                ),
                rich = true
            )
            link(tr("A link to Google"), "http://www.google.com")
            label(tr("A responsive table:"))
            table(
                listOf(tr("Column 1"), tr("Column 2"), tr("Column 3")),
                setOf(TableType.BORDERED, TableType.CONDENSED, TableType.STRIPED, TableType.HOVER), responsive = true
            ) {
                row {
                    cell("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis.")
                    cell("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis.")
                    cell("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis.")
                }
                row {
                    cell("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis.")
                    cell("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis.")
                    cell("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis.")
                }
            }
            label(tr("A Handlebars.js template:"))

            val data = obj {
                name = "Alan"
                hometown = "Somewhere, TX"
                kids = arrayOf(obj {
                    name = "Jimmy"
                    age = "12"
                }, obj {
                    name = "Sally"
                    age = "5"
                })
            }

            div {
                templates = mapOf(
                    "en" to require("hbs/template1.en.hbs"),
                    "pl" to require("hbs/template1.pl.hbs")
                )
                templateData = data
            }

            label(tr("An iframe:"))

            iframe(src = "https://rjaros.github.io/kvision/api/") {
                iframeWidth = 600
                iframeHeight = 300
            }

        }
    }
}