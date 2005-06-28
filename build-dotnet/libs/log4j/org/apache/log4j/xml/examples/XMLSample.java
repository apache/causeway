
package org.apache.log4j.xml.examples;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Category;
import java.net.*;

/**

   This <a href="doc-files/XMLSample.java">example code</a> shows how to
   read an XML based configuration file using a DOM parser.

   <p>Sample XML files <a href="doc-files/sample1.xml">sample1.xml</a>
   and <a href="doc-files/sample2.xml">sample2.xml</a> are provided.


   <p>Note that the log4j.dtd is not in the local directory.
   It is found by the class loader.

   @author Ceki G&uuml;lc&uuml;

*/
public class XMLSample {

  static Category cat = Category.getInstance(XMLSample.class.getName());

  public
  static
  void main(String argv[]) {

    if(argv.length == 1)
      init(argv[0]);
    else
      Usage("Wrong number of arguments.");
    sample();
  }

  static
  void Usage(String msg) {
    System.err.println(msg);
    System.err.println( "Usage: java " + XMLSample.class.getName() +
			"configFile");
    System.exit(1);
  }

  static
  void init(String configFile) {
    DOMConfigurator.configure(configFile);
  }

  static
  void sample() {
    int i = -1;
    Category root = Category.getRoot();
    cat.debug("Message " + ++i);
    cat.warn ("Message " + ++i);
    cat.error("Message " + ++i);
    Exception e = new Exception("Just testing");
    cat.debug("Message " + ++i, e);
  }
}
