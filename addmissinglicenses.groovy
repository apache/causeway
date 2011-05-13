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

def cli = new CliBuilder( usage: 'groovy replace [-xv]')
cli.x(argName: 'exec', longOpt: 'exec', 'execute (perform changes if any found)'
)
cli.v(argName: 'verbose', longOpt: 'verbose', 'verbose')

def options=cli.parse(args)

if(options.x) {
   println("-x (execute) flag specified: will make changes to files (if any replacements found)")
} else {
   println("-x (execute) flag not specified: no changes will be made to files")
}


def license_using_c_style_comments="""/*
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
"""

def license_using_xml_comments="""<!--
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

def license_using_hash_comments="""#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#  
#         http://www.apache.org/licenses/LICENSE-2.0
#         
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
"""


//def fileEndings = [".java"]
def fileEndings = [".xml", ".css", ".java", ".sh", ".properties", ".groovy"]

def licenseTextByFileEnding = [
	".java": license_using_c_style_comments,
	".groovy": license_using_c_style_comments,
	".css": license_using_c_style_comments,
	".xml": license_using_xml_comments,
	".properties": license_using_hash_comments,
	".sh": license_using_hash_comments,
	]



def currentDir = new File(".");

currentDir.eachFileRecurse { file ->
  fileEndings.each { fileEnding ->
    if (! (file.canonicalPath =~ /[\\\/]target[\\\/]/)) {
      if (file.name.endsWith(fileEnding)) {
        def fileText = file.text;

        def matchingText = fileText.find(".*Licensed to the Apache Software Foundation.*")
        if(matchingText == null) {

          if(options.v || ! options.x) {
            println file.canonicalPath
          }

          if(options.x) {
            file.write(licenseTextByFileEnding[fileEnding])
            file.append(fileText)
          }

        }
      }    
    }
  }
}
