/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer;

import org.apache.log4j.lf5.util.DateFormatManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * LogTable.
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brad Marlborough
 * @author Brent Sprecher
 */

// Contributed by ThoughtWorks Inc.

public class LogTable extends JTable {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------
  protected int _rowHeight = 30;
  protected JTextArea _detailTextArea;

  // For the columns:
  protected int _numCols = 9;
  protected TableColumn[] _tableColumns = new TableColumn[_numCols];
  protected int[] _colWidths = {40, 40, 40, 70, 70, 360, 440, 200, 60};
  protected LogTableColumn[] _colNames = LogTableColumn.getLogTableColumnArray();
  protected int _colDate = 0;
  protected int _colThread = 1;
  protected int _colMessageNum = 2;
  protected int _colLevel = 3;
  protected int _colNDC = 4;
  protected int _colCategory = 5;
  protected int _colMessage = 6;
  protected int _colLocation = 7;
  protected int _colThrown = 8;

  protected DateFormatManager _dateFormatManager = null;

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------

  public LogTable(JTextArea detailTextArea) {
    super();

    init();

    _detailTextArea = detailTextArea;

    setModel(new FilteredLogTableModel());

    Enumeration columns = getColumnModel().getColumns();
    int i = 0;
    while (columns.hasMoreElements()) {
      TableColumn col = (TableColumn) columns.nextElement();
      col.setCellRenderer(new LogTableRowRenderer());
      col.setPreferredWidth(_colWidths[i]);

      _tableColumns[i] = col;
      i++;
    }

    ListSelectionModel rowSM = getSelectionModel();
    rowSM.addListSelectionListener(new LogTableListSelectionListener(this));

    //setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
  }

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------

  /**
   * Get the DateFormatManager for formatting dates.
   */
  public DateFormatManager getDateFormatManager() {
    return _dateFormatManager;
  }

  /**
   * Set the date format manager for formatting dates.
   */
  public void setDateFormatManager(DateFormatManager dfm) {
    _dateFormatManager = dfm;
  }

  public synchronized void clearLogRecords() {
    //For JDK1.3
    //((DefaultTableModel)getModel()).setRowCount(0);

    // For JDK1.2.x
    getFilteredLogTableModel().clear();
  }

  public FilteredLogTableModel getFilteredLogTableModel() {
    return (FilteredLogTableModel) getModel();
  }

  // default view if a view is not set and saved
  public void setDetailedView() {
    //TODO: Defineable Views.
    TableColumnModel model = getColumnModel();
    // Remove all the columns:
    for (int f = 0; f < _numCols; f++) {
      model.removeColumn(_tableColumns[f]);
    }
    // Add them back in the correct order:
    for (int i = 0; i < _numCols; i++) {
      model.addColumn(_tableColumns[i]);
    }
    //SWING BUG:
    sizeColumnsToFit(-1);
  }

  public void setView(List columns) {
    TableColumnModel model = getColumnModel();

    // Remove all the columns:
    for (int f = 0; f < _numCols; f++) {
      model.removeColumn(_tableColumns[f]);
    }
    Iterator selectedColumns = columns.iterator();
    Vector columnNameAndNumber = getColumnNameAndNumber();
    while (selectedColumns.hasNext()) {
      // add the column to the view
      model.addColumn(_tableColumns[columnNameAndNumber.indexOf(selectedColumns.next())]);
    }

    //SWING BUG:
    sizeColumnsToFit(-1);
  }

  public void setFont(Font font) {
    super.setFont(font);
    Graphics g = this.getGraphics();
    if (g != null) {
      FontMetrics fm = g.getFontMetrics(font);
      int height = fm.getHeight();
      _rowHeight = height + height / 3;
      setRowHeight(_rowHeight);
    }


  }


  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  protected void init() {
    setRowHeight(_rowHeight);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  // assign a column number to a column name
  protected Vector getColumnNameAndNumber() {
    Vector columnNameAndNumber = new Vector();
    for (int i = 0; i < _colNames.length; i++) {
      columnNameAndNumber.add(i, _colNames[i]);
    }
    return columnNameAndNumber;
  }

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces:
  //--------------------------------------------------------------------------

  class LogTableListSelectionListener implements ListSelectionListener {
    protected JTable _table;

    public LogTableListSelectionListener(JTable table) {
      _table = table;
    }

    public void valueChanged(ListSelectionEvent e) {
      //Ignore extra messages.
      if (e.getValueIsAdjusting()) {
        return;
      }

      ListSelectionModel lsm = (ListSelectionModel) e.getSource();
      if (lsm.isSelectionEmpty()) {
        //no rows are selected
      } else {
        StringBuffer buf = new StringBuffer();
        int selectedRow = lsm.getMinSelectionIndex();

        for (int i = 0; i < _numCols - 1; i++) {
          String value = "";
          Object obj = _table.getModel().getValueAt(selectedRow, i);
          if (obj != null) {
            value = obj.toString();
          }

          buf.append(_colNames[i] + ":");
          buf.append("\t");

          if (i == _colThread || i == _colMessage || i == _colLevel) {
            buf.append("\t"); // pad out the date.
          }

          if (i == _colDate || i == _colNDC) {
            buf.append("\t\t"); // pad out the date.
          }

//               if( i == _colSequence)
//               {
//                  buf.append("\t\t\t"); // pad out the Sequnce.
//               }

          buf.append(value);
          buf.append("\n");
        }
        buf.append(_colNames[_numCols - 1] + ":\n");
        Object obj = _table.getModel().getValueAt(selectedRow, _numCols - 1);
        if (obj != null) {
          buf.append(obj.toString());
        }

        _detailTextArea.setText(buf.toString());
      }
    }
  }
}






