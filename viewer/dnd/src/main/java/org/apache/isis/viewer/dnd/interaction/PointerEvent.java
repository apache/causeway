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

package org.apache.isis.viewer.dnd.interaction;

import java.awt.event.InputEvent;

/**
 * Details an event involving the pointer, such as a click or drag.
 */
public abstract class PointerEvent {
    protected int mods;

    /**
     * Creates a new pointer event object.
     * 
     * @param mods
     *            the button and key modifiers (@see java.awt.event.MouseEvent)
     */
    PointerEvent(final int mods) {
        this.mods = mods;
    }

    public boolean button1() {
        return (isButton1() && !isShift()) || (isButton2() && isShift());
    }

    public boolean button2() {
        return (isButton2() && !isShift()) || (isButton1() && isShift());
    }

    public boolean button3() {
        return isButton3();
    }

    /**
     * Returns true if the 'Alt' key is depressed
     */
    public boolean isAlt() {
        return (mods & InputEvent.ALT_MASK) > 0;
    }

    /**
     * Returns true if the left-hand button on the mouse is depressed
     */
    private boolean isButton1() {
        return (mods & InputEvent.BUTTON1_MASK) > 0;
    }

    /**
     * Returns true if the middle button on the mouse is depressed
     */
    private boolean isButton2() {
        return (mods & InputEvent.BUTTON2_MASK) > 0;
    }

    /**
     * Returns true if the right-hand button on the mouse is depressed
     */
    private boolean isButton3() {
        return (mods & InputEvent.BUTTON3_MASK) > 0;
    }

    /**
     * Returns true if the control key is depressed
     */
    public boolean isCtrl() {
        return (mods & InputEvent.CTRL_MASK) > 0;
    }

    /**
     * Returns true if the 'Alt' key is depressed
     */
    public boolean isMeta() {
        return (mods & InputEvent.META_MASK) > 0;
    }

    /**
     * Returns true if the shift key is depressed
     */
    public boolean isShift() {
        return (mods & InputEvent.SHIFT_MASK) > 0;
    }

    @Override
    public String toString() {
        final String buttons = (isButton1() ? "^" : "-") + (isButton2() ? "^" : "-") + (isButton3() ? "^" : "-");
        final String modifiers = (isShift() ? "S" : "-") + (isAlt() ? "A" : "-") + (isCtrl() ? "C" : "-");

        return "buttons=" + buttons + ",modifiers=" + modifiers;
    }
}
