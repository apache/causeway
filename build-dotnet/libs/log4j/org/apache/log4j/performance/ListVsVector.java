/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.performance;


import java.util.Vector;

/**

   Compares the performance of looping through a list versus a Vector.

   Chain looping is *20* times faster than vector access on JDK 1.1.7B on NT

*/
public class ListVsVector {

  static int RUN_LENGTH = 1000000;
  static Vector v = new Vector();
  static Chain head;
  static String tmp;

  static
  public 
  void main(String[] args) {

    v.addElement("aaa");
    v.addElement("bbb");
    v.addElement("ccc");
    v.addElement("ddd");
    v.addElement("eee");
    
    Chain c = new Chain("aaa");
    head = c;
    c.next = new Chain("bbb"); c = c.next;
    c.next = new Chain("ccc"); c = c.next;
    c.next = new Chain("ddd"); c = c.next;
    c.next = new Chain("eee");
    double t;
    t = loopChain();
    System.out.println("Looping thourgh the chain took " + t);

    t = loopVector();
    System.out.println("Looping thourgh the vector took " + t);

  }

  static
  double loopChain() {
    long before = System.currentTimeMillis();
    Chain c;
    for(int i = 0; i < RUN_LENGTH; i++) {
      c = head;
      while(c != null) {
	tmp = c.s;
	c = c.next;
      }
    }
    return (System.currentTimeMillis() - before)*1000.0/RUN_LENGTH;    
  }

  static
  double loopVector() {
    long before = System.currentTimeMillis();
    int size = v.size();
    for(int i = 0; i < RUN_LENGTH; i++) {
      for(int j = 0; j < size; j++)
	tmp = (String) v.elementAt(j);
    }
    return (System.currentTimeMillis() - before)*1000.0/RUN_LENGTH;    
  }

  static class Chain {
    public String s;
    public Chain next;

    Chain(String s) {
      this.s = s;
    }
    
    void setNext(Chain c) {
      next = c;
    }
  }
}
