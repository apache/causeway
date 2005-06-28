/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.helpers;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
   
  An always-empty Enumerator.

  @author Anders Kristensen
  @since version 1.0
 */
public class NullEnumeration implements Enumeration {
  private static final NullEnumeration instance = new NullEnumeration();
  
  private
  NullEnumeration() {
  }
  
  public static NullEnumeration getInstance() {
    return instance;
  }
  
  public
  boolean hasMoreElements() {
    return false;
  }
  
  public
  Object nextElement() {
    throw new NoSuchElementException();
  }
}
