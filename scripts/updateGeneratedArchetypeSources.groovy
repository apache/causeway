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

import groovy.xml.XmlUtil

def cli = new CliBuilder(usage: 'updateGeneratedArchetypeSources.groovy -n [name] -v [version]')
cli.with {
    n(longOpt: 'name', args: 1, required: true, argName: 'name', 'Application name (eg \'simpleapp\' or \'helloworld\')')
    v(longOpt: 'version', args: 1, required: true, argName: 'version', 'Isis core version to use as parent POM')
}


/////////////////////////////////////////////////////
//
// constants
//
/////////////////////////////////////////////////////

def BASE="target/generated-sources/archetype/"
def ROOT=BASE + "src/main/resources/"


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

println "isis_version = ${isis_version}"

/////////////////////////////////////////////////////
//
// update archetype's own pom.xml's groupId
//
/////////////////////////////////////////////////////

def pomFile=new File(BASE+"pom.xml")

println "updating ${pomFile.path}"

// read file, ignoring XML pragma
def pomFileText = stripXmlPragma(pomFile)

def pomXml = new XmlSlurper(false,false).parseText(pomFileText)
pomXml.appendNode {
  parent {
    groupId("org.apache.isis.core")
    artifactId("isis")
    version(isis_version)
    relativePath("../../../core/pom.xml")
  }
}
pomXml.groupId='org.apache.isis.archetype'

pomXml.appendNode(new XmlSlurper( false, false ).parseText( '''
  <parent>
    <groupId>org.apache</groupId>
    <artifactId>apache</artifactId>
    <version>18</version>
    <relativePath />
  </parent>
''' )
)

pomXml.appendNode(new XmlSlurper( false, false ).parseText( '''
<properties>
    <archetype.test.skip>true</archetype.test.skip>
</properties>
''' )
)

pomXml.appendNode(new XmlSlurper( false, false ).parseText( '''
  <profiles>
    <profile>
      <!--
      as per https://stackoverflow.com/a/28860520/56880
      allows -Dgpg.passphrase= to be used rather than gpg.useagent
      inherited from parent.
      Note that this requires gpg v2.1+
      -->
      <id>gpg</id>
      <activation>
        <property>
          <name>gpg.passphrase</name>
        </property>
      </activation>
      <properties>
        <gpg.useagent>false</gpg.useagent>
      </properties>
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-gpg-plugin</artifactId>
            <executions>
              <execution>
                <id>sign-release-artifacts</id>
                <goals>
                  <goal>sign</goal>
                </goals>
                <configuration>
                  <gpgArguments>
                    <arg>--pinentry-mode</arg>
                    <arg>loopback</arg>
                  </gpgArguments>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
''' )
)

pomFile.text = withLicense(pomXml)


pomFile.text = serializeWithLicense(pomXml)

/////////////////////////////////////////////////////
//
// update archetype's resource's pom.xml's
// <revision> and <isis.version>
//
/////////////////////////////////////////////////////

def resourcePomXmlFile=new File(BASE+"src/main/resources/archetype-resources/pom.xml")

println "updating ${resourcePomXmlFile.path}"

def resourcePomXml = new XmlSlurper(false,false).parseText(resourcePomXmlFile.text)


// the properties.revision, meanwhile, is set to the version that is prompted for when the
// app is first generated from the archetype
resourcePomXml.properties.revision='${version}'

resourcePomXml.properties['isis.version']=isis_version

resourcePomXml.dependencyManagement.dependencies.dependency.each { dependency ->
    if(dependency.groupId=='${groupId}') {
        dependency.version='${project.version}'
    }
}


resourcePomXmlFile.text = withLicense(resourcePomXml)


/////////////////////////////////////////////////////
//
// update the pom files parent version
//
/////////////////////////////////////////////////////

new File(ROOT+"BASE+\"src/main/resources/archetype-resources/").eachDirRecurse() { dir ->

    dir.eachFileMatch(~/pom[.].xml/) { eachPomXmlFile ->

        println "updating ${eachPomXmlFile.path}"

        def eachPomXml = new XmlSlurper(false,false).parseText(eachPomXmlFile.text)
        if(eachPomXml.parent.groupId=='${groupId}') {
            eachPomXml.parent.version='${revision}'
        }

        eachPomXmlFile.text = withLicense(eachPomXml)
    }
}

/////////////////////////////////////////////////////
//
// update archetype-metadata.xml
//
/////////////////////////////////////////////////////

def metaDataFile=new File(ROOT+"META-INF/maven/archetype-metadata.xml")

println "updating ${metaDataFile.path}"


// read file, ignoring XML pragma
def metaDataFileText = stripXmlPragma(metaDataFile)

def metaDataXml = new XmlSlurper(false,false).parseText(metaDataFileText)
metaDataXml.modules.module.fileSets.fileSet.each { fileSet ->
    if(fileSet.directory=='ide/eclipse') {
        fileSet.@filtered='true'
    }
}

// https://issues.apache.org/jira/browse/ARCHETYPE-548
metaDataXml.modules.module.each { module ->
    if(module.@dir=='webapp') {
        module.fileSets.fileSet.each { fileSet ->
            if(fileSet.directory=='src/main/resources') {
                fileSet.includes.include.each { include ->
                    if(include == '**/*.') {
                        include.replaceBody "**/*"
                    }
                }
            }
        }
    }
}

metaDataFile.text = serializeWithLicense(metaDataXml)



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

String serializeWithLicense(xmlNode) {

    def pragma="""<?xml version="1.0" encoding="UTF-8"?>"""
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

    def xmlText = XmlUtil.serialize( new groovy.xml.StreamingMarkupBuilder().bind { mkp.yield(xmlNode) } )
    return xmlText.replace(pragma, license_using_xml_comments)
}

String stripXmlPragma(File file) {
    def sw = new StringWriter()
    file.filterLine(sw) { ! (it =~ /^\<\?xml/ ) }
    return sw.toString()
}


