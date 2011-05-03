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

package org.apache.isis.viewer.dnd.view;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Text;

public abstract class Toolkit {
    public static boolean debug = false;
    private static Toolkit instance;

    public static int defaultBaseline() {
        return getInstance().colorsAndFonts.defaultBaseline();
    }

    public static int defaultFieldHeight() {
        return getInstance().colorsAndFonts.defaultFieldHeight();
    }

    public static Color getColor(final int rgbColor) {
        return getInstance().colorsAndFonts.getColor(rgbColor);
    }

    public static Color getColor(final String name) {
        final Color color = getInstance().colorsAndFonts.getColor(name);
        if (color == null) {
            throw new IsisException("No such color: " + name);
        }
        return color;
    }

    public static ContentFactory getContentFactory() {
        return getInstance().contentFactory;
    }

    protected static Toolkit getInstance() {
        return instance;
    }

    public static Text getText(final String name) {
        final Text text = getInstance().colorsAndFonts.getText(name);
        if (text == null) {
            throw new IsisException("No such text style: " + name);
        }
        return text;
    }

    public static Viewer getViewer() {
        return getInstance().viewer;
    }

    public static Feedback getFeedbackManager() {
        return getInstance().feedbackManager;
    }

    public static GlobalViewFactory getViewFactory() {
        return getInstance().viewFactory;
    }

    protected ContentFactory contentFactory;
    protected ColorsAndFonts colorsAndFonts;
    protected Viewer viewer;
    protected Feedback feedbackManager;
    protected GlobalViewFactory viewFactory;

    protected Toolkit() {
        if (instance != null) {
            throw new IllegalStateException("Toolkit already instantiated");
        }
        instance = this;
        init();
    }

    protected abstract void init();

}
