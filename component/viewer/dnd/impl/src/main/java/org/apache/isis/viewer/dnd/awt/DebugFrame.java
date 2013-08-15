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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.viewer.dnd.view.debug.DebugOutput;

/**
 * A specialised frame for displaying the details of an object and its display
 * mechanisms.
 */
public abstract class DebugFrame extends Frame {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DebugFrame.class);
    private static Vector<Frame> frames = new Vector<Frame>();
    private int panel = 0;

    /**
     * Calls dispose on all the open debug frames
     * 
     */
    public static void disposeAll() {
        final Frame[] f = new Frame[frames.size()];

        for (int i = 0; i < f.length; i++) {
            f[i] = frames.elementAt(i);
        }

        for (final Frame element : f) {
            element.dispose();
        }
    }

    private TextArea field;
    private TabPane tabPane;

    public DebugFrame() {
        frames.addElement(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                closeDialog();
            }
        });

        final URL url = DebugFrame.class.getResource("/" + "images/debug-log.gif");
        if (url != null) {
            final Image image = Toolkit.getDefaultToolkit().getImage(url);
            if (image != null) {
                setIconImage(image);
            }
        }

        setLayout(new BorderLayout(7, 7));
        final Panel tabPane = createTabPane();
        add(tabPane);
    }

    private Panel createTabPane() {
        tabPane = new TabPane();
        tabPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                final Point point = e.getPoint();
                panel = tabPane.select(point);

                showDebugForPane();
            }
        });

        tabPane.setLayout(new BorderLayout(7, 7));

        final TextArea textArea = new TextArea("", 60, 110, TextArea.SCROLLBARS_BOTH);
        textArea.setForeground(Color.black);
        textArea.setEditable(false);
        // Font font = Isis.getConfiguration().getFont("isis.debug.font", new
        // Font("Monospaced", Font.PLAIN, 10));
        final Font font = new Font("Monospaced", Font.PLAIN, 11);
        textArea.setFont(font);
        tabPane.add("Center", textArea);
        field = textArea;

        final Panel buttons = new Panel();
        buttons.setLayout(new FlowLayout());
        tabPane.add(buttons, BorderLayout.SOUTH);

        // add buttons
        Button b = new java.awt.Button("Refresh");
        b.setFont(font);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                showDebugForPane();
            }
        });

        b = new java.awt.Button("Print...");
        b.setFont(font);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DebugOutput.print("Debug " + tabPane.getName(), field.getText());
            }
        });

        b = new java.awt.Button("Save...");
        b.setFont(font);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DebugOutput.saveToFile("Save details", "Debug " + tabPane.getName(), field.getText());
            }
        });

        b = new java.awt.Button("Copy");
        b.setFont(font);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DebugOutput.saveToClipboard(field.getText());
            }
        });

        b = new java.awt.Button("Close");
        b.setFont(font);

        buttons.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                closeDialog();
            }
        });

        return tabPane;
    }

    @Override
    public Insets getInsets() {
        final Insets insets = super.getInsets();
        insets.left += 10;
        insets.right += 10;
        insets.top += 10;
        insets.bottom += 10;
        return insets;
    }

    private void closeDialog() {
        dialogClosing();
        // hide();
        dispose();
    }

    public void dialogClosing() {
    }

    @Override
    public void dispose() {
        LOG.debug("dispose...");
        tabPane.removeAll();
        frames.removeElement(this);
        super.dispose();
        LOG.debug("...disposed");
    }

    protected abstract DebuggableWithTitle[] getInfo();

    /**
     * show the frame at the specified coordinates
     */
    public void show(final int x, final int y) {
        /*
         * WARNING - When refresh button is pressed it is in the AWT thread; if
         * the repository is thread based then the wrong set of components will
         * be used giving strange results, particularly in the object persistor.
         */
        // TODO run in correct thread
        refresh();

        pack();
        limitBounds(x, y);
        setVisible(true);
    }

    private void refresh() {
        final DebuggableWithTitle[] infos = getInfo();
        final DebuggableWithTitle info = infos[panel];
        if (info != null) {
            setTitle(info.debugTitle());
            final DebugString str = new DebugString();
            info.debugData(str);
            field.setText(str.toString());
            field.setCaretPosition(0);
        }
    }

    public void showDebugForPane() {
        refresh();
    }

    private void limitBounds(final int xLimit, final int yLimit) {
        final Dimension screenSize = getToolkit().getScreenSize();
        final int maxWidth = screenSize.width - 50;
        final int maxHeight = screenSize.height - 50;

        int width = getSize().width;
        int height = getSize().height;

        int x = xLimit;
        if (x + width > maxWidth) {
            x = 0;
            if (x + width > maxWidth) {
                width = maxWidth;
            }
        }

        int y = yLimit;
        if (y + height > maxHeight) {
            y = 0;
            if (y + height > maxHeight) {
                height = maxHeight;
            }
        }

        setSize(width, height);
        setLocation(x, y);
    }

    private class TabPane extends Panel {
        private static final long serialVersionUID = 1L;
        private Rectangle[] tabs;
        private int panel = 0;

        public int select(final Point point) {
            for (int i = 0; i < tabs.length; i++) {
                if (tabs[i] != null && tabs[i].contains(point)) {
                    panel = i;
                    repaint();
                    break;
                }
            }
            return panel;
        }

        @Override
        public Insets getInsets() {
            final Insets insets = super.getInsets();
            insets.left += 10;
            insets.right += 10;
            insets.top += 30;
            insets.bottom += 10;
            return insets;
        }

        @Override
        public void paint(final Graphics g) {
            final DebuggableWithTitle[] info = getInfo();

            if (info != null) {
                if (tabs == null) {
                    tabs = new Rectangle[getInfo().length];
                }
                final Dimension size = getSize();
                g.setColor(Color.gray);
                g.drawRect(0, 20, size.width - 1, size.height - 21);

                FontMetrics fm;
                fm = g.getFontMetrics();
                int offset = 0;
                final int maxWidth = info.length == 0 ? size.width : size.width / info.length - 1;
                for (int i = 0; i < info.length; i++) {
                    String title = info[i].debugTitle();
                    title = title == null ? info[i].getClass().getName() : title;
                    final int width = Math.min(maxWidth, fm.stringWidth(title) + 20);

                    tabs[i] = new Rectangle(offset, 0, width, 20);
                    g.setColor(Color.gray);
                    g.drawRect(offset + 0, 0, width, 20);
                    if (i == panel) {
                        g.setColor(Color.white);
                        g.fillRect(offset + 1, 1, width - 1, 20);
                        // g.drawLine(offset + 1, 20, offset + width, 20);
                        g.setColor(Color.black);
                    } else {
                        g.setColor(Color.lightGray);
                        g.fillRect(offset + 1, 1, width - 1, 20 - 1);
                        g.setColor(Color.gray);
                    }

                    g.drawString(title, offset + 9, 20 - 5);

                    offset += width;
                }
                g.setColor(Color.white);
                g.fillRect(offset + 1, 1, size.width - offset, 20 - 1);
            }
        }

    }
}
