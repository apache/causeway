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

package org.apache.isis.viewer.html.monitoring.systemconsole;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import org.apache.isis.core.runtime.system.context.IsisContext;

public class AWTConsole extends Frame implements ServerConsole {
    private static final long serialVersionUID = 1L;
    private final static Logger LOG = Logger.getLogger(AWTConsole.class);
    public static final String WIDTH = "isis.awt-console.width";
    public static final String HEIGHT = "isis.awt-console.height";
    public static final int DEFAULT_WIDTH = 600;
    public static final int DEFAULT_HEIGHT = 350;
    private Server server;
    private TextArea log;
    private Button quit;

    public AWTConsole() {
        super("Object Server Console");
        buildGUI();
        setVisible(true);
        ;
    }

    /**
     *
     */
    private void addButtons() {
        final Panel p = new Panel();

        p.setLayout(new java.awt.GridLayout(1, 0, 10, 0));
        add(p, BorderLayout.SOUTH);
        Button b;

        p.add(b = new Button("Blank"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                clearLog();
            }
        });

        // debug
        p.add(b = new Button("Classes"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                listClasses();
            }
        });

        p.add(b = new Button("Cache"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                listCachedObjects();
            }
        });

        p.add(b = new Button("C/Cache"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                clearCache();
            }
        });

        // quit
        p.add(quit = new Button("Quit"));
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                quit();
            }
        });
    }

    /**
     * LogWindow constructor comment.
     */
    private void buildGUI() {
        add(log = new TextArea());
        addButtons();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = new Dimension();
        final Insets insets = getInsets();

        frameSize.width = IsisContext.getConfiguration().getInteger(WIDTH, DEFAULT_WIDTH);
        frameSize.height = IsisContext.getConfiguration().getInteger(HEIGHT, DEFAULT_HEIGHT);
        final Rectangle bounds = new Rectangle(frameSize);

        bounds.x = screenSize.width - frameSize.width - insets.right;
        bounds.y = 0 + insets.top;
        setBounds(bounds);
    }

    private void clearCache() {
    }

    /**
     * 
     * @param message
     *            java.lang.String
     */
    private void clearLog() {
        log.setText("");
    }

    /**
     *
     */
    @Override
    public void close() {
        dispose();
    }

    @Override
    public void init(final Server server) {
        this.server = server;
        log("Console in control of " + server);
    }

    private void listCachedObjects() {
        /*
         * Enumeration e = server.getObjectStore().cache();
         * 
         * log("Cached objects:-"); while (e.hasMoreElements()) { ObjectAdapter
         * object = (ObjectAdapter) e.nextElement();
         * 
         * log(" " + object.getClassName() + "[" + (object.isResolved() ? "" :
         * "~") + object.getOid() + "] " + object.title()); } log();
         */
    }

    private void listClasses() {
        /*
         * try { Enumeration e = server.getObjectStore().classes();
         * 
         * log("Loaded classes:-"); while (e.hasMoreElements()) {
         * ObjectSpecification object = (ObjectSpecification) e.nextElement();
         * 
         * log(" " + object); } log(); } catch (ObjectStoreException e) {
         * LOG.error("Error listing classes " + e.getMessage()); }
         */
    }

    @Override
    public void log() {
        log.append("\n");
    }

    @Override
    public void log(final String message) {
        log.append(message + '\n');
        LOG.info(message);
    }

    /**
     *
     */
    public void quit() {
        server.shutdown();
        close();
        exitSystem();
    }

    private void exitSystem() {
        System.exit(0);
    }
}
