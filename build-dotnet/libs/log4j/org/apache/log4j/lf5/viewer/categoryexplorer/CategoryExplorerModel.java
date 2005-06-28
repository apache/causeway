/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer.categoryexplorer;

import org.apache.log4j.lf5.LogRecord;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/**
 * CategoryExplorerModel
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brent Sprecher
 * @author Richard Hurst
 */

// Contributed by ThoughtWorks Inc.

public class CategoryExplorerModel extends DefaultTreeModel {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------

  protected boolean _renderFatal = true;
  protected ActionListener _listener = null;
  protected ActionEvent _event = new ActionEvent(this,
      ActionEvent.ACTION_PERFORMED,
      "Nodes Selection changed");

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  public CategoryExplorerModel(CategoryNode node) {
    super(node);
  }
  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  public void addLogRecord(LogRecord lr) {
    CategoryPath path = new CategoryPath(lr.getCategory());
    addCategory(path); // create category path if it is new
    CategoryNode node = getCategoryNode(path);
    node.addRecord(); // update category node
    if (_renderFatal && lr.isFatal()) {
      TreeNode[] nodes = getPathToRoot(node);
      int len = nodes.length;
      CategoryNode parent;

      // i = 0 gives root node
      // skip node and root, loop through "parents" in between
      for (int i = 1; i < len - 1; i++) {
        parent = (CategoryNode) nodes[i];
        parent.setHasFatalChildren(true);
        nodeChanged(parent);
      }
      node.setHasFatalRecords(true);
      nodeChanged(node);
    }
  }

  public CategoryNode getRootCategoryNode() {
    return (CategoryNode) getRoot();
  }

  public CategoryNode getCategoryNode(String category) {
    CategoryPath path = new CategoryPath(category);
    return (getCategoryNode(path));
  }

  /**
   * returns null if no CategoryNode exists.
   */
  public CategoryNode getCategoryNode(CategoryPath path) {
    CategoryNode root = (CategoryNode) getRoot();
    CategoryNode parent = root; // Start condition.

    for (int i = 0; i < path.size(); i++) {
      CategoryElement element = path.categoryElementAt(i);

      // If the two nodes have matching titles they are considered equal.
      Enumeration children = parent.children();

      boolean categoryAlreadyExists = false;
      while (children.hasMoreElements()) {
        CategoryNode node = (CategoryNode) children.nextElement();
        String title = node.getTitle().toLowerCase();

        String pathLC = element.getTitle().toLowerCase();
        if (title.equals(pathLC)) {
          categoryAlreadyExists = true;
          // This is now the new parent node.
          parent = node;
          break; // out of the while, and back to the for().
        }
      }

      if (categoryAlreadyExists == false) {
        return null; // Didn't find the Node.
      }
    }

    return (parent);
  }

  /**
   * @return true if all the nodes in the specified CategoryPath are
   * selected.
   */
  public boolean isCategoryPathActive(CategoryPath path) {
    CategoryNode root = (CategoryNode) getRoot();
    CategoryNode parent = root; // Start condition.
    boolean active = false;

    for (int i = 0; i < path.size(); i++) {
      CategoryElement element = path.categoryElementAt(i);

      // If the two nodes have matching titles they are considered equal.
      Enumeration children = parent.children();

      boolean categoryAlreadyExists = false;
      active = false;

      while (children.hasMoreElements()) {
        CategoryNode node = (CategoryNode) children.nextElement();
        String title = node.getTitle().toLowerCase();

        String pathLC = element.getTitle().toLowerCase();
        if (title.equals(pathLC)) {
          categoryAlreadyExists = true;
          // This is now the new parent node.
          parent = node;

          if (parent.isSelected()) {
            active = true;
          }

          break; // out of the while, and back to the for().
        }
      }

      if (active == false || categoryAlreadyExists == false) {
        return false;
      }
    }

    return (active);
  }


  /**
   * <p>Method altered by Richard Hurst such that it returns the CategoryNode
   * corresponding to the CategoryPath</p>
   *
   * @param CategoryPath
   * @returns CategoryNode
   */
  public CategoryNode addCategory(CategoryPath path) {
    CategoryNode root = (CategoryNode) getRoot();
    CategoryNode parent = root; // Start condition.

    for (int i = 0; i < path.size(); i++) {
      CategoryElement element = path.categoryElementAt(i);

      // If the two nodes have matching titles they are considered equal.
      Enumeration children = parent.children();

      boolean categoryAlreadyExists = false;
      while (children.hasMoreElements()) {
        CategoryNode node = (CategoryNode) children.nextElement();
        String title = node.getTitle().toLowerCase();

        String pathLC = element.getTitle().toLowerCase();
        if (title.equals(pathLC)) {
          categoryAlreadyExists = true;
          // This is now the new parent node.
          parent = node;
          break;
        }
      }

      if (categoryAlreadyExists == false) {
        // We need to add the node.
        CategoryNode newNode = new CategoryNode(element.getTitle());

        //This method of adding a new node cause parent roots to be
        // collapsed.
        //parent.add( newNode );
        //reload(parent);

        // This doesn't force the nodes to collapse.
        insertNodeInto(newNode, parent, parent.getChildCount());
        refresh(newNode);

        // The newly added node is now the parent.
        parent = newNode;

      }
    }

    return parent;
  }

  public void update(CategoryNode node, boolean selected) {
    if (node.isSelected() == selected) {
      return; // nothing was changed, nothing to do
    }
    // select parents or deselect children
    if (selected) {
      setParentSelection(node, true);
    } else {
      setDescendantSelection(node, false);
    }
  }

  public void setDescendantSelection(CategoryNode node, boolean selected) {
    Enumeration descendants = node.depthFirstEnumeration();
    CategoryNode current;
    while (descendants.hasMoreElements()) {
      current = (CategoryNode) descendants.nextElement();
      // does the current node need to be changed?
      if (current.isSelected() != selected) {
        current.setSelected(selected);
        nodeChanged(current);
      }
    }
    notifyActionListeners();
  }

  public void setParentSelection(CategoryNode node, boolean selected) {
    TreeNode[] nodes = getPathToRoot(node);
    int len = nodes.length;
    CategoryNode parent;

    // i = 0 gives root node, i=len-1 gives this node
    // skip the root node
    for (int i = 1; i < len; i++) {
      parent = (CategoryNode) nodes[i];
      if (parent.isSelected() != selected) {
        parent.setSelected(selected);
        nodeChanged(parent);
      }
    }
    notifyActionListeners();
  }


  public synchronized void addActionListener(ActionListener l) {
    _listener = AWTEventMulticaster.add(_listener, l);
  }

  public synchronized void removeActionListener(ActionListener l) {
    _listener = AWTEventMulticaster.remove(_listener, l);
  }

  public void resetAllNodeCounts() {
    Enumeration nodes = getRootCategoryNode().depthFirstEnumeration();
    CategoryNode current;
    while (nodes.hasMoreElements()) {
      current = (CategoryNode) nodes.nextElement();
      current.resetNumberOfContainedRecords();
      nodeChanged(current);
    }
  }

  /**
   * <p>Returns the CategoryPath to the specified CategoryNode</p>
   *
   * @param CategoryNode The target CategoryNode
   * @returns CategoryPath
   */
  public TreePath getTreePathToRoot(CategoryNode node) {
    if (node == null) {
      return null;
    }
    return (new TreePath(getPathToRoot(node)));
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------
  protected void notifyActionListeners() {
    if (_listener != null) {
      _listener.actionPerformed(_event);
    }
  }

  /**
   * Fires a nodechanged event on the SwingThread.
   */
  protected void refresh(final CategoryNode node) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        nodeChanged(node); // remind the tree to render the new node
      }
    });
  }

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}






