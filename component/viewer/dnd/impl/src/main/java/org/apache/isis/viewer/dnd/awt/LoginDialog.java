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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;

public class LoginDialog extends Frame implements ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(LoginDialog.class);
    private final static int BORDER = 12;
    private TextField user;
    private TextField password;
    private Button cancel;
    private Button login;

    private static String CANCEL_LABEL = " Cancel ";
    private static String LOGIN_LABEL = " Login ";
    private boolean logIn = true;
    private final AuthenticationManager authenticationManager;
    private AuthenticationSession session;
    private Label instructionLabel;

    public LoginDialog(final AuthenticationManager authenticationManager) {
        super("Apache Isis Login");
        this.authenticationManager = authenticationManager;

        setBackground(new Color(0xe0e0e0));

        AWTUtilities.addWindowIcon(this, "login-logo.png");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                cancel(e.getComponent());
            }
        });

        setLayout(new BorderLayout(0, 10));

        createInstructionLabel();
        createLoginFields();
        createButtonsPanel();

        setResizable(false);
        pack();
        final int height = getSize().height;
        final int width = getFontMetrics(getFont()).charWidth('x') * 48;
        setSize(width, height);
        final Dimension screen = getToolkit().getScreenSize();

        int x = (screen.width / 2) - (width / 2);

        if ((screen.width / screen.height) >= 2) {
            x = (screen.width / 4) - (width / 2);
        }

        final int y = (screen.height / 2) - (height / 2);
        setLocation(x, y);
        user.requestFocus();
    }

    private void createInstructionLabel() {
        instructionLabel = new Label("Please enter your user name and password.");
        add(instructionLabel, BorderLayout.NORTH);
    }

    private void createLoginFields() {
        final Panel form = new Panel(new GridLayout(2, 2, 6, 8)) {
            private static final long serialVersionUID = 1L;

            @Override
            public Insets getInsets() {
                return new Insets(12, 0, 6, 80);
            }
        };
        add(form, BorderLayout.CENTER);

        form.add(new Label("User name:", Label.RIGHT));
        form.add(user = new TextField());
        user.addKeyListener(this);

        form.add(new Label("Password:", Label.RIGHT));
        form.add(password = new TextField());
        password.addKeyListener(this);
        password.setEchoChar('*');
    }

    private void createButtonsPanel() {
        final Panel buttons = new Panel(new FlowLayout(FlowLayout.RIGHT));
        add(buttons, BorderLayout.SOUTH);

        buttons.add(cancel = new Button(CANCEL_LABEL));
        cancel.addActionListener(this);
        cancel.addKeyListener(this);

        buttons.add(login = new Button(LOGIN_LABEL));
        login.addActionListener(this);
        login.addKeyListener(this);
    }

    @Override
    public Insets getInsets() {
        final Insets in = super.getInsets();
        in.top += BORDER;
        in.bottom += BORDER / 2;
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
        logIn = false;
        notify();
    }

    private synchronized void action(final Object widget) {
        if (widget == cancel) {
            cancel(widget);
        } else if (widget == login || widget == password) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            instructionLabel.setText("Authorising...");
            instructionLabel.setForeground(Color.BLACK);

            final AuthenticationRequestPassword authenticationRequest = new AuthenticationRequestPassword(getUser(), getPassword());
            session = authenticationManager.authenticate(authenticationRequest);
            if (session == null) {
                try {
                    Thread.sleep(750);
                } catch (final InterruptedException ignore) {
                }
                instructionLabel.setText("Invalid user name or password; please try again.");
                instructionLabel.setForeground(Color.RED);
            } else {
                logIn = true;
                notify();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (widget == user) {
            password.requestFocus();
        }
    }

    @Override
    public void dispose() {
        LOG.debug("dispose...");
        super.dispose();
        LOG.debug("...disposed");

    }

    private String getUser() {
        return StringExtensions.removeTabs(user.getText()).trim();
    }

    public void setUserName(final String name) {
        user.setText(name);
    }

    private String getPassword() {
        return StringExtensions.removeTabs(password.getText()).trim();
    }

    public AuthenticationSession getSession() {
        return session;
    }

    public synchronized boolean login() {
        try {
            wait();
        } catch (final InterruptedException e) {
        }
        return logIn;
    }

}
