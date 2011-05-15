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

def currentDir=new File(".")

currentDir.eachFileRecurse { file ->
  if ( file.name == "licenses.xml" ) {


    def fileXml = new XmlSlurper().parseText(file.text)

    def anyMissingLicenses = false

    fileXml.dependencies.dependency.each { dependency ->

      def url = dependency.licenses.license.url.text()

      if(! url) {

        if (!anyMissingLicenses) {
          println( "----------------------------------------------------------" )
          println( file.canonicalPath )
          println( "----------------------------------------------------------" )
          anyMissingLicenses = true
        }

	println( dependency.groupId.text() + " : " + dependency.artifactId.text() + " : " + dependency.version.text() )
      }

    }
  }
}
