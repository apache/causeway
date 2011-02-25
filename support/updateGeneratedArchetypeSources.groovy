import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import javax.xml.transform.*
import javax.xml.transform.stream.*

def ROOT="quickstart/target/generated-sources/archetype/src/main/resources/"

/////////////////////////////////////////////////////
//
// update archetype-metadata.xml
//
/////////////////////////////////////////////////////

def metaDataFile=new File(ROOT+"META-INF/maven/archetype-metadata.xml")

println "updating ${metaDataFile.path}"

def metaDataXml = new XmlSlurper().parseText(metaDataFile.text)
metaDataXml.modules.module.fileSets.fileSet.each { fileSet ->
    if(fileSet.directory=='ide/eclipse') {
        fileSet.@filtered='true'
    }
}

def metaDataSmb = new groovy.xml.StreamingMarkupBuilder().bind {
    mkp.xmlDeclaration()
    mkp.declareNamespace("":"http://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.0.0")
    mkp.yield(metaDataXml)
}

String indentXml(xml) {
    def factory = TransformerFactory.newInstance()
    factory.setAttribute("indent-number", 2);

    Transformer transformer = factory.newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
    StreamResult result = new StreamResult(new StringWriter())
    transformer.transform(new StreamSource(new ByteArrayInputStream(xml.toString().bytes)), result)
    return result.writer.toString()
}


metaDataFile.text = indentXml(metaDataSmb.toString())


/////////////////////////////////////////////////////
//
// update the .launch files
//
/////////////////////////////////////////////////////

new File(ROOT+"archetype-resources/").eachDirRecurse() { dir ->  

    dir.eachFileMatch(~/.*[.]launch/) { launchFile ->  

        println "updating ${launchFile.path}"

        def launchXml = new XmlSlurper().parseText(launchFile.text)
        def projectAttr = launchXml.stringAttribute.find { it.@key=="org.eclipse.jdt.launching.PROJECT_ATTR" }
        String oldValue=projectAttr.@value
        def newValue = oldValue.replaceAll("quickstart-","\\\${rootArtifactId}-")
        projectAttr.@value=newValue

        launchFile.text = """#set( \$symbol_pound = '#' )
#set( \$symbol_dollar = '\$' )
#set( \$symbol_escape = '\\' )
"""
        launchFile.append(XmlUtil.serialize(launchXml))
     }  
}



