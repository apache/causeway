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


def baseDir=new File("src/site/resources")
def iconsDir=new File(baseDir, "images/icons")
def iconPageDir=new File(baseDir, "icons")

def icons = []
def extensions = ["png","gif"]

def iconsByCategoryByPage = [:]


def getExtensionFromFilename(filename) {
  def m = (filename =~ /(\.[^\.]*)$/)
  if (m.size()>0) {
    return (m[0][0].size()>0 ? m[0][0].substring(1).trim().toLowerCase() : "")
  }
  ""
}

def getMap(map, key) {
  def value = map[key]
  if (!value) {
    value = [:]
    map[key] = value
  }
  value
}

def getList(map, key) {
  def value = map[key]
  if (!value) {
    value = []
    map[key] = value
  }
  value
}

def asList(list) {
  def buf = new StringBuilder()
  list.each {
    buf.append(it).append("/")
  }
  buf.deleteCharAt(buf.length()-1)
  buf.toString()
}

iconsDir.eachFileRecurse { file ->
  def fileExtension = getExtensionFromFilename(file.name)
  if (extensions.contains(fileExtension)) {
    def parts = file.path.split("[\\/\\\\]")
    def filePath = asList(parts[3..-1])
    parts = parts[5..-1] // strip off 'src/site/resources/images/icons'

    def pageName = parts[0]
    def iconSize = parts[1]

    parts = parts[2..-1] // strip off pageName & iconSize

    def hasCategory = parts.size() == 2
    def categoryName = hasCategory? parts[0]: "all"
    parts = parts[(hasCategory?1:0)..-1] // strip off category if exists
    def iconName = parts[0]

    def iconsByCategory = getMap(iconsByCategoryByPage, pageName)

    def iconsByName = getMap(iconsByCategory,categoryName)

    def iconList = getList(iconsByName, iconName)

    iconList.add(filePath)
  }
}

def appendImgRef(buf, fileName) {
  buf.append("<a href='").append(fileName).append("'>")
  buf.append("<img src='").append(fileName).append("'/>")
  buf.append("</a>")
  buf
}

def colsPerRow = [ haywood: 8, nogl: 12, tango: 8]

iconsByCategoryByPage.each { pageName, iconsByCategory ->
  def pageFile = new File(iconPageDir, pageName + ".html")
  println(pageFile.path)

  def numCols = colsPerRow[pageName]
  def buf = new StringBuilder()

  buf.append("""<!-- Licensed to the Apache Software Foundation (ASF) under one or more contributor 
    license agreements. See the NOTICE file distributed with this work for additional 
    information regarding copyright ownership. The ASF licenses this file to 
    you under the Apache License, Version 2.0 (the "License"); you may not use 
    this file except in compliance with the License. You may obtain a copy of 
    the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required 
    by applicable law or agreed to in writing, software distributed under the 
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS 
    OF ANY KIND, either express or implied. See the License for the specific 
    language governing permissions and limitations under the License. -->
<html><head><title>${pageName}</title></head><body>
<!-- START SNIPPET: icon-table -->
""")

  iconsByCategory.each { categoryName, iconsByName ->

    def numIcons = 0
    def rowStarted = false

    def categoryNameCapitalized = 
        categoryName[0].toUpperCase() + categoryName[1..-1]
    if (!categoryName.equals("all")) {
      buf.append("<p>${categoryNameCapitalized}</p>")
    }
    buf.append("<table>\n")

    iconsByName.each { iconName, iconList ->

      if(!rowStarted) {
        buf.append("  <tr>\n")
        rowStarted = true
      }
      
      buf.append("<td>")
      iconList.each {
        appendImgRef(buf, it)
      }
      buf.append("</td>")

      numIcons++
      if(numIcons%numCols==0) {
        buf.append("</tr>\n")
        rowStarted = false
      }
    }
    if (rowStarted) {
      def numCellsToCompleteRow = numIcons%numCols
      numCellsToCompleteRow.each {
        buf.append("<td/>")
      }
      buf.append("</tr>\n")
    }
    buf.append("</table>\n")
  }

  buf.append("""<!-- END SNIPPET: icon-table -->
</body></html>\n""")

  def pageFileText = buf.toString()
  pageFile.text = pageFileText
}


