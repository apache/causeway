/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.apache.log4j.helpers.LogLog;

public class SAXErrorHandler implements ErrorHandler {

  public
  void error(SAXParseException ex) {
    LogLog.error("Parsing error on line "+ex.getLineNumber()+" and column "
		 +ex.getColumnNumber());
    LogLog.error(ex.getMessage(), ex.getException());
    //LogLog.error("pid="+ex.getPublicId()+" sid="+ex.getSystemId());
  }
  
  public
  void fatalError(SAXParseException ex) {
    error(ex);
  }
   
  public
  void warning(SAXParseException ex) {
    LogLog.warn("Parsing error on line "+ex.getLineNumber()+" and column "
		+ex.getColumnNumber());
    LogLog.warn(ex.getMessage(), ex.getException());
  }


}
