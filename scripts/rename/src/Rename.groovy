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
 *
 */

/**
 * Renames project from 'isis' to '...'
 *
 * usage:
 * <pre>
 *    pushd scripts/rename/src
 *    groovy Rename.groovy
 *    popd
 * </pre>
 *
 * <p>
 * This doesn't rewrite the contents of the following (binary) extensions:
 *
 * <ul>
 *     <li>.jar
 *     <li> .zip
 *     <li> .pptx
 *     <li> .docx
 *     <li> .xlsx
 *     <li> .odt
 *     <li> .rtf
 *     <li> .pdf
 * </ul>
 */
class Rename {

  // root of the repo
  static final ROOT_DIR = "../../.."
  //static final ROOT_DIR = "../../.." + "/persistence/jdo/metamodel/src/main/resources/META-INF/services"

  static final FROM_LOWERCASE = "isis"
  static final TO_LOWERCASE = "causeway"

  static final FROM_TITLECASE = FROM_LOWERCASE.capitalize()
  static final TO_TITLECASE = TO_LOWERCASE.capitalize()

  static final FROM_UPPERCASE = FROM_LOWERCASE.toUpperCase()
  static final TO_UPPERCASE = TO_LOWERCASE.toUpperCase()


  // to obtain all the suffixes:
  // find . -type f | sed -rn 's|.*/[^/]+\.([^/.]+)$|\1|p' | sort -u
  static final EXTENSIONS = [
          "NOTICE",
          "STATUS",
          "ConstraintValidator",
          "CausewayBeanTypeClassifier",
          "MF",
          "TXT",
          "UriBuilderPlugin",
          "adoc",
          "bat",
          "cfg",
          "css",
          "dcl",
          "dtd",
          "factories",
          "feature",
          "fxml",
          "gql",
          "graphqls",
          "hbs",
          "html",
          "importorder",
          "info",
          "ini",
          "java",
          "jdo",
          "js",
          "json",
          "kt",
          "kts",
          "ldif",
          "list",
          "md",
          "po",
          "pot",
          "properties",
          "puml",
          "rdf",
          "sh",
          "soc",
          "svg",
          "template",
          "thtml",
          "ts",
          "txt",
          "xml",
          "xsd",
          "yaml",
          "yml"
  ]

  def renameAllFiles() {

    def currentDir = new File(ROOT_DIR)

    currentDir.eachFileRecurse { File file ->
      if (file.isDirectory()) {
        return
      }
      renameIfRequired(file)
    }
  }

  def renameIfRequired(File file) {
    def filePathRename = file.path
            .replaceAll(FROM_TITLECASE, TO_TITLECASE)
            .replaceAll(FROM_UPPERCASE, TO_UPPERCASE)
            .replaceAll("\\\\" + FROM_LOWERCASE, "\\\\" + TO_LOWERCASE)
            .replaceAll("/" + FROM_LOWERCASE, "/" + TO_LOWERCASE)
            .replaceAll("-" + FROM_LOWERCASE, "-" + TO_LOWERCASE)
            .replaceAll("_" + FROM_LOWERCASE, "_" + TO_LOWERCASE)
            .replaceAll("[.]" + FROM_LOWERCASE, "." + TO_LOWERCASE)

    if (!filePathRename.equals(file.path)) {

      println "${file.path} -> ${filePathRename}"

      def fileRename = new File(filePathRename)
      def parentFile = fileRename.parentFile
      parentFile.mkdirs()

      file.renameTo(fileRename)
    }
  }

  def rewriteAllFileContents() {

    def currentDir = new File(ROOT_DIR)

    currentDir.eachFileRecurse { file ->
      // println "${file.path}"
      if (!file.exists()) {
        return
      }
      if (file.isDirectory()) {
        return
      }
      if (file.path.contains(".git\\" )) {
        return
      }

      if(fileNameEndsWithSupportedExtension(file)) {
        rewriteIfRequired(file)
      }
    }
  }

  private void rewriteIfRequired(File file) {
    def fileText = file.text
    def updatedFileText = fileText
            .replaceAll(FROM_LOWERCASE, TO_LOWERCASE)
            .replaceAll(FROM_UPPERCASE, TO_UPPERCASE)
            .replaceAll(FROM_TITLECASE, TO_TITLECASE)

    if (!fileText.equals(updatedFileText)) {

      println "rewriting ${file.path}"

      file.write(updatedFileText)
    }
  }


  private boolean fileNameEndsWithSupportedExtension(File file) {
    for (ext in EXTENSIONS) {
      if (file.name.endsWith(ext)) {
        return true
      }
    }
    return false
  }

}

static void main(String[] args) {
  def rr = new Rename()

  rr.renameAllFiles()
  rr.rewriteAllFileContents()
}


