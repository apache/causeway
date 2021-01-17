/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// prereq:
//
// run mvn license:download-licenses
//
// this will create target/generated-resources/license.xml for each module,
// which this script then parses through.
//

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import javax.xml.transform.*
import javax.xml.transform.stream.*

//
// phase 1: find missing dependencies from each of the license.xml files
//

def parentDir=new File(".")
def missing = []

parentDir.eachFileRecurse { file ->

  if ( file.name == "licenses.xml" ) {

    println file.canonicalPath

    def fileXml = new XmlSlurper().parseText(file.text)

    def anyMissing = false

    fileXml.dependencies.dependency.each { dependency ->

      def url = dependency.licenses.license.url.text()

      if(! url) {
	def mavenCoords = [ dependency.groupId.text() ,
                            dependency.artifactId.text() ,
                            dependency.version.text() ]
        missing.add(mavenCoords)
      }

    }
  }
}
missing = missing.unique().sort()


//
// phase 2: convert existing supplemental model entries into list form
//
def supModelsFile = new File("supplemental-model/src/main/resources/supplemental-models.xml")
def supModelsXml = new XmlSlurper().parseText(supModelsFile.text)

def supplements = []
supModelsXml.supplement.project.each { project ->
    def mavenCoords = [ project.groupId.text(),
                        project.artifactId.text(),
                        project.version.text() ]
    supplements.add(mavenCoords)
}

supplements = supplements.unique().sort()



//
// phase 3: check each list against the other
//

def missingCopy = []
missingCopy.addAll(missing)

def supplementsCopy = []
supplementsCopy.addAll(supplements)

missing.removeAll(supplementsCopy)
supplements.removeAll(missingCopy)


println("")
println("")
println("licenses to add to supplemental-models.xml:")
println("")

if (missing.size()) {
    missing.each {
        println(it)
    }
} else {
    println("nothing to add")
}



println("")
println("")
println("licenses to remove from supplemental-models.xml (are spurious):")
println("")

if (supplements.size()) {
    supplements.each {
        println(it)
    }
} else {
    println("nothing to remove")
}
