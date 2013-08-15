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

package org.apache.isis.core.runtime.logging;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PopupSnapshotAppender extends SnapshotAppender {

    public PopupSnapshotAppender(final org.apache.log4j.spi.TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public PopupSnapshotAppender() {
        super();
    }

    @Override
    protected void writeSnapshot(final String message, final String details) {
        final SubmitDialog dialog = new SubmitDialog("Error logged");
        dialog.show(message, details);
    }
}

class SubmitDialog extends Frame {
    private static final long serialVersionUID = 1L;

    public SubmitDialog(final String title) {
        super(title);
        setLayout(new BorderLayout());
        setBounds(0, 200, 800, 400);
    }

    public void show(final String message, final String text) {
        final TextArea messagePanel = new TextArea();
        messagePanel.setText(message + "\n\n" + text);
        messagePanel.setForeground(Color.black);
        messagePanel.setEditable(false);
        messagePanel.setFont(new Font("Dialog", Font.PLAIN, 9));

        add(messagePanel, BorderLayout.CENTER);

        final Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        add(buttonPanel, BorderLayout.SOUTH);

        final Button ok = new Button("Close");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ok(true);
            }
        });
        buttonPanel.add(ok);

        /*
         * Button cancel = new Button("Save and Close"); cancel
         * .addActionListener(new ActionListener() { public void
         * actionPerformed(final ActionEvent e) { ok(false); } });
         * buttonPanel.add(cancel);
         */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                ok(false);
            }
        });

        setVisible(true);
    }

    protected synchronized void ok(final boolean b) {
        dispose();
    }

}
