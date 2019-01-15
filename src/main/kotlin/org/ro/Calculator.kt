/*
 * Copyright (c) 2018. Robert Jaros
 */

package org.ro

import pl.treksoft.kvision.core.Border
import pl.treksoft.kvision.core.BorderStyle
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.html.Align
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Div.Companion.div
import pl.treksoft.kvision.panel.GridJustify
import pl.treksoft.kvision.panel.GridPanel.Companion.gridPanel
import pl.treksoft.kvision.utils.px

enum class Operator {
    PLUS,
    MINUS,
    DIVIDE,
    MULTIPLY
}

class Calculator : DesktopWindow("Calculator", 280, 270) {

    val inputDiv: Div
    var input: String = "0"
    var cleared = true
    var isDivider = false

    var first: Double = 0.0
    var operator: Operator? = null

    init {
        isResizable = false
        inputDiv = div("0", align = Align.RIGHT) {
            padding = 5.px
            marginTop = 15.px
            marginLeft = 15.px
            marginRight = 15.px
            border = Border(2.px, BorderStyle.SOLID)
        }
        gridPanel(columnGap = 5, rowGap = 5, justifyItems = GridJustify.CENTER) {
            padding = 10.px
            add(CalcButton("AC").onClick { clear() }, 4, 1)
            add(CalcButton("7").onClick { number(7) }, 1, 2)
            add(CalcButton("8").onClick { number(8) }, 2, 2)
            add(CalcButton("9").onClick { number(9) }, 3, 2)
            add(CalcButton("4").onClick { number(4) }, 1, 3)
            add(CalcButton("5").onClick { number(5) }, 2, 3)
            add(CalcButton("6").onClick { number(6) }, 3, 3)
            add(CalcButton("1").onClick { number(1) }, 1, 4)
            add(CalcButton("2").onClick { number(2) }, 2, 4)
            add(CalcButton("3").onClick { number(3) }, 3, 4)
            add(CalcButton("0").onClick { number(0) }, 1, 5)
            add(CalcButton(".").onClick { divider() }, 2, 5)
            add(CalcButton("=").onClick { calculate() }, 3, 5)
            add(CalcButton("/").onClick { operator(Operator.DIVIDE) }, 4, 2)
            add(CalcButton("*").onClick { operator(Operator.MULTIPLY) }, 4, 3)
            add(CalcButton("-").onClick { operator(Operator.MINUS) }, 4, 4)
            add(CalcButton("+").onClick { operator(Operator.PLUS) }, 4, 5)
        }
    }

    private fun clear() {
        input = "0"
        cleared = true
        isDivider = false
        first = 0.0
        operator = null
        printInput()
    }

    private fun number(num: Int) {
        if (input == "0" || cleared) {
            input = "$num"
        } else {
            input += "$num"
        }
        cleared = false
        printInput()
    }

    private fun divider() {
        if (!isDivider) {
            if (input == "0" || cleared) {
                input = "0."
            } else {
                input += "."
            }
            isDivider = true
        }
        cleared = false
        printInput()
    }

    private fun operator(op: Operator) {
        if (operator != null) {
            calculate()
        }
        first = input.toDouble()
        operator = op
        cleared = true
        isDivider = false
    }

    private fun calculate() {
        val second = input.toDouble()
        val result = when (operator) {
            Operator.PLUS -> first + second
            Operator.MINUS -> first - second
            Operator.MULTIPLY -> first * second
            Operator.DIVIDE -> first / second
            else -> input.toDouble()
        }
        input = result.toString()
        printInput()
        cleared = true
        operator = null
        isDivider = false
    }

    private fun printInput() {
        inputDiv.content = "$input"
    }

    companion object {
        fun run(container: Container) {
            container.add(Calculator())
        }
    }
}

class CalcButton(label: String) : Button(label) {
    init {
        width = 50.px
    }
}
