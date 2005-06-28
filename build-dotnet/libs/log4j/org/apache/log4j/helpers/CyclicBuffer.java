/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
     
package org.apache.log4j.helpers;

import org.apache.log4j.spi.LoggingEvent;

/**

   CyclicBuffer is used by other appenders to hold {@link LoggingEvent
   LoggingEvents} for immediate or differed display.
   
   <p>This buffer gives read access to any element in the buffer not
   just the first or last element.

   @author Ceki G&uuml;lc&uuml;
   @since 0.9.0

 */
public class CyclicBuffer {
  
  LoggingEvent[] ea;
  int first; 
  int last; 
  int numElems;
  int maxSize;

  /**
     Instantiate a new CyclicBuffer of at most <code>maxSize</code> events.

     The <code>maxSize</code> argument must a positive integer.

     @param maxSize The maximum number of elements in the buffer.
  */
  public CyclicBuffer(int maxSize) throws IllegalArgumentException {
    if(maxSize < 1) {
      throw new IllegalArgumentException("The maxSize argument ("+maxSize+
			    ") is not a positive integer.");
    }
    this.maxSize = maxSize;
    ea = new LoggingEvent[maxSize];
    first = 0;
    last = 0;
    numElems = 0;
  }
    
  /**
     Add an <code>event</code> as the last event in the buffer.

   */
  public
  void add(LoggingEvent event) {    
    ea[last] = event;    
    if(++last == maxSize)
      last = 0;

    if(numElems < maxSize)
      numElems++;
    else if(++first == maxSize)
      first = 0;
  }


  /**
     Get the <i>i</i>th oldest event currently in the buffer. If
     <em>i</em> is outside the range 0 to the number of elements
     currently in the buffer, then <code>null</code> is returned.


  */
  public
  LoggingEvent get(int i) {
    if(i < 0 || i >= numElems)
      return null;

    return ea[(first + i) % maxSize];
  }

  public 
  int getMaxSize() {
    return maxSize;
  }

  /**
     Get the oldest (first) element in the buffer. The oldest element
     is removed from the buffer.
  */
  public
  LoggingEvent get() {
    LoggingEvent r = null;
    if(numElems > 0) {
      numElems--;
      r = ea[first];
      ea[first] = null;
      if(++first == maxSize)
	first = 0;
    } 
    return r;
  }
  
  /**
     Get the number of elements in the buffer. This number is
     guaranteed to be in the range 0 to <code>maxSize</code>
     (inclusive).
  */
  public
  int length() {
    return numElems;
  } 

  /**
     Resize the cyclic buffer to <code>newSize</code>.

     @throws IllegalArgumentException if <code>newSize</code> is negative.
   */
  public 
  void resize(int newSize) {
    if(newSize < 0) {
      throw new IllegalArgumentException("Negative array size ["+newSize+
					 "] not allowed.");
    }
    if(newSize == numElems)
      return; // nothing to do
    
    LoggingEvent[] temp = new  LoggingEvent[newSize];

    int loopLen = newSize < numElems ? newSize : numElems;
    
    for(int i = 0; i < loopLen; i++) {
      temp[i] = ea[first];
      ea[first] = null;
      if(++first == numElems) 
	first = 0;
    }
    ea = temp;
    first = 0;
    numElems = loopLen;
    maxSize = newSize;
    if (loopLen == newSize) {
      last = 0;
    } else {
      last = loopLen;
    }
  }
}
