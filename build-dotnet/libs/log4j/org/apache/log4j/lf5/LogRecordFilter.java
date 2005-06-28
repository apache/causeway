/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5;


/**
 * An interface for classes which filters LogRecords.  Implementations
 * represent a rule or condition which LogRecords may pass or fail.
 * @see LogRecord
 *
 * @author Richard Wan
 */

// Contributed by ThoughtWorks Inc.

public interface LogRecordFilter {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  /**
   * @return true if the specified LogRecord satisfies whatever condition
   * implementing class tests for.
   */
  public boolean passes(LogRecord record);

}

