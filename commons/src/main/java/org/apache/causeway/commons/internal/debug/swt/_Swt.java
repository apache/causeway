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
package org.apache.causeway.commons.internal.debug.swt;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class _Swt {

    public void enableSwing() {
        System.setProperty("java.awt.headless", "false");
    }

    /**
     * Display a Swing dialog to be confirmed with an OK button.
     * <p>
     * Blocks the calling thread until the dialog is closed.
     * <p>
     * Introduced to stop the program at specific point(s),
     * to allow for performance profiling.
     */
    public void prompt(final String message) {

        val awaitableLatch = new CountDownLatch(1);

        SwingUtilities.invokeLater(()->{
            val frame = new JFrame();
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(new JLabel(message));
            frame.pack();
            frame.setLocationRelativeTo(null); // center to screen
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent arg0) {
                    frame.setVisible(false);
                    awaitableLatch.countDown();
                }
            });
        });
        try {
            awaitableLatch.await();
        } catch (InterruptedException e) {
            // ignore
        }
    }

}
