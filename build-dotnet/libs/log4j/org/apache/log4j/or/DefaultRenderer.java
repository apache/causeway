/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.or;

/**
   The default Renderer renders objects by calling their
   <code>toString</code> method.

   @author Ceki G&uuml;lc&uuml;
   @since 1.0 */
class DefaultRenderer implements ObjectRenderer {
  
  DefaultRenderer() {
  }

  /**
     Render the object passed as parameter by calling its
     <code>toString</code> method.  */
  public
  String doRender(Object o) {
    return o.toString();
  }
}  
