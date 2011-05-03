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

package org.apache.isis.viewer.dnd.drawing;

/**
 * A look-up for font and color details.
 * 
 */
public interface ColorsAndFonts {
    public final static String COLOR_BLACK = "color.black";
    public final static String COLOR_WHITE = "color.white";
    public final static String COLOR_PRIMARY1 = "color.primary1";
    public final static String COLOR_PRIMARY2 = "color.primary2";
    public final static String COLOR_PRIMARY3 = "color.primary3";
    public final static String COLOR_SECONDARY1 = "color.secondary1";
    public final static String COLOR_SECONDARY2 = "color.secondary2";
    public final static String COLOR_SECONDARY3 = "color.secondary3";

    // background colors
    public final static String COLOR_APPLICATION = "color.background.application";
    public final static String COLOR_WINDOW = "color.background.window";
    public final static String COLOR_MENU_VALUE = "color.background.menu.value";
    public final static String COLOR_MENU_CONTENT = "color.background.menu.content";
    public final static String COLOR_MENU_VIEW = "color.background.menu.view";
    public final static String COLOR_MENU_WORKSPACE = "color.background.menu.workspace";

    // menu colors
    public final static String COLOR_MENU = "color.menu.normal";
    public final static String COLOR_MENU_DISABLED = "color.menu.disabled";
    public final static String COLOR_MENU_REVERSED = "color.menu.reversed";

    // label colors
    public final static String COLOR_LABEL = "color.label.normal";
    public final static String COLOR_LABEL_DISABLED = "color.label.disabled";
    public final static String COLOR_LABEL_MANDATORY = "color.label.mandatory";

    // state colors
    public final static String COLOR_IDENTIFIED = "color.identified";
    public final static String COLOR_VALID = "color.valid";
    public final static String COLOR_INVALID = "color.invalid";
    public final static String COLOR_ERROR = "color.error";
    public final static String COLOR_ACTIVE = "color.active";
    public final static String COLOR_OUT_OF_SYNC = "color.out-of-sync";

    // text colors
    public final static String COLOR_TEXT_SAVED = "color.text.saved";
    public final static String COLOR_TEXT_EDIT = "color.text.edit";
    public final static String COLOR_TEXT_CURSOR = "color.text.cursor";
    public final static String COLOR_TEXT_HIGHLIGHT = "color.text.highlight";

    // debug outline colors
    public final static String COLOR_DEBUG_BASELINE = "color.debug.baseline";
    public final static String COLOR_DEBUG_BOUNDS_BORDER = "color.debug.bounds.border";
    public final static String COLOR_DEBUG_BOUNDS_DRAW = "color.debug.bounds.draw";
    public final static String COLOR_DEBUG_BOUNDS_REPAINT = "color.debug.bounds.repaint";
    public final static String COLOR_DEBUG_BOUNDS_VIEW = "color.debug.bounds.view";

    // fonts
    public final static String TEXT_DEFAULT = "text.default";
    public final static String TEXT_CONTROL = "text.control";
    public final static String TEXT_TITLE = "text.title";
    public final static String TEXT_TITLE_SMALL = "text.title.small";
    public final static String TEXT_DEBUG = "text.debug";
    public final static String TEXT_STATUS = "text.status";
    public final static String TEXT_ICON = "text.icon";
    public final static String TEXT_LABEL = "text.label";
    public final static String TEXT_LABEL_MANDATORY = "text.label.mandatory";
    public final static String TEXT_LABEL_DISABLED = "text.label.disabled";
    public final static String TEXT_MENU = "text.menu";
    public final static String TEXT_NORMAL = "text.normal";

    int defaultBaseline();

    int defaultFieldHeight();

    Color getColor(int rgbColor);

    Color getColor(String name);

    Text getText(String name);

    void init();

}
