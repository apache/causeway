
package org.apache.log4j.xml.examples;

import org.apache.log4j.helpers.LogLog;

/**

   This class is needed for validating a log4j.dtd derived XML file.

   @author Joe Kesselman

   @since 0.8.3
   
 */
public class ReportParserError implements org.xml.sax.ErrorHandler {
  
  void report(String msg, org.xml.sax.SAXParseException e) {
    LogLog.error(msg+e.getMessage()+ "\n\tat line="+ e.getLineNumber()+
		 " col="+e.getColumnNumber()+ " of "+
		 "SystemId=\""+e.getSystemId()+
		 "\" PublicID = \""+e.getPublicId()+'\"');
  }
   
  public void warning(org.xml.sax.SAXParseException e) {
    report("WARNING: ", e);
  }
   
  public void error(org.xml.sax.SAXParseException e) {
    report("ERROR: ", e);
  }
   
  public void fatalError(org.xml.sax.SAXParseException e) {
    report("FATAL: ", e);
  }
}
