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
cli.x(argName: 'exec', longOpt: 'exec', 'execute (perform changes if any found)')
cli.v(argName: 'verbose', longOpt: 'verbose', 'verbose')

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
"0.3.0-incubating-SNAPSHOT": "0.3.1-incubating-SNAPSHOT"

]



//
//
//
//def fileEndings = [".xml", ".java", ".launch", ".properties"]
def fileEndings = [".xml"]



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
        }
      }
    }    
  }
}
