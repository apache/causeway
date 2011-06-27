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

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.apache.isis.viewer.dnd.view.InteractionSpyWindow;

class SpyWindow implements InteractionSpyWindow {
    private int event;
    private String label[][] = new String[2][20];
    private String[] trace = new String[60];
    private int traceIndex;
    private SpyFrame frame;

    class SpyFrame extends Frame {
        private static final long serialVersionUID = 1L;

        public SpyFrame() {
            super("View/Interaction Spy");
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    close();
                }
            });
        }

        @Override
        public void paint(final Graphics g) {
            int baseline = getInsets().top + 15;

            g.drawString("Event " + event, 10, baseline);
            baseline += 18;

            for (int i = 0; i < label[0].length; i++) {
                if (label[0][i] != null) {
                    g.drawString(label[0][i], 10, baseline);
                    g.drawString(label[1][i], 150, baseline);
                }
                baseline += 12;
            }

            baseline += 6;
            for (int i = 0; i < traceIndex; i++) {
                if (trace[i] != null) {
                    g.drawString(trace[i], 10, baseline);
                }
                baseline += 12;
            }
        }
    }

    @Override
    public void display(final int event, final String label[][], final String[] trace, final int traceIndex) {
        if (frame != null) {
            this.event = event;
            this.traceIndex = traceIndex;
            this.label = label;
            this.trace = trace;
            frame.repaint();
        }
    }

    @Override
    public void open() {
        frame = new SpyFrame();
        frame.setBounds(10, 10, 800, 500);
        frame.setVisible(true);
    }

    @Override
    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }
}
