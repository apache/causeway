/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j;

import java.util.Hashtable;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.ThreadLocalMap;

/**
   The MDC class is similar to the {@link NDC} class except that it is
   based on a map instead of a stack. It provides <em>mapped
   diagnostic contexts</em>. A <em>Mapped Diagnostic Context</em>, or
   MDC in short, is an instrument for distinguishing interleaved log
   output from different sources. Log output is typically interleaved
   when a server handles multiple clients near-simultaneously.

   <p><b><em>The MDC is managed on a per thread basis</em></b>. A
   child thread automatically inherits a <em>copy</em> of the mapped
   diagnostic context of its parent.
  
   <p>The MDC class requires JDK 1.2 or above. Under JDK 1.1 the MDC
   will always return empty values but otherwise will not affect or
   harm your application.
   
   @since 1.2

   @author Ceki G&uuml;lc&uuml; */
public class MDC {
  
  final static MDC mdc = new MDC();
  
  static final int HT_SIZE = 7;

  boolean java1;
  
  Object tlm;
  
  private
  MDC() {
    java1 = Loader.isJava1();
    if(!java1) {
      tlm = new ThreadLocalMap();
    }
  }

  /**
     Put a context value (the <code>o</code> parameter) as identified
     with the <code>key</code> parameter into the current thread's
     context map.

     <p>If the current thread does not have a context map it is
     created as a side effect.
    
   */
  static
  public
  void put(String key, Object o) {
    mdc.put0(key, o);
  }
  
  /**
     Get the context identified by the <code>key</code> parameter.

     <p>This method has no side effects.
   */
  static 
  public
  Object get(String key) {
    return mdc.get0(key);
  }

  /**
     Remove the the context identified by the <code>key</code>
     parameter.

  */
  static 
  public
  void remove(String key) {
    mdc.remove0(key);
  }


  /**
   * Get the current thread's MDC as a hashtable. This method is
   * intended to be used internally.  
   * */
  public static Hashtable getContext() {
    return mdc.getContext0();
  }


  private
  void put0(String key, Object o) {
    if(java1) {
      return;
    } else {
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht == null) {
        ht = new Hashtable(HT_SIZE);
        ((ThreadLocalMap)tlm).set(ht);
      }    
      ht.put(key, o);
    }
  }
  
  private
  Object get0(String key) {
    if(java1) {
      return null;
    } else {       
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht != null && key != null) {
        return ht.get(key);
      } else {
        return null;
      }
    }
  }

  private
  void remove0(String key) {
    if(!java1) {
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht != null) {
        ht.remove(key);
      } 
    }
  }


  private
  Hashtable getContext0() {
     if(java1) {
      return null;
    } else {       
      return (Hashtable) ((ThreadLocalMap)tlm).get();
    }
  }
}
