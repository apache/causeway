/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * CategoryImmediateEditor
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public class CategoryImmediateEditor extends DefaultTreeCellEditor {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------
  private CategoryNodeRenderer renderer;
  protected Icon editingIcon = null;

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------
  public CategoryImmediateEditor(JTree tree,
      CategoryNodeRenderer renderer,
      CategoryNodeEditor editor) {
    super(tree, renderer, editor);
    this.renderer = renderer;
    renderer.setIcon(null);
    renderer.setLeafIcon(null);
    renderer.setOpenIcon(null);
    renderer.setClosedIcon(null);

    super.editingIcon = null;
  }

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------
  public boolean shouldSelectCell(EventObject e) {
    boolean rv = false;  // only mouse events

    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      TreePath path = tree.getPathForLocation(me.getX(),
          me.getY());
      CategoryNode node = (CategoryNode)
          path.getLastPathComponent();

      rv = node.isLeaf() /*|| !inCheckBoxHitRegion(me)*/;
    }
    return rv;
  }

  public boolean inCheckBoxHitRegion(MouseEvent e) {
    TreePath path = tree.getPathForLocation(e.getX(),
        e.getY());
    if (path == null) {
      return false;
    }
    CategoryNode node = (CategoryNode) path.getLastPathComponent();
    boolean rv = false;

    if (true) {
      // offset and lastRow DefaultTreeCellEditor
      // protected members

      Rectangle bounds = tree.getRowBounds(lastRow);
      Dimension checkBoxOffset =
          renderer.getCheckBoxOffset();

      bounds.translate(offset + checkBoxOffset.width,
          checkBoxOffset.height);

      rv = bounds.contains(e.getPoint());
    }
    return true;
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  protected boolean canEditImmediately(EventObject e) {
    boolean rv = false;

    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      rv = inCheckBoxHitRegion(me);
    }

    return rv;
  }

  protected void determineOffset(JTree tree, Object value,
      boolean isSelected, boolean expanded,
      boolean leaf, int row) {
    // Very important: means that the tree won't jump around.
    offset = 0;
  }

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}






