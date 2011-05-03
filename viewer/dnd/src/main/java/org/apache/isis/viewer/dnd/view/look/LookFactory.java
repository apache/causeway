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

package org.apache.isis.viewer.dnd.view.look;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.viewer.dnd.util.Properties;
import org.apache.isis.viewer.dnd.view.Look;
import org.apache.isis.viewer.dnd.view.look.line.LineLook;
import org.apache.isis.viewer.dnd.view.look.linux.LinuxLook;
import org.apache.isis.viewer.dnd.view.look.simple.SimpleLook;
import org.apache.isis.viewer.dnd.view.look.swing.SwingLook;

public class LookFactory {
    private static final Look SIMPLE_LOOK = new SimpleLook();
    private static final LineLook LINE_LOOK = new LineLook();
    private static final Look SWING_LOOK = new SwingLook();
    private static final Look LINUX_LOOK = new LinuxLook();
    private static final Look defaultLook = SIMPLE_LOOK;
    private static List<Look> looks = new ArrayList<Look>();
    private static Look installedLook;

    public static void init() {
        looks.add(SIMPLE_LOOK);
        looks.add(LINE_LOOK);
        looks.add(SWING_LOOK);
        looks.add(LINUX_LOOK);

        final String className = Properties.getString("look");
        if (className != null) {
            for (final Look look : looks) {
                if (look.getClass().getName().equals(className)) {
                    setLook(look);
                    return;
                }
            }
        }
        setLook(defaultLook);
    }

    public static void setLook(final Look look) {
        defaultLook.install();
        look.install();
        installedLook = look;

        Properties.setStringOption("look", installedLook.getClass().getName());
    }

    public static Look getInstalledLook() {
        return installedLook;
    }

    public static Iterable<Look> getAvailableLooks() {
        return looks;
    }

}
