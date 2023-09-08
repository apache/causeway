/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

function svgCheckBox (label) {

    var size = 20,
        x = 0,
        y = 0,
        rx = 0,
        ry = 0,
        checked = false,
        clickEvent,
		calculatedTextHeight = 10,
		labelMarginLeft = size/3;
		
    function checkBox (selection) {

        var g = selection.append("g");
        var box = g.append("rect")
			.attr("class", "checkBox")
            .attr("width", size)
            .attr("height", size)
            .attr("x", x)
            .attr("y", y)
            .attr("rx", rx)
            .attr("ry", ry)
            ;

        // coors to represent the check mark path
        var coordinates = [
            {x: x + (size / 8), y: y + (size / 3)},
            {x: x + (size / 2.2), y: (y + size) - (size / 4)},
            {x: (x + size) - (size / 8), y: (y + (size / 10))}
        ];
		
		// coors to place the label
		var labelBottomLeft = {
				x: (x + size) + labelMarginLeft,  
				y: (y + (size+calculatedTextHeight) / 2)
		};

        var line = d3.line()
                .x(function(d){ return d.x; })
                .y(function(d){ return d.y; })
                .curve(d3.curveCardinal)
				;

        var mark = g.append("path");
		
        mark.attr("d", line(coordinates))
			.attr("class", (checked)? "checkMark-checked" : "checkMark-unchecked");
			
		var text = g.append("text");
		text.attr("x", labelBottomLeft.x).attr("y", labelBottomLeft.y).text(label);

        g.on("click", function () {
            checked = !checked;
            mark.attr("class", (checked)? "checkMark-checked" : "checkMark-unchecked");

            if(clickEvent) {
                clickEvent();
			}

            d3.event.stopPropagation();
        });

    }

    checkBox.size = function (val) {
        size = val;
        return checkBox;
    }

    checkBox.x = function (val) {
        x = val;
        return checkBox;
    }

    checkBox.y = function (val) {
        y = val;
        return checkBox;
    }

    checkBox.rx = function (val) {
        rx = val;
        return checkBox;
    }

    checkBox.ry = function (val) {
        ry = val;
        return checkBox;
    }

    checkBox.checked = function (val) {

        if(val === undefined) {
            return checked;
        } else {
            checked = val;
            return checkBox;
        }
    }

    checkBox.clickEvent = function (val) {
        clickEvent = val;
        return checkBox;
    }

    return checkBox;
}