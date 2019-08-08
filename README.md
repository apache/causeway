[![Build Status](https://travis-ci.com/joerg-rade/kroviz.svg?branch=master)](https://travis-ci.com/joerg-rade/kroviz.svg?branch=master)

![Preview](./images/WheatFieldWithCrows.png)
# About

kroViz (to be pronounced: [krous]) is a viewer for [Restful Objects](http://www.restfulobjects.org) written in Kotlin/JS, using [kvision](https://rjaros.github.io/kvision) for the UI part. 

It's a generic client for applications that implement the Restful Objects Specification, namely:

* [Apache Isis](https://isis.apache.org/)
* [Naked Objects for .NET](http://nakedobjects.net/home/index.shtml)

In order to see it working, setup a server with an [Apache Isis](https://isis.apache.org/) application with the restful objects interface enabled 
[SimpleApp](https://github.com/apache/isis/tree/master/example/application/simpleapp), [ToDoApp](https://github.com/isisaddons/isis-app-todoapp), 
etc. See [BACKEND.md] for setup instructions. 

A [Naked Objects for .NET](http://nakedobjects.net/home/index.shtml) application should work as well, but is not tested yet.

## How does the UI look like?

It will be similar to the well known Wicket-UI of Apache Isis, but will make use of Tabs for Lists and Objects instead of Bookmarking URLs.
The table widget will allow filtering/sorting on the client and scrolling behavior will be more like in traditional Desktop applications.  

In the left upper corner you will find a burger icon with an drop down menu. Click and select -> Connect, edit/confirm the settings and press OK.
The menu gets populated. 

* Prototyping -> runFixtureScript
* SimpleObjects -> listAll
* Configuration -> configuration


![Preview](./images/SimpleObjects.png)

'Burger' -> Log Entries  creates another tab showing logged XmlHttpRequests:

![Preview2](./images/LogEntries.png)

TIP: Remote applications (here: [SEMAT](http://semat.ofbizian.com/)) can be used as well, and even mixed with local apps. 

![Remote Application](./images/SEMAT.png)


#Credits
Artwork reused:

* https://commons.wikimedia.org/wiki/File:Vincent_Van_Gogh_-_Wheatfield_with_Crows.jpg by Vincent van Gogh 1890
* https://fr.wikipedia.org/wiki/Fichier:Meuble_corbeau.svg by Henry Salomé le 06/12/2006
