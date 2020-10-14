<!--
Notice: Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at. http://www.apache.org/licenses/LICENSE-2.0 . Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
-->


# Users Guide
This is about explaining what can be done on the client side in order to customize the viewer.

## Branding
See kroviz.css

Colours, dimensions, and fonts can be influenced here, as well as images:

```css
.text-danger {
    color: red !important;
}

.text-warn {
    color: orange !important;
}

.text-ok {
    color: green !important;
}

.text-normal {
    color: black;
}
/*...*/
@font-face {
    font-family: Chicago;
    src: url('../fonts/ChicagoFLF.ttf');
}

div {
   /* font-family: Chicago, sans-serif;*/
}

.logo-button-image:before {
    content: "";
    width: 75px;
    height: 40px;
    display: inline-block;
    vertical-align: text-top;
    background-color: transparent;
    background-position : center center;
    background-repeat:no-repeat;
    background-size: contain, cover;
    background-image : url("https://svn.apache.org/repos/asf/comdev/project-logos/originals/isis.svg");
}
```  

## Rendering
Some data, eg. Dates can be rendered differently depending on context:
* when editing in a form it can be practical to click on a cell in a 5 by 7 table representing a month, or
* selecting a point on a x-axis, resembling a timeline.

Objects may even be aggregations of different aspects, eg. may have a Date and a Location so their representation
depends on the aspect and the perspective most useful to the user working with them.

Which renderers should be provided for what data?

One approach to identifying applicable renderers is [duck typing](https://en.wikipedia.org/wiki/Duck_typing).
This means resorting to behavior / properties of an object. If eg. an object has two properties of type date, labeled "start" and "end"
it is reasonable to render it as horizontal bar on a timeline. If there is a list of such objects, they can form a gantt chart. 

The number of renderers included with kroviz is limited and there isn't yet an example of selecting one of possibly many applicale renderers, but there is room for impovement and creativity.
Such renderes should as well have links leading back to 'plain old object' renderings.

![Preview](./uml-renderer.png)

## Sneak Preview (Demo)
* Burger Menu -> Connect, OK
* Primitives -> Temporals
* Primitives -> Blob
* _Burger Menu -> History_: 
** Sort, 
** Move Column, 
** Filter
* History -> Details Menu, Visualize
