def cli = new CliBuilder( usage: 'groovy replace [-xv]')
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
//"0.1.1-incubating-SNAPSHOT": "0.1.2-incubating-SNAPSHOT"

"org.apache.isis.profilestores.xml.XmlUserProfileStoreLoaderInstaller":"org.apache.isis.runtimes.dflt.profilestores.xml.XmlUserProfileStoreInstaller",
"org.apache.isis.profilestores.dflt.InMemoryUserProfileStoreInstaller":"org.apache.isis.runtimes.dflt.profilestores.dflt.InMemoryUserProfileStoreInstaller",
"org.apache.isis.runtimes.dflt.webapp.StaticContentFilter":"org.apache.isis.core.webapp.content.StaticContentFilter",
"org.apache.isis.runtimes.dflt.webapp.ResourceStreamSourceServletContext":"org.apache.isis.core.webapp.config.ResourceStreamSourceForWebInf",
"org.apache.isis.runtimes.dflt.webapp.ConfigurationBuilderServletContext":"org.apache.isis.core.webapp.config.ConfigurationBuilderForWebapp",
"org.apache.isis.runtimes.dflt.webapp.servlets.ForwardingServlet":"org.apache.isis.core.webapp.routing.ForwardingServlet",
"org.apache.isis.runtimes.dflt.webapp.servlets.RedirectServlet":"org.apache.isis.core.webapp.routing.RedirectServlet",
"org.apache.isis.runtimes.dflt.webapp.servlets.ResourceServlet":"org.apache.isis.core.webapp.content.ResourceServlet",
"org.apache.isis.runtimes.dflt.runtime.imageloader.awt.TemplateImageLoaderAwtInstaller":"org.apache.isis.core.runtime.imageloader.awt.TemplateImageLoaderAwtInstaller",
"org.apache.isis.runtimes.dflt.runtime.imageloader.TemplateImageLoaderNoopInstaller":"org.apache.isis.core.runtime.imageloader.noop.TemplateImageLoaderNoopInstaller"
]



//
//
//
def fileEndings = [".xml", ".java", ".launch", ".properties"]
//def fileEndings = ["pom.xml"]



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
        if(options.v) {
            println(file.path)
        }
        if(options.x) {
            file.write(newFileText);
        }
      }
    }    
  }
}
