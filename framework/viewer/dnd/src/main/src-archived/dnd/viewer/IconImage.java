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


package org.apache.isis.viewer.dnd.viewer;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;



public class IconImage extends Frame {

    public IconImage(String fileName) {
        Image image = getToolkit().getImage(fileName);
        setIconImage(image);
        setSize(150, 150);
        show();
    }

    public static void main(String[] args) {
        new IconImage(args[0]);
    }

    public void paint(Graphics g) {
        Insets in = insets();
        g.drawImage(getIconImage(), in.left, in.top, this);
    }
}


