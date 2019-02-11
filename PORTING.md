

## Setup
proxy settings:
https://jjasonclark.com/how-to-setup-node-behind-web-proxy/
https://gist.github.com/EudesSilva/0329645b9c258e0495544b8a5ccd1454

## Build
Build is done via gradle - under Windows with gitbash:

* ./gradlew.bat tasks # list all gradle tasks
* ./gradlew.bat webpack-budle # create main.bundle.js
* ./gradlew.bat test --exclude-task npm-install
 
Internally gradle uses npm for the JS part.

 npm --verbose 
 
 If task npm-install hangs, try
 
 ./gradlew.bat npm-install --info --debug --stacktrace
 

Helps in identifying thing that may go wrong (eg. due to proxy settings).
 

##From ActionScript to Kotlin:
###DataTypes / Keywords
uint -> Int
void -> Unit
substr -> substring
charAt ->
Vector ->  MutableList<T> , mutableListOf()
trace -> console.log
   
###Patterns
Singleton -> object




 
DisplayManager.addView

json.resulttype
json.memberType
json.hasOwnProperty
Class
HttpService
push



https://discuss.kotlinlang.org/t/iterating-over-json-properties/1940/3
private fun jsonToMap(json: Json): Map<String, String> {
    val map: MutableMap<String, String> = linkedMapOf()
    for (key in js("Object").keys(json)) {
        map.put(key, json[key] as String)
    }
    return map
}

# Readings
http://petersommerhoff.com/dev/kotlin/kotlin-for-java-devs/

#Toolchain
intellij ultimate 2018
JetBrains IDE Support (plugin for chrome)

# Kotlin Concepts
- Delegated Properties
- lazyinit

# Libraries
klaxon https://github.com/cbeust/klaxon

# Graphics
https://github.com/unosviluppatore/kotlin-js-D3js-example
https://github.com/hnakamur/d3.js-class-diagram-example