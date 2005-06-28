/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.performance;

import java.io.Writer;
import java.io.IOException;

/**
 * <p>Extends {@link Writer} with methods that return immediately
 * without doing anything. This class is used to measure the cost of
 * constructing a log message but not actually writing to any device.
 * </p>

 * <p><b> <font color="#FF2222">The
 * <code>org.apache.log4j.performance.NOPWriter</code> class is
 * intended for internal use only.</font> Consequently, it is not
 * included in the <em>log4j.jar</em> file.</b> </p>
 *  
 * @author Ceki G&uuml;lc&uuml; 
 * */
public class NOPWriter extends Writer {

  public void write(char[] cbuf) throws IOException {}

  public void write(char[] cbuf, int off, int len) throws IOException {}


  public void write(int b) throws IOException {}

  public void write(String s) throws IOException {} 

  public void write(String s, int off, int len) throws IOException {} 

  public void flush() throws IOException {
  }

  public void close() throws IOException {
    System.err.println("Close called.");
  }
}
