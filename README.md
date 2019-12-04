![kroviz Logo](./images/kroviz-logo.svg )
# kroviz 
(to be pronounced: [krous]) is a viewer for [Restful Objects](http://www.restfulobjects.org) written in [Kotlin/JS](https://github.com/JetBrains/kotlin/tree/master/js), using [KVision](https://rjaros.github.io/kvision) for the UI part. 


[![Build Status](https://travis-ci.com/joerg-rade/kroviz.svg?branch=master)](https://travis-ci.com/joerg-rade/kroviz.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/joerg-rade/kroviz/badge.svg?branch=master)](https://coveralls.io/github/joerg-rade/kroviz?branch=master)
[![License: Apache 2](https://img.shields.io/badge/license-Apache%202-blue)](https://opensource.org/licenses/Apache-2.0)
## About

kroviz is a generic client for applications that implement the Restful Objects Specification, namely:

* [Apache Isis](https://isis.apache.org/)
* [Naked Objects for .NET](http://nakedobjects.net/home/index.shtml)

In order to see it working, setup a server with an [Apache Isis](https://isis.apache.org/) application with the restful objects interface enabled 
[SimpleApp](https://github.com/apache/isis/tree/master/example/application/simpleapp), [ToDoApp](https://github.com/isisaddons/isis-app-todoapp), 
etc. See [setup instructions](./docs/DevelopmentGuide.md#setup-the-back-end). 

A [Naked Objects for .NET](http://nakedobjects.net/home/index.shtml) application should work as well, but is not tested yet.

![Preview](./docs/arc-overview.png)

## User Interface

The GUI will be similar to the well known Wicket-UI of Apache Isis, but will make use of Tabs for Lists and Objects instead of Bookmarking URLs.
The table widget allows filtering/sorting on the client and scrolling behavior will be more like in traditional desktop applications.  

In the left upper corner you will find a burger icon with an drop down menu. Click and select -> Connect, edit/confirm the settings and press OK.
![Preview](./images/Connect1.png)

The default url is localhost:8080/
![Preview](./images/Connect2.png)

The menu gets populated and you may select Prototyping -> runFixtureScript:
![Preview](./images/RunFixtureScript1.png)
![Preview](./images/RunFixtureScript2.png)
![Preview](./images/RunFixtureScript3.png)
In order to filter the list, enter a string:
![Preview](./images/RunFixtureScript4.png)

Other lists work the same way, eg. SimpleObjects -> listAll:
![Preview](./images/ListAll1.png)
Clicking on the icon with three dots on the right will open a context menu:
![Preview](./images/ListAll2.png)

'Burger' -> Log Entries  creates another tab showing logged XmlHttpRequests, sorted by number of 'cache hits'.
![Preview](./images/LogEntries1.png)
Not only XHR's are logged, but 'screen events' as well (filtered for type VIEW)
![Preview2](./images/LogEntries2.png)


# Contributing
Contributions are welcome! Especially:
* bug reports
* code review
* tests increasing the code coverage
* documentation improvements
* feature requests
* comments regarding usability

The contribution workflow is analogous to that of Apache Isis (see: [Contributing](https://isis.apache.org/guides/dg/dg.html#_dg_contributing)), 
except for issues use GitHub instead of JIRA.

