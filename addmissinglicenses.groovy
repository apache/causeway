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

def cli = new CliBuilder( usage: 'groovy addmissinglicenses [-x]')
cli.x(argName: 'exec', longOpt: 'exec', 'execute (perform changes if any found)')

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


def fileEndings = [".xml"]
//def fileEndings = [".xml", ".shtml", ".css", ".java", ".sh", ".properties", ".groovy", ".allow", ".passwords"]

def licenseTextByFileEnding = [
	".java": license_using_c_style_comments,
	".groovy": license_using_c_style_comments,
	".css": license_using_c_style_comments,
	".xml": license_using_xml_comments,
    ".shtml": license_using_xml_comments,
	".properties": license_using_hash_comments,
    ".allow": license_using_hash_comments,
    ".passwords": license_using_hash_comments,
	".sh": license_using_hash_comments,
	]



def currentDir = new File(".");

currentDir.eachFileRecurse { file ->
  fileEndings.each { fileEnding ->
    if (! (file.canonicalPath =~ /[\\\/]target[\\\/]/)     && 
        ! (file.canonicalPath =~ /[\\\/]docbkx[\\\/]/)     &&  
        ! (file.canonicalPath =~ /[\\\/]\.settings[\\\/]/)    ) {
      if (file.name.endsWith(fileEnding)) {
        def fileText = file.text;

        def hasLicense = fileText.find(".*http://www.apache.org/licenses/LICENSE-2.0.*")
        if(hasLicense == null) {

          println file.canonicalPath

          // special handling for xml ... remove pragma if present
          if (fileEnding.endsWith(".xml")) {
            def sw = new StringWriter()
            file.filterLine(sw) { ! (it =~ /^\<\?xml/ ) }
            fileText = sw.toString()
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
