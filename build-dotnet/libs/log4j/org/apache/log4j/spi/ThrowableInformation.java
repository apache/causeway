/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.spi;

import java.io.Writer;
import java.io.PrintWriter;
import java.util.Vector;

/**
  * ThrowableInformation is log4j's internal representation of
  * throwables. It essentially consists of a string array, called
  * 'rep', where the first element, that is rep[0], represents the
  * string representation of the throwable (i.e. the value you get
  * when you do throwable.toString()) and subsequent elements
  * correspond the stack trace with the top most entry of the stack
  * corresponding to the second entry of the 'rep' array that is
  * rep[1].
  *
  * @author Ceki G&uuml;lc&uuml;
  *
  * */
public class ThrowableInformation implements java.io.Serializable {

  static final long serialVersionUID = -4748765566864322735L;

  private transient Throwable throwable;
  private String[] rep;
  
  public
  ThrowableInformation(Throwable throwable) {
    this.throwable = throwable;
  }

  public
  Throwable getThrowable() {
    return throwable;
  }
  
  public
  String[] getThrowableStrRep() {
    if(rep != null) {
      return (String[]) rep.clone();
    } else {
      VectorWriter vw = new VectorWriter();
      throwable.printStackTrace(vw);
      rep = vw.toStringArray();
      return rep;
    }
  }
}

/**
  * VectorWriter is a seemingly trivial implemtantion of PrintWriter.
  * The throwable instance that we are trying to represnt is asked to
  * print itself to a VectorWriter. 
  *
  * By our design choice, r string representation of the throwable
  * does not contain any line separators. It follows that println()
  * methods of VectorWriter ignore the 'ln' part.
  * */
class VectorWriter extends PrintWriter {
    
  private Vector v;
  
  VectorWriter() {
    super(new NullWriter());
    v = new Vector();
  }

  public void print(Object o) {      
    v.addElement(o.toString());
  }
  
  public void print(char[] chars) {
    v.addElement(new String(chars));
  }
  
  public void print(String s) {
    v.addElement(s);
  }

  public void println(Object o) {      
    v.addElement(o.toString());
  }
  
  // JDK 1.1.x apprenly uses this form of println while in
  // printStackTrace()
  public
  void println(char[] chars) {
    v.addElement(new String(chars));
  }
  
  public  
  void println(String s) {
    v.addElement(s);
  }

  public void write(char[] chars) {
    v.addElement(new String(chars));
  }

  public void write(char[] chars, int off, int len) {
    v.addElement(new String(chars, off, len));
  }

  public void write(String s, int off, int len) {
    v.addElement(s.substring(off, off+len));
  }

  public void write(String s) {
     v.addElement(s);
  }

  public String[] toStringArray() {
    int len = v.size();
    String[] sa = new String[len];
    for(int i = 0; i < len; i++) {
      sa[i] = (String) v.elementAt(i);
    }
    return sa;
  }

}  

class NullWriter extends Writer {    
  
  public void close() {
    // blank
  }

  public void flush() {
    // blank
  }

  public void write(char[] cbuf, int off, int len) {
    // blank
  }
}

