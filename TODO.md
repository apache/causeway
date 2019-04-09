# TODO's
## Fix Integration Tests
* PropertyDescriptionHandlerTest ->
* 
## Implement Features
* Link Button for Objects
cell {
                        add(button("Delete") {
                            style = ButtonStyle.INFO
                        })
                    }

* Generic Tables (ResultList etc.)
* List<Member> ^= TObject?
* direct Accessors ^= Methods  / indirect Accessors ^= Map.get(byName), sufficient for use in tables 
* Form Layout
* Switch the backend from Apache Isis 1.16.0 to 2.0.0-RC2
* Use Layout.xml for menu and layout

### Parse XML
Either via 
* JAXB -> Java -> Kotlin/JS or
* https://github.com/pdvrieze/xmlutil (implementation("net.devrieze:xmlutil-js:0.9.0"))
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<bs3:grid
  xsi:schemaLocation="http://isis.apache.org/applib/layout/component
                      http://isis.apache.org/applib/layout/component/component.xsd
                      http://isis.apache.org/applib/layout/grid/bootstrap3
                      http://isis.apache.org/applib/layout/grid/bootstrap3/bootstrap3.xsd"
  xmlns:bs3="http://isis.apache.org/applib/layout/grid/bootstrap3"
  xmlns:c="http://isis.apache.org/applib/layout/component"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    ...
</bs3:grid>
```


## Fix Bugs   
* Sequence of menu entries is random. Use layout.xml
* LogEntries search/filter always shows first entry 
* On startup Configuration is added as separate menu entry
* LogEntries sometimes have negative offset

## Request kvision Features
-[x] Tooltips (#28)
-[ ] table, filterable by column
-[ ] (Google)Maps Integration
-[ ] Tree/TreeTable
-[ ] (Month/Week/Year)Calender with Icons
-[ ] D3.js 
    * https://github.com/unosviluppatore/kotlin-js-D3js-example
    * https://github.com/hnakamur/d3.js-class-diagram-example)

## Next 
* Integrate Calendar View (for ToDo App)
* Fullblown GanttChart (incl. birds eye view, cf. http://bl.ocks.org/bunkat/2338034 (d3.js))
* Display SVG's (cf. https://kotlinlang.org/api/latest/jvm/stdlib/org.w3c.dom.svg/index.html)
* Use SQL lite as store for url/credentials, customized layouts
* Visualize DomainModel (reconstruct from DomainObjects, Attributes, etc.)
* Hook up to the internal event bus of the server to have change notifications pushed. 
Callback server before display/edit. 

# Readings
http://petersommerhoff.com/dev/kotlin/kotlin-for-java-devs/

#Toolchain
intellij ultimate 2018
JetBrains IDE Support (plugin for chrome)
