Apache Isis
===========

*[Apache Isis](http://isis.apache.org)™ software is a framework for rapidly developing domain-driven apps in Java.  Write your business logic in entities, domain services and repositories, and the framework dynamically generates a representation of that domain model as a webapp or a RESTful API.*

To see Isis in action, watch these [screencasts](http://isis.apache.org/intro/tutorials/screencasts.html).

Get started yourself using the [Maven archetype](http://isis.apache.org/intro/getting-started/simple-archetype.html).

For help and support, join the [mailing lists](http://isis.apache.org/support.html).  

## Screenshots

Isis automatically generates the UI from the domain classes.  The following are taken from the [screenshots](http://isis.apache.org/intro/elevator-pitch/isis-in-pictures.html) page on the Isis website.  The corresponding domain classes from which this UI was built can be found [here](https://github.com/apache/isis/tree/master/example/application/quickstart_wicket_restful_jdo/dom/src/main/java/dom/todo).

A list of objects returned from a domain service action:

![](http://isis.apache.org/images/screenshots/08-collection-action.png)

A domain object:

![](http://isis.apache.org/images/screenshots/11-todo-entity.png)

Invoking an action:

![](http://isis.apache.org/images/screenshots/18-invoke-action-args.png)

The REST API for a domain object:

![](http://isis.apache.org/images/screenshots/screenshot-34-restful-entity.png)

## Extensions

The Wicket viewer is extensible; a number of extensions (hosted on github) are available integrating [google maps](https://github.com/danhaywood/isis-wicket-gmap3), [charting](https://github.com/danhaywood/isis-wicket-wickedcharts), and also a [calendar](https://github.com/danhaywood/isis-wicket-fullcalendar2) and [excel download](https://github.com/danhaywood/isis-wicket-excel).

#### Google maps v3 integration

Standalone collection:

<img src="https://raw.github.com/danhaywood/isis-wicket-gmap3/master/images/screenshot-1.png" style="width: 900px;"/>

Parented collection:

<img src="https://raw.github.com/danhaywood/isis-wicket-gmap3/master/images/screenshot-2.png" style="width: 900px;"/>


#### Calendar integration

Standalone collection:

<img src="https://raw.github.com/danhaywood/isis-wicket-fullcalendar2/master/images/standalone-collection.png" style="width: 900px;"/>


Parented collection in a custom dashboard view model

<img src="https://raw.github.com/danhaywood/isis-wicket-fullcalendar2/master/images/dashboard.png" style="width: 900px;"/>


Parented collection in a regular entity:

<img src="https://raw.github.com/danhaywood/isis-wicket-fullcalendar2/master/images/parented-collection.png" style="width: 900px;"/>

#### Excel integration

The extension renders a new tab (highlighted): 

<img src="https://raw.github.com/danhaywood/isis-wicket-excel/master/images/excel-tab.png" style="width: 900px;"/>

Clicking the tab provides a download link:

<img src="https://raw.github.com/danhaywood/isis-wicket-excel/master/images/download-link.png" style="width: 900px;"/>

The downloaded file can be opened in Excel:

<img src="https://raw.github.com/danhaywood/isis-wicket-excel/master/images/excel.png" style="width: 600px;"/>


#### Wicked Charts integration

Summary chart for a collection with `BigDecimal` properties:

<img src="https://raw.github.com/danhaywood/isis-wicket-wickedcharts/master/images/summarychart-tab.png" style="width: 900px;"/>

... renders the returned chart with associated summary data:

<img src="https://raw.github.com/danhaywood/isis-wicket-wickedcharts/master/images/summarychart.png" style="width: 900px;"/>


Scalar chart, being the result of an action to analyze `ToDoItem`s by their category:

<img src="https://raw.github.com/danhaywood/isis-wicket-wickedcharts/master/images/piechart.png" style="width: 900px;"/>

A scalar chart is simply a wrapper around a [WickedChart](http://wicked-charts.googlecode.com).
s

