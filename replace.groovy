def currentDir = new File(".");

def packages = [
//"org.apache.isis.core.runtime": "org.apache.isis.runtimes.dflt.runtime"
// "org.apache.isis.core.webapp": "org.apache.isis.runtimes.dflt.webapp"
//,"org.apache.isis.core.webserver": "org.apache.isis.runtimes.dflt.webserver"
//,"org.apache.isis.defaults.objectstore": "org.apache.isis.runtimes.dflt.objectstores.dflt"

//"org.apache.isis.defaults.bytecode": "org.apache.isis.runtimes.dflt.bytecode.dflt"
//,"org.apache.isis.alternatives.bytecode.javassist": "org.apache.isis.runtimes.dflt.bytecode.javassist"
//,"org.apache.isis.alternatives.bytecode.identity": "org.apache.isis.runtimes.dflt.bytecode.identity"

// "org.apache.isis.alternatives.objectstore.xml": "org.apache.isis.runtimes.dflt.objectstores.xml"
// "org.apache.isis.alternatives.objectstore.nosql": "org.apache.isis.runtimes.dflt.objectstores.nosql"
// "org.apache.isis.alternatives.objectstore.sql": "org.apache.isis.runtimes.dflt.objectstores.sql"
// "org.apache.isis.alternatives.remoting": "org.apache.isis.runtimes.dflt.remoting"

//"org.apache.isis.alternatives.embedded": "org.apache.isis.runtimes.embedded"
 //"org.apache.isis.defaults.security" : "org.apache.isis.security.dflt"
//"org.apache.isis.defaults.profilestore": "org.apache.isis.profilestores.dflt"
// ,"org.apache.isis.alternatives.profilestore.xml": "org.apache.isis.profilestores.xml"

// ,"org.apache.isis.alternatives.security.file": "org.apache.isis.security.file"
// ,"org.apache.isis.alternatives.security.ldap": "org.apache.isis.security.ldap"
// "org.apache.isis.defaults.progmodel": "org.apache.isis.progmodels.dflt"

  // "org.apache.isis.defaults.bytecode": "org.apache.isis.runtimes.dflt.bytecode.dflt"
  // ,"org.apache.isis.alternatives.bytecode": "org.apache.isis.runtimes.dflt.bytecode"
]

//def fileEndings = ["web.xml", ".java", ".launch", ".properties"]

def fileEndings = []

currentDir.eachFileRecurse { file ->
  def endsWithFileEnding = 
    fileEndings.inject(false) { endsWith, fileEnding -> 
      endsWith || file.name.endsWith(fileEnding) 
    }
  if (endsWithFileEnding) {
    def fileText = file.text;
    packages.each { from, to -> 
      def newFileText = fileText.replaceAll(from, to)
      if(fileText != newFileText) {
        println(file.path)
        file.write(newFileText);
      }
    }    
  }
}
