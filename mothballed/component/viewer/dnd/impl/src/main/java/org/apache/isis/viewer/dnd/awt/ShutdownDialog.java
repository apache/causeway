/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.dnd.awt;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShutdownDialog extends Dialog implements ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ShutdownDialog.class);
    private final static int BORDER = 10;
    private Button cancel;
    private Button quit;
    private static String CANCEL_LABEL = "Cancel";
    private static String QUIT_LABEL = "Ok";

    public ShutdownDialog(final ViewerFrame owner) {
        super(owner, "Apache Isis", true);

        // AWTUtilities.addWindowIcon(this, "shutdown-logo.gif");

        setLayout(new GridLayout(2, 3, 10, 10));

        add(new Label("Exit Apache Isis?", Label.LEFT));

        add(new Panel());
        add(new Panel());
        add(new Panel());

        add(quit = new Button(QUIT_LABEL));
        quit.addActionListener(this);
        quit.addKeyListener(this);

        add(cancel = new Button(CANCEL_LABEL));
        cancel.addActionListener(this);
        cancel.addKeyListener(this);

        pack();
        final int width = getSize().width; // getWidth();
        final int height = getSize().height; // getHeight();
        final Dimension screen = owner.getSize();
        final Point point = owner.getLocation();

        final int x = point.x + (screen.width / 2) - (width / 2);
        final int y = point.y + (screen.height / 2) - (height / 2);

        setLocation(x, y);
        setVisible(true);
        quit.requestFocus();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                dispose();
            }
        });
    }

    @Override
    public Insets getInsets() {
        final Insets in = super.getInsets();
        in.top += BORDER;
        in.bottom += BORDER;
        in.left += BORDER;
        in.right += BORDER;
        return in;
    }

    @Override
    public void actionPerformed(final ActionEvent evt) {
        action(evt.getSource());
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        // ignore
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            action(e.getComponent());
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancel(e.getComponent());
        }
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        // ignore
    }

    private synchronized void cancel(final Object widget) {
        dispose();
    }

    private synchronized void action(final Object widget) {
        if (widget == cancel) {
            cancel(widget);
        } else if (widget == quit) {
            quit();
        }
    }

    private void quit() {
        dispose();
        ((ViewerFrame) getParent()).quit();
    }

    @Override
    public void dispose() {
        LOG.debug("dispose...");
        super.dispose();
        LOG.debug("...disposed");

    }
}
