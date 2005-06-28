/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer;

import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * LogTableRowRenderer
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public class LogTableRowRenderer extends DefaultTableCellRenderer {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------
  protected boolean _highlightFatal = true;
  protected Color _color = new Color(230, 230, 230);

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  public Component getTableCellRendererComponent(JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row,
      int col) {

    if ((row % 2) == 0) {
      setBackground(_color);
    } else {
      setBackground(Color.white);
    }

    FilteredLogTableModel model = (FilteredLogTableModel) table.getModel();
    LogRecord record = model.getFilteredRecord(row);

    setForeground(getLogLevelColor(record.getLevel()));

    return (super.getTableCellRendererComponent(table,
        value,
        isSelected,
        hasFocus,
        row, col));
  }


  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------
  protected Color getLogLevelColor(LogLevel level) {
    return (Color) LogLevel.getLogLevelColorMap().get(level);
  }

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

}






