//
// groovy generateConfigDocs -f "target/classes/META-INF/spring-configuration-metadata.json" -o /tmp

import groovy.json.JsonSlurper

def cli = new CliBuilder( usage: 'groovy generateConfigDocs -f inputFile -o outputDir')
cli.with {
    f(longOpt: 'file', args: 1, required: false, argName: 'inputFile', 'Fully qualified file name, defaults to spring-configuration-metadata.json')
    o(longOpt: 'outputDir', args: 1, required: false, argName: 'outputDir', 'Directory to write to, defaults to current directory')
}

def options = cli.parse(args)
if (!options) {
    return
}

def fileName=options.f
def outputDir=options.o

if( ! fileName ) {
    fileName = 'spring-configuration-metadata.json'
}
if( ! outputDir ) {
    outputDir = '.'
}

def inputFile = new File(fileName)
if(! inputFile.exists()) {
    System.err.println "Cannot find file '${fileName}'"
    System.exit (1)
}



class Property {
    String name
    String type
    String description
    String sourceType
    Object defaultValue
    Boolean deprecated
    Object deprecation
}

class PropertyGroup {
    String prefix
    String name
    int searchOrder
    List<Property> properties = []

    String fileName() { prefix ? prefix : name }
}

List<PropertyGroup> groups = []
groups+= new PropertyGroup() {{
    prefix = "isis.applib"
    name = "Applib"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.meta-model"
    name = "Core MetaModel"
    searchOrder = 501
}}


groups+= new PropertyGroup() {{
    prefix = "isis.core.meta-model.introspector"
    name = "Core MetaModel Introspection"
    searchOrder = 100
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.meta-model.validator"
    name = "MetaModel Validator"
    searchOrder = 101
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.runtime"
    name = "Core Runtime Services configurations"
    properties: []
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.runtime-services"
    name = "Core Runtime Services configurations"
    properties: []
    searchOrder = 101
}}

groups+= new PropertyGroup() {{
    prefix = "isis.security.shiro"
    name = "Shiro Security Implementation"
    properties: []
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.persistence.jdo-datanucleus"
    name = "JDO DataNucleus"
    properties: []
    searchOrder = 510
}}

groups+= new PropertyGroup() {{
    prefix = "isis.persistence.jdo-datanucleus.impl"
    name = "DataNucleus Configuration"
    properties: []
    searchOrder = 100
}}

groups+= new PropertyGroup() {{
    prefix = "isis.viewer.restfulobjects"
    name = "Restful Objects Viewer"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.viewer.wicket"
    name = "Wicket Viewer"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.extensions"
    name = "Extensions"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.value-types"
    name = "Value types"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.subdomains"
    name = "Subdomains"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.testing"
    name = "Testing"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.mappings"
    name = "Bounded Context Mappings"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.incubator"
    name = "Incubator"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.legacy"
    name = "Legacy"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "resteasy"
    name = "RestEasy Configuration"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "spring"
    name = "Spring Configuration"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "" // 'isis.objects', 'isis.environment'
    name = "Other"
    searchOrder = 999
}}

Comparator<PropertyGroup> comparator = { a, b -> a.searchOrder - b.searchOrder }
List<PropertyGroup> sortedGroups = []
sortedGroups.addAll groups
sortedGroups.sort(comparator)

def jsonSlurper = new JsonSlurper()
def data = jsonSlurper.parse(inputFile)

eachProperty:
for (property in data.properties) {
    if(['isis.raw-key-value-map',
        'isis.environment'].contains(property.name)) {
        // ignore these special cases
        continue
    }
    eachGroup:
    for (PropertyGroup group in sortedGroups) {
        if(property.name.startsWith(group.prefix)) {
            group.properties += property
            continue eachProperty
        }
    }
}

new File(outputDir).mkdirs()

def bufNav = StringBuilder.newInstance()

for (PropertyGroup group in groups) {
    def buf = StringBuilder.newInstance()
    if(group.properties.size() > 0) {

        bufNav << "* xref:section/${group.fileName()}.adoc[${group.name}]\n"

        buf << """= ${group.name}

include::../section-hooks/${group.fileName()}~pre.adoc[]

[cols="3a,2a,5a", options="header"]
|===
|Property
|Default
|Description
"""


        for (Property property in group.properties) {
            def anchorPropertyName = format("${property.name}\n")
            def formattedPropertyName = format("${property.name}\n")
            buf << """|
[[${anchorPropertyName}]]
${formattedPropertyName}
"""

            buf << "| "
            if(property.defaultValue) {
                buf << format(" ${property.defaultValue}")
            }
            buf << "\n"
            buf << "| "
            def propertyDescription = toAsciidoc(" ${property.description}")
            if(propertyDescription) {
                buf << propertyDescription
                buf << "\n"
                buf << "\n"
            }

//            String configFilePath = property.name.replace(".", File.separator) + ".adoc";
//            def dirBaseSplitAt = configFilePath.lastIndexOf(File.separator)
//            def configFileParentDir = outputDir + File.separator + configFilePath.substring(0, dirBaseSplitAt)
//            def configFileName = configFilePath.substring(dirBaseSplitAt+1)
//
//            def buf2 = StringBuilder.newInstance()
//            buf2 << "= `${property.name}`"
//            buf2 << "\n"
//            buf2 << "\n"
//            buf2 << "== Description\n"
//            buf2 << "\n"
//            if(propertyDescription) {
//                buf2 << propertyDescription
//            } else {
//                buf2 << "No description available."
//            }
//            buf2 << "\n"
//            buf2 << "== Type\n"
//            buf2 << "\n"
//            if(property.type) {
//                buf2 << "${property.type}"
//            } else {
//                buf2 << "Type not specified (assume string)."
//            }
//            buf2 << "\n"
//            buf2 << "\n"
//            buf2 << "== Default Value\n"
//            buf2 << "\n"
//            if(property.defaultValue) {
//                buf2 << "${property.defaultValue}"
//            } else {
//                buf2 << "No default value."
//            }
//            buf2 << "\n"
//
//            def configFile = new File(configFileParentDir, configFileName)
//            new File(configFileParentDir).mkdirs()
//            configFile.write(buf2.toString())
        }

        buf << """
|===

include::../section-hooks/${group.fileName()}~post.adoc[]
"""

        def outputFile = new File(outputDir, "${group.fileName()}.adoc")
        outputFile.write(buf.toString())
    }

}
def outputFile = new File(outputDir, "_nav.adoc")
outputFile.write(bufNav.toString())


System.out.println("");
System.out.println("");

static String toAsciidoc(String str) {

    if (str == null) return null;

    System.out.print(".");
    str = str.replaceAll( /\{@link[ ]+?([^}]+?)[ ]+?([^}]+?)}/, '$2')
    str = str.replaceAll( /\{@link (?:(?:[^}]|[.])+[.])*([^}]+)}/, '``$1``')
    str = str.replaceAll( /<tt>(?:(?:[^<]|[.])+[.])*([^<]+)<\/tt>/, '``$1``')
    str = str.replaceAll( /<code>(?:(?:[^<]|[.])+[.])*([^<]+)<\/code>/, '``$1``')
    str = str.replaceAll( /@apiNote -/, 'TIP:')
    str = str.replaceAll( /@apiNote/, 'TIP:')
    str = str.replaceAll( /@implNote -/, 'NOTE:')
    str = str.replaceAll( /@implNote/, 'NOTE:')
    str = str.replaceAll( /<tt>/, '`')
    str = str.replaceAll( /<\/tt>/, '`')
    str = str.replaceAll( /<code>/, '`')
    str = str.replaceAll( /<\/code>/, '`')

    File tf = File.createTempFile("input",".html")
    tf.write(str)   // write to the file

    String cmd = "pandoc --wrap=none -f html -t asciidoc " + tf.getCanonicalPath()
    String adoc = cmd.execute().text

    tf.delete()

    return adoc;
}

static String anchor(String str) {
    //return str.replace('.', '_').replace('-','_')
    return str;
}

static String format(String str, int len = 30) {

    String lineFeed = " +\n";

    if(str.length() <= len) {
        return str;
    }

    final StringBuilder buf = new StringBuilder();
    String remaining = str;

    while(remaining.length() > 0) {
        int lastDot = remaining.substring(0, len).lastIndexOf('.');
        int lastDash = remaining.substring(0, len).lastIndexOf('-');
        int splitAt = lastDot > 0
                ? lastDot + 1
                : lastDash > 0
                ? lastDash + 1
                : len;
        if(buf.length() > 0) {
            buf.append(lineFeed);
        }
        buf.append(remaining, 0, splitAt);
        remaining = remaining.substring(splitAt);

        if(remaining.length() <= len) {
            buf.append(lineFeed).append(remaining);
            remaining = "";
        }
    }

    def string = buf.toString()
//    System.out.println(str)
//    System.out.println(string)
    return string;
}
