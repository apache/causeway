/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.config;

/**
 * Thrown when an error is encountered whilst attempting to set a property
 * using the {@link PropertySetter} utility class.
 * 
 * @author Anders Kristensen
 * @since 1.1
 */
public class PropertySetterException extends Exception {
  protected Throwable rootCause;
  
  public
  PropertySetterException(String msg) {
    super(msg);
  }
  
  public
  PropertySetterException(Throwable rootCause)
  {
    super();
    this.rootCause = rootCause;
  }
  
  /**
     Returns descriptive text on the cause of this exception.
   */
  public
  String getMessage() {
    String msg = super.getMessage();
    if (msg == null && rootCause != null) {
      msg = rootCause.getMessage();
    }
    return msg;
  }
}
