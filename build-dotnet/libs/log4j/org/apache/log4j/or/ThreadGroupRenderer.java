/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.or;

import org.apache.log4j.Layout;


/**
   Render {@link ThreadGroup} objects in a format similar to the
   information output by the {@link ThreadGroup#list} method.
   @author Ceki G&uuml;lc&uuml;
   @since 1.0 */
public class ThreadGroupRenderer implements ObjectRenderer {

  public
  ThreadGroupRenderer() {
  }
  
  /**
     Render a {@link ThreadGroup} object similar to the way that the
     {@link ThreadGroup#list} method output information. 

     <p>The output of a simple program consisting of one
     <code>main</code> thread is:
     <pre>
     java.lang.ThreadGroup[name=main, maxpri=10]
         Thread=[main,5,false]
     </pre>
     
     <p>The boolean value in thread information is the value returned
     by {@link Thread#isDaemon}.
     
  */
  public
  String  doRender(Object o) {
    if(o instanceof ThreadGroup) {
      StringBuffer sbuf = new StringBuffer();
      ThreadGroup tg = (ThreadGroup) o;
      sbuf.append("java.lang.ThreadGroup[name=");
      sbuf.append(tg.getName());
      sbuf.append(", maxpri=");
      sbuf.append(tg.getMaxPriority());
      sbuf.append("]");
      Thread[] t = new Thread[tg.activeCount()];
      tg.enumerate(t);
      for(int i = 0; i < t.length; i++) {
	sbuf.append(Layout.LINE_SEP);	
	sbuf.append("   Thread=[");
	sbuf.append(t[i].getName());
	sbuf.append(",");
	sbuf.append(t[i].getPriority());
	sbuf.append(",");
	sbuf.append(t[i].isDaemon());
	sbuf.append("]");
      }
      return sbuf.toString();
    } else {
      // this is the best we can do
      return o.toString();
    }    
  }
}  
