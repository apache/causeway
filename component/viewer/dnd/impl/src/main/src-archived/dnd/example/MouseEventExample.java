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


package org.apache.isis.viewer.dnd.example;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class MouseEventExample extends Frame {

    public static void main(final String[] args) {
        final MouseEventExample frame = new MouseEventExample();
        frame.setSize(300, 400);
        frame.show();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                frame.dispose();
            }
        });

        frame.addMouseListener(new Mouse());
    }

    int left = 10 + getInsets().left;
    int top = 40 + getInsets().top;
    int width = 100;
    int height;

    public void show() {
        super.show();
        left = 10 + getInsets().left;
        top = 10 + getInsets().top;
    }
}

class Mouse implements MouseListener {

    public void mouseClicked(MouseEvent e) {
        System.out.print(e.isPopupTrigger() ? "POPUP " : "");
        System.out.println(e);
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        System.out.print(e.isPopupTrigger() ? "POPUP " : "");
        System.out.println(e);
    }

    public void mouseReleased(MouseEvent e) {
        System.out.print(e.isPopupTrigger() ? "POPUP " : "");
        System.out.println(e);
    }

}
