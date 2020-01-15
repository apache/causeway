//
// groovy generateConfigDocs -f ".././core/config/target/classes/META-INF/spring-configuration-metadata.json" -o /tmp

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
    String description
    int searchOrder
    List<Property> properties = []

    String fileName() { prefix ? prefix : name }
}

List<PropertyGroup> groups = []
groups+= new PropertyGroup() {{
    prefix = "isis.reflector"
    name = "Reflector"
    description = "The component responsible for building up the metamodel"
    searchOrder = 2
}}


groups+= new PropertyGroup() {{
    prefix = "isis.reflector.facet"
    name = "Individual Reflector Facets"
    description = "Configuration of specific facet implementations"
    searchOrder = 1
}}

groups+= new PropertyGroup() {{
    prefix = "isis.reflector.validator"
    name = "Reflector Validator"
    description = "Configuration of the validator that checks the well-formedness of the inferred metamodel (how strict to be etc.)"
    searchOrder = 1
}}

groups+= new PropertyGroup() {{
    prefix = "isis.authentication.shiro"
    name = "Shiro Security Implementation"
    description = "Configuration of the Shiro implementation of the Authenticator and Authorizor APIs"
    properties: []
    searchOrder = 1
}}

groups+= new PropertyGroup() {{
    prefix = "isis.authentication"
    name = "Security Configuration (general)"
    description = "Configuration of the Shiro implementation of the Authenticator and Authorizor APIs"
    properties: []
    searchOrder = 2
}}

groups+= new PropertyGroup() {{
    prefix = "isis.persistor"
    name = "Object Store configuration (general)"
    description = "Configuration settings relating to all Object Store implementations"
    properties: []
    searchOrder = 520
}}

groups+= new PropertyGroup() {{
    prefix = "isis.persistor.datanucleus"
    name = "DataNucleus Object Store"
    description = "Configuration settings of the DataNucleus Object Store itself (the Apache Isis component that wraps DataNucleus library)"
    properties: []
    searchOrder = 510
}}

groups+= new PropertyGroup() {{
    prefix = "isis.persistor.datanucleus.impl"
    name = "DataNucleus Configuration"
    description = "Passed thru directly to DataNucleus, for the most part unused by Apache Isis itself"
    properties: []
    searchOrder = 500
}}

groups+= new PropertyGroup() {{
    prefix = "isis.service.email"
    name = "Email Domain Service configuration"
    description = "Configuration of the Email domain services"
    properties: []
    searchOrder = 502
}}

groups+= new PropertyGroup() {{
    prefix = "isis.services"
    name = "Domain Service configurations"
    description = "Configuration of individual domain services"
    properties: []
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.viewer.restfulobjects"
    name = "Restful Objects Viewer"
    description = "Configuration of the REST API surfaced by the Restful Objects viewer"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.viewer.wicket"
    name = "Wicket Viewer"
    description = "Configuration of the Wicket viewer"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.viewers"
    name = "Viewers Configuration (General)"
    description = "Configuration that applies to viewers"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.extensions"
    name = "Extensions"
    description = "Configuration that applies to the catalogue of extensions to the framework"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "resteasy"
    name = "RestEasy Configuration"
    description = "Configuration that applies to the RestEasy framework (that bootstraps the Restful Objects viewer)"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "spring"
    name = "Spring Configuration"
    description = "Configuration that applies to Spring itself"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "" // 'isis.objects', 'isis.environment'
    name = "Other"
    description = "Any other general or miscellaneous configuration settings"
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

for (PropertyGroup group in groups) {
    def buf = StringBuilder.newInstance()
    if(group.properties.size() > 0) {

        for (Property property in group.properties) {
            buf << "| "
            buf  << format("${property.name}\n")
//            buf << "|"
//            if(property.type) {
//                buf << " ${property.type}"
//            }
//            buf << "\n"
            buf << "| "
            if(property.defaultValue) {
                buf << format(" ${property.defaultValue}")
            }
            buf << "\n"
            buf << "| "
            if(property.description) {
                buf << toAsciidoc(" ${property.description}")
            }
            buf << "\n"
            buf << "\n"
        }


        def outputFile = new File(outputDir, "${group.fileName()}.adoc")
        outputFile.write(buf.toString())
    }
}

static String toAsciidoc(String str) {

    String lineFeed = " +\n";

    // simple html -> asciidoc substitutions
    str = str.replace("<p>", lineFeed);
    str = str.replace("</p>", "");

    str = str.replace("<i>", "_");
    str = str.replace("<b>", "*");
    //str = str.replaceAll("<a href=\"(.*)\">(.*?)</a>", "link:$1[$2]");

    str = str.replace("<code>", "`");
    str = str.replace("<\\code>", "`");
    str = str.replace("<tt>", "`");
    str = str.replace("<\\tt>", "`");

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
