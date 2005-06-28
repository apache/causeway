/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package org.apache.log4j.lf5.viewer;

import javax.swing.*;
import java.awt.*;

/**
 * LogFactor5Dialog
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public abstract class LogFactor5Dialog extends JDialog {
  //--------------------------------------------------------------------------
  //   Constants:
  //--------------------------------------------------------------------------
  protected static final Font DISPLAY_FONT = new Font("Arial", Font.BOLD, 12);
  //--------------------------------------------------------------------------
  //   Protected Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Variables:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Constructors:
  //--------------------------------------------------------------------------
  protected LogFactor5Dialog(JFrame jframe, String message, boolean modal) {
    super(jframe, message, modal);
  }

  //--------------------------------------------------------------------------
  //   Public Methods:
  //--------------------------------------------------------------------------
  public void show() {
    pack();
    minimumSizeDialog(this, 200, 100);
    centerWindow(this);
    super.show();
  }

  //--------------------------------------------------------------------------
  //   Protected Methods:
  //--------------------------------------------------------------------------

  //--------------------------------------------------------------------------
  //   Private Methods:
  //--------------------------------------------------------------------------
  protected void centerWindow(Window win) {
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

    // If larger than screen, reduce window width or height
    if (screenDim.width < win.getSize().width) {
      win.setSize(screenDim.width, win.getSize().height);
    }

    if (screenDim.height < win.getSize().height) {
      win.setSize(win.getSize().width, screenDim.height);
    }

    // Center Frame, Dialogue or Window on screen
    int x = (screenDim.width - win.getSize().width) / 2;
    int y = (screenDim.height - win.getSize().height) / 2;
    win.setLocation(x, y);
  }

  protected void wrapStringOnPanel(String message,
      Container container) {
    GridBagConstraints c = getDefaultConstraints();
    c.gridwidth = GridBagConstraints.REMAINDER;
    // Insets() args are top, left, bottom, right
    c.insets = new Insets(0, 0, 0, 0);
    GridBagLayout gbLayout = (GridBagLayout) container.getLayout();


    while (message.length() > 0) {
      int newLineIndex = message.indexOf('\n');
      String line;
      if (newLineIndex >= 0) {
        line = message.substring(0, newLineIndex);
        message = message.substring(newLineIndex + 1);
      } else {
        line = message;
        message = "";
      }
      Label label = new Label(line);
      label.setFont(DISPLAY_FONT);
      gbLayout.setConstraints(label, c);
      container.add(label);
    }
  }

  protected GridBagConstraints getDefaultConstraints() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1; // One row high
    // Insets() args are top, left, bottom, right
    constraints.insets = new Insets(4, 4, 4, 4);
    // fill of NONE means do not change size
    constraints.fill = GridBagConstraints.NONE;
    // WEST means align left
    constraints.anchor = GridBagConstraints.WEST;

    return constraints;
  }

  protected void minimumSizeDialog(Component component,
      int minWidth,
      int minHeight) {
    // set the min width
    if (component.getSize().width < minWidth)
      component.setSize(minWidth, component.getSize().height);
    // set the min height
    if (component.getSize().height < minHeight)
      component.setSize(component.getSize().width, minHeight);
  }
  //--------------------------------------------------------------------------
  //   Nested Top-Level Classes or Interfaces
  //--------------------------------------------------------------------------
}