[![Build Status](https://travis-ci.com/joerg-rade/kroviz.svg?branch=master)](https://travis-ci.com/joerg-rade/kroviz.svg?branch=master)

# About

kroViz is a viewer for [Restful Objects](http://www.restfulobjects.org) written in Kotlin/JS, using [kvision](https://rjaros.github.io/kvision) for the UI part. 
It's a generic client for applications that implement the Restful Objects Specification. 
An [Apache Isis](https://isis.apache.org/) application with the restful objects interface enabled 
[SimpleApp](https://github.com/apache/isis/tree/master/example/application/simpleapp), [ToDoApp](https://github.com/isisaddons/isis-app-todoapp) , etc. can be used for the server part. See [BACKEND.md] for setup instructions. 

A [Naked Objects for .NET](http://nakedobjects.net/home/index.shtml) application should work as well, but is not tested yet.

## How will the UI look like?

It will be similar to the well known Wicket-UI of Apache Isis, but will make use of Tabs for Lists and Objects instead of Bookmarking URLs.
The table widget will allow filtering/sorting on the client and scrolling behavior will be more like in tradition Desktop applications.  

TIP: After entering url, userId, password in the Connect Dialog (Main -> Connect), the menu gets populated. Actions (here: SimpleObjects.listAll) can be selected and a Tab with a table is displayed:

![Preview](./images/SimpleObjects.png)

TIP: Clicking on the StatusBar at the bottom will create another tab showing logged XmlHttpRequests:

![Preview2](./images/LogEntries.png)

TIP: Remote applications (here: [SEMAT](http://semat.ofbizian.com/)) can be used as well, and even mixed with local apps. 

![Remote Application](./images/SEMAT.png)
