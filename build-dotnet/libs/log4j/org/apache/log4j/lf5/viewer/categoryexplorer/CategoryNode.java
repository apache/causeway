/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

/**
 * CategoryNode
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public class CategoryNode extends DefaultMutableTreeNode {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------
  protected boolean _selected = true;
  protected int _numberOfContainedRecords = 0;
  protected int _numberOfRecordsFromChildren = 0;
  protected boolean _hasFatalChildren = false;
  protected boolean _hasFatalRecords = false;

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  /**
   *
   */
  public CategoryNode(String title) {
    setUserObject(title);
  }

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------
  public String getTitle() {
    return (String) getUserObject();
  }

  public void setSelected(boolean s) {
    if (s != _selected) {
      _selected = s;
    }
  }

  public boolean isSelected() {
    return _selected;
  }

  /**
   * @deprecated
   */
  public void setAllDescendantsSelected() {
    Enumeration children = children();
    while (children.hasMoreElements()) {
      CategoryNode node = (CategoryNode) children.nextElement();
      node.setSelected(true);
      node.setAllDescendantsSelected();
    }
  }

  /**
   * @deprecated
   */
  public void setAllDescendantsDeSelected() {
    Enumeration children = children();
    while (children.hasMoreElements()) {
      CategoryNode node = (CategoryNode) children.nextElement();
      node.setSelected(false);
      node.setAllDescendantsDeSelected();
    }
  }

  public String toString() {
    return (getTitle());
  }

  public boolean equals(Object obj) {
    if (obj instanceof CategoryNode) {
      CategoryNode node = (CategoryNode) obj;
      String tit1 = getTitle().toLowerCase();
      String tit2 = node.getTitle().toLowerCase();

      if (tit1.equals(tit2)) {
        return (true);
      }
    }
    return (false);
  }

  public int hashCode() {
    return (getTitle().hashCode());
  }

  public void addRecord() {
    _numberOfContainedRecords++;
    addRecordToParent();
  }

  public int getNumberOfContainedRecords() {
    return _numberOfContainedRecords;
  }

  public void resetNumberOfContainedRecords() {
    _numberOfContainedRecords = 0;
    _numberOfRecordsFromChildren = 0;
    _hasFatalRecords = false;
    _hasFatalChildren = false;
  }

  public boolean hasFatalRecords() {
    return _hasFatalRecords;
  }

  public boolean hasFatalChildren() {
    return _hasFatalChildren;
  }

  public void setHasFatalRecords(boolean flag) {
    _hasFatalRecords = flag;
  }

  public void setHasFatalChildren(boolean flag) {
    _hasFatalChildren = flag;
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  protected int getTotalNumberOfRecords() {
    return getNumberOfRecordsFromChildren() + getNumberOfContainedRecords();
  }

  /**
   * Passes up the addition from child to parent
   */
  protected void addRecordFromChild() {
    _numberOfRecordsFromChildren++;
    addRecordToParent();
  }

  protected int getNumberOfRecordsFromChildren() {
    return _numberOfRecordsFromChildren;
  }

  protected void addRecordToParent() {
    TreeNode parent = getParent();
    if (parent == null) {
      return;
    }
    ((CategoryNode) parent).addRecordFromChild();
  }
  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}






