/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */

package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * Default implementation of TreeModelListener which does nothing.
 *
 * @author Richard Wan
 */

// Contributed by ThoughtWorks Inc.

public class TreeModelAdapter implements TreeModelListener {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------
  public void treeNodesChanged(TreeModelEvent e) {
  }

  public void treeNodesInserted(TreeModelEvent e) {
  }

  public void treeNodesRemoved(TreeModelEvent e) {
  }

  public void treeStructureChanged(TreeModelEvent e) {
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces
  //--------------------------------------------------------------------------
}

