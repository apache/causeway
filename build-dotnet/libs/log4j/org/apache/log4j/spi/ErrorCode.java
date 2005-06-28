/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.spi;


/**
   This interface defines commonly encoutered error codes.

   @author Ceki G&uuml;lc&uuml;
   @since 0.9.0
 */
public interface ErrorCode {

  public final int GENERIC_FAILURE = 0;
  public final int WRITE_FAILURE = 1;
  public final int FLUSH_FAILURE = 2;
  public final int CLOSE_FAILURE = 3;
  public final int FILE_OPEN_FAILURE = 4;
  public final int MISSING_LAYOUT = 5;
  public final int ADDRESS_PARSE_FAILURE = 6;
}
