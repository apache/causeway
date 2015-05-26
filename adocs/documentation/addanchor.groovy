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

def cli = new CliBuilder( usage: 'groovy addanchor [-x]')
cli.x(argName: 'exec', longOpt: 'exec', 'execute (perform changes if any found)')

def options=cli.parse(args)

if(options.x) {
   println("-x (execute) flag specified: will update anchors")
} else {
   println("-x (execute) flag not specified: no changes will be made")
}

def currentDir = new File(".");

currentDir.eachFileRecurse { file ->
  if (! (file.canonicalPath =~ /[\\\/]target[\\\/]/)     && 
      ! (file.canonicalPath =~ /[\\\/]target-ide[\\\/]/)    ) {

    def basename = file.name
    def dirname = file.parent

    if (basename.endsWith(".adoc") && !dirname.endsWith("\\__")) {

      // derive anchor
      def lastIndex = basename.lastIndexOf(".");
      def anchor = basename.substring(0,lastIndex);

      def fileText = file.text;

      // strip any existing anchor if required
      def hasAnchor = fileText.find("^\\[\\[.+\\]\\]")
      if(hasAnchor != null) {
        def sw = new StringWriter()
        file.filterLine(sw) { ! (it =~ /^\[\[.+\]\]/ ) }
        fileText = sw.toString()
      }

      // write out new
      println file.canonicalPath

      if(options.x) {
        file.write("[[" + anchor + "]]\n");
        file.append(fileText)
      }
    }    
  }
}
