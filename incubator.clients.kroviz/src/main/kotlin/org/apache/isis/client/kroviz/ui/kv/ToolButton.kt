package org.apache.isis.client.kroviz.ui.kv

import org.w3c.dom.DragEvent
import pl.treksoft.kvision.html.Div

class ToolButton(text: String) : Div(text) {

    init {
        //style = ButtonStyle.LINK
       // onEvent { DragEvent. }
    }

    fun ondragover(ev:DragEvent) {
        ev.preventDefault();
        console.log("[ToolButton.ondragover]")
        console.log(ev)
    }

    fun  allowDrop(ev:DragEvent) {
        ev.preventDefault();
    }

    fun drop(ev:DragEvent) {
        ev.preventDefault();
        console.log("[ToolButton.drop]")
        console.log(ev)
        //var data = ev.dataTransfer.getData("text");
        //ev.target.appendChild(document.getElementById(data));
    }
}
