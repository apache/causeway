/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * CategoryAbstractCellEditor.  Base class to handle the some common
 * details of cell editing.
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 */

// Contributed by ThoughtWorks Inc.

public class CategoryAbstractCellEditor implements TableCellEditor, TreeCellEditor {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------
  protected EventListenerList _listenerList = new EventListenerList();
  protected Object _value;
  protected ChangeEvent _changeEvent = null;
  protected int _clickCountToStart = 1;

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  public Object getCellEditorValue() {
    return _value;
  }

  public void setCellEditorValue(Object value) {
    _value = value;
  }

  public void setClickCountToStart(int count) {
    _clickCountToStart = count;
  }

  public int getClickCountToStart() {
    return _clickCountToStart;
  }

  public boolean isCellEditable(EventObject anEvent) {
    if (anEvent instanceof MouseEvent) {
      if (((MouseEvent) anEvent).getClickCount() < _clickCountToStart) {
        return false;
      }
    }
    return true;
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    if (this.isCellEditable(anEvent)) {
      if (anEvent == null ||
          ((MouseEvent) anEvent).getClickCount() >= _clickCountToStart) {
        return true;
      }
    }
    return false;
  }

  public boolean stopCellEditing() {
    fireEditingStopped();
    return true;
  }

  public void cancelCellEditing() {
    fireEditingCanceled();
  }

  public void addCellEditorListener(CellEditorListener l) {
    _listenerList.add(CellEditorListener.class, l);
  }

  public void removeCellEditorListener(CellEditorListener l) {
    _listenerList.remove(CellEditorListener.class, l);
  }

  public Component getTreeCellEditorComponent(
      JTree tree, Object value,
      boolean isSelected,
      boolean expanded,
      boolean leaf, int row) {
    return null;
  }

  public Component getTableCellEditorComponent(
      JTable table, Object value,
      boolean isSelected,
      int row, int column) {
    return null;
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------
  protected void fireEditingStopped() {
    Object[] listeners = _listenerList.getListenerList();

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == CellEditorListener.class) {
        if (_changeEvent == null) {
          _changeEvent = new ChangeEvent(this);
        }

        ((CellEditorListener) listeners[i + 1]).editingStopped(_changeEvent);
      }
    }
  }

  protected void fireEditingCanceled() {
    Object[] listeners = _listenerList.getListenerList();

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == CellEditorListener.class) {
        if (_changeEvent == null) {
          _changeEvent = new ChangeEvent(this);
        }

        ((CellEditorListener) listeners[i + 1]).editingCanceled(_changeEvent);
      }
    }
  }

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}
