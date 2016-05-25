/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import javax.xml.transform.*
import javax.xml.transform.stream.*


def cli = new CliBuilder(usage: 'updateGeneratedArchetypeSources.groovy -n [name] -v [version]')
cli.with {
    n(longOpt: 'name', args: 1, required: true, argName: 'name', 'Application name (eg \'simple\' or \'quickstart\')')
    v(longOpt: 'version', args: 1, required: true, argName: 'version', 'Isis core version to use as parent POM')
}


/////////////////////////////////////////////////////
//
// constants
//
/////////////////////////////////////////////////////

def BASE="target/generated-sources/archetype/"
def ROOT=BASE + "src/main/resources/"

def license_using_xml_comments="""<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
         http://www.apache.org/licenses/LICENSE-2.0
         
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->
"""

def supplemental_models_text="""<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
         http://www.apache.org/licenses/LICENSE-2.0
         
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->
<supplementalDataModels 
  xmlns="http://maven.apache.org/supplemental-model/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/supplemental-model/1.0.0 
            http://maven.apache.org/xsd/supplemental-model-1.0.0.xsd">

</supplementalDataModels>
"""

/////////////////////////////////////////////////////
//
// Parse command line
//
/////////////////////////////////////////////////////

def options = cli.parse(args)
if (!options) {
    return
}

application_name=options.n
isis_version=options.v

/////////////////////////////////////////////////////
//
// update archetype's own pom.xml's groupId
//
/////////////////////////////////////////////////////

def pomFile=new File(BASE+"pom.xml")

println "updating ${pomFile.path}"

// read file, ignoring XML pragma
def pomFileText = stripXmlPragma(pomFile)

def pomXml = new XmlSlurper(false,true).parseText(pomFileText)
pomXml.appendNode {
  parent {
    groupId("org.apache.isis.core")
    artifactId("isis")
    version(isis_version)
    relativePath("../../../core/pom.xml")
  }
}
pomXml.groupId='org.apache.isis.archetype'

def pomSmb = new groovy.xml.StreamingMarkupBuilder().bind {
    mkp.declareNamespace("":"http://maven.apache.org/POM/4.0.0")
    mkp.yield(pomXml)
}


def pomTempFile = File.createTempFile("temp",".xml")
def indentedXml = indentXml(pomSmb.toString())
pomTempFile.text = indentedXml
def pomXmlText = stripXmlPragma(pomTempFile)


pomFile.text = 
    license_using_xml_comments + 
    pomXmlText



///////////////////////////////////////////////////////
////
//// update archetype's resource's dom/pom.xml's activeByDefault=true
////
///////////////////////////////////////////////////////
//
//def pomDomFile=new File(BASE+"src/main/resources/archetype-resources/dom/pom.xml")
//
//println "updating ${pomDomFile.path}"
//
//// read file, ignoring XML pragma
//def pomDomFileText = stripXmlPragma(pomDomFile)
//
//def pomDomXml = new XmlSlurper(false,true).parseText(pomDomFileText)
//
//pomDomXml.profiles.profile.activation.activeByDefault='true'
//
//def pomDomSmb = new groovy.xml.StreamingMarkupBuilder().bind {
//     mkp.declareNamespace("":"http://maven.apache.org/POM/4.0.0")
//     mkp.yield(pomDomXml)
//}
//
//
//def pomDomTempFile = File.createTempFile("temp",".xml")
//def indentedDomXml = indentXml(pomDomSmb.toString())
//pomDomTempFile.text = indentedDomXml
//def pomDomXmlText = stripXmlPragma(pomDomTempFile)
//
//
//pomDomFile.text = 
//    license_using_xml_comments + 
//    pomDomXmlText



/////////////////////////////////////////////////////
//
// update archetype-metadata.xml
//
/////////////////////////////////////////////////////


def metaDataFile=new File(ROOT+"META-INF/maven/archetype-metadata.xml")

println "updating ${metaDataFile.path}"


// read file, ignoring XML pragma
def metaDataFileText = stripXmlPragma(metaDataFile)

def metaDataXml = new XmlSlurper().parseText(metaDataFileText)
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

def tempFile = File.createTempFile("temp",".xml")
tempFile.text = indentXml(metaDataSmb.toString())
def metaDataXmlText = stripXmlPragma(tempFile)


metaDataFile.text = 
    license_using_xml_comments + 
    metaDataXmlText


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
        def newValue = oldValue.replaceAll("${application_name}[^-]*-","\\\${rootArtifactId}-")
        projectAttr.@value=newValue

        launchFile.text = """#set( \$symbol_pound = '#' )
#set( \$symbol_dollar = '\$' )
#set( \$symbol_escape = '\\' )
"""
        launchFile.append(XmlUtil.serialize(launchXml))
     }  
}


///////////////////////////////////////////////////
//
// add empty supplemental-models.xml
//
///////////////////////////////////////////////////

def appendedResourcesDir = new File(BASE + "src/main/appended-resources")
appendedResourcesDir.mkdir()
def supplementalModelsFile=new File(appendedResourcesDir, "supplemental-models.xml")
supplementalModelsFile.text = supplemental_models_text



///////////////////////////////////////////////////
//
// helper methods
//
///////////////////////////////////////////////////

String indentXml(xml) {
    def factory = TransformerFactory.newInstance()
    factory.setAttribute("indent-number", 4);

    Transformer transformer = factory.newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
    StreamResult result = new StreamResult(new StringWriter())
    transformer.transform(new StreamSource(new ByteArrayInputStream(xml.toString().bytes)), result)
    return result.writer.toString().replaceAll("\\?><", "\\?>\n<")
}

String stripXmlPragma(File file) {
    def sw = new StringWriter()
    file.filterLine(sw) { ! (it =~ /^\<\?xml/ ) }
    return sw.toString()
}

