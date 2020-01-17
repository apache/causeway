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
    prefix = "isis.applib"
    name = "Applib"
    description = "Default configuration for applib annotations"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.meta-model"
    name = "Core MetaModel"
    description = "The component responsible for building and validating the metamodel"
    searchOrder = 501
}}


groups+= new PropertyGroup() {{
    prefix = "isis.core.meta-model.introspector"
    name = "Core MetaModel Introspection"
    description = "Configuration of the introspector component that actually builds the metamodel by introspecting the domain object classes"
    searchOrder = 100
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.meta-model.validator"
    name = "MetaModel Validator"
    description = "Configuration of the validator component that checks the well-formedness of the built metamodel (how strict to be etc.)"
    searchOrder = 101
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.runtime"
    name = "Core Runtime Services configurations"
    description = "Configuration of runtime (locale, timezone etc)."
    properties: []
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.core.runtime-services"
    name = "Core Runtime Services configurations"
    description = "Configuration of individual domain services"
    properties: []
    searchOrder = 101
}}

groups+= new PropertyGroup() {{
    prefix = "isis.security.shiro"
    name = "Shiro Security Implementation"
    description = "Configuration of the Shiro implementation of the Authenticator and Authorizor APIs"
    properties: []
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.persistence.jdo-datanucleus"
    name = "JDO DataNucleus"
    description = "Configuration settings of the DataNucleus JDO persistence (the Apache Isis component that wraps DataNucleus library)"
    properties: []
    searchOrder = 510
}}

groups+= new PropertyGroup() {{
    prefix = "isis.persistence.jdo-datanucleus.impl"
    name = "DataNucleus Configuration"
    description = "Passed thru directly to DataNucleus, for the most part unused by Apache Isis itself"
    properties: []
    searchOrder = 100
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
    prefix = "isis.extensions"
    name = "Extensions"
    description = "Configuration that applies to the catalogue of extensions to the framework"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.value-types"
    name = "Value types"
    description = "Configuration that applies to value type definitions"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.subdomains"
    name = "Subdomains"
    description = "Configuration that applies to value type definitions"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.mappings"
    name = "Bounded Context Mappings"
    description = "Configuration that applies to utilities and libraries for integrating (mapping between) bounded contexts"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.incubator"
    name = "Incubator"
    description = "Configuration that applies to incubating components"
    searchOrder = 501
}}

groups+= new PropertyGroup() {{
    prefix = "isis.legacy"
    name = "Legacy"
    description = "Configuration that applies to legacy components"
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
