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
// For adhoc search-and-replace of files. 
//

def cli = new CliBuilder( usage: 'groovy searchandreplace [-xv]')
cli.x(argName: 'exec', 
      longOpt: 'exec', 
      'execute (perform changes if any found)')
cli.v(argName: 'verbose', 
      longOpt: 'verbose', 
      'verbose')

def options=cli.parse(args)

if(options.x) {
   println("-x (execute) flag specified: will make changes to files (if any replacements found)")
} else {
   println("-x (execute) flag not specified: no changes will be made to files")
}


//
//
//
def replacements = [
"2010-2011":"2010-2012"

//"org.apache.isis.runtimes.dflt.webapp.IsisWebAppBootstrapper":"org.apache.isis.core.webapp.IsisWebAppBootstrapper"
//,"org.apache.isis.runtimes.dflt.webapp.IsisSessionFilter":"org.apache.isis.core.webapp.IsisSessionFilter"
//,"org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionStrategyDefault":"org.apache.isis.core.webapp.auth.AuthenticationSessionStrategyDefault"
,
//,"oai.runtimes.dflt:runtime":"oai.core:isis-core-runtime"
//,"oai.runtimes.dflt:webapp":"oai.core:isis-core-runtime"
//,"oai.runtimes.dflt:webserver":"oai.core:isis-core-webserver"

//"<artifactId>isis-metamodel": "<artifactId>isis-core-metamodel"

 //"integteestsupport": "integtestsupport"
//,"unitteestsupport": "unittestsupport"
//,"0.3.1-SNAPSHOT": "1.0.0-SNAPSHOT"
//,"<artifactId>isis-applib": "<artifactId>isis-core-applib"
//,"<artifactId>isis-cglib-bytecode": "<artifactId>isis-core-bytecode-cglib"
//,"<artifactId>isis-javassist-bytecode": "<artifactId>isis-core-bytecode-javassist"
//,"<artifactId>isis-inmemory-objectstore": "<artifactId>isis-core-objectstore"
//,"<artifactId>isis-inmemory-profilestore": "<artifactId>isis-core-profilestore"
//,"<artifactId>isis-noop-security": "<artifactId>isis-core-security"
//,"<artifactId>isis-integtestsupport": "<artifactId>isis-core-integteestsupport"
//,"<artifactId>isis-runtime": "<artifactId>isis-core-runtime"
//,"<artifactId>isis-tck": "<artifactId>isis-core-tck"
//,"<artifactId>isis-unittestsupport": "<artifactId>isis-core-unitteestsupport"
//,"<artifactId>isis-webserver": "<artifactId>isis-core-webserver"
//,"<artifactId>isis-jdo-objectstore": "<artifactId>isis-objectstore-jdo"
//,"<artifactId>isis-nosql-objectstore": "<artifactId>isis-objectstore-nosql"
//,"<artifactId>isis-sql-objectstore": "<artifactId>isis-objectstore-sql"
//,"<artifactId>isis-xml-objectstore": "<artifactId>isis-objectstore-xml"
//,"<artifactId>isis-sql-profilestore": "<artifactId>isis-profilestore-sql"
//,"<artifactId>isis-xml-profilestore": "<artifactId>isis-profilestore-xml"
//,"<artifactId>isis-groovy-progmodel": "<artifactId>isis-progmodel-groovy"
//,"<artifactId>isis-wrapper-progmodel": "<artifactId>isis-progmodel-wrapper"
//,"<artifactId>isis-file-security": "<artifactId>isis-security-file"
//,"<artifactId>isis-ldap-security": "<artifactId>isis-security-ldap"
//,"<artifactId>isis-sql-security": "<artifactId>isis-security-sql"
//,"<artifactId>isis-bdd-viewer": "<artifactId>isis-viewer-bdd"
//,"<artifactId>isis-dnd-viewer": "<artifactId>isis-viewer-dnd"
//,"<artifactId>isis-junit-viewer": "<artifactId>isis-viewer-junit"
//,"<artifactId>isis-html-viewer": "<artifactId>isis-viewer-html"
//,"<artifactId>isis-restfulobjects-viewer": "<artifactId>isis-viewer-restfulobjects"
//,"<artifactId>isis-scimpi-viewer": "<artifactId>isis-viewer-scimpi"
//,"<artifactId>isis-wicket-viewer": "<artifactId>isis-viewer-wicket"
]



//
//
//
//def fileEndings = [".xml", ".java", ".launch", ".properties"]
def fileEndings = ["NOTICE"]



def currentDir = new File(".");

currentDir.eachFileRecurse { file ->
  def endsWithFileEnding = 
    fileEndings.inject(false) { endsWith, fileEnding -> 
      endsWith || file.name.endsWith(fileEnding) 
    }
  if (endsWithFileEnding) {
    def fileText = file.text;
    replacements.each { from, to -> 
      def newFileText = fileText.replaceAll(from, to)
      if(fileText != newFileText) {
        if(options.v || !options.x) {
            println(file.path)
        }
        if(options.x) {
            file.write(newFileText);
            fileText = newFileText;
        }
      }
    }    
  }
}
