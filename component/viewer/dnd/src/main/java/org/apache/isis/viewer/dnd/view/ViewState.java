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

public class ViewState implements Cloneable {
    private static final short CAN_DROP = 0x10;
    private static final short CANT_DROP = 0x08;
    private static final short CONTENT_IDENTIFIED = 0x04;
    private static final short ROOT_VIEW_IDENTIFIED = 0x01;
    private static final short VIEW_IDENTIFIED = 0x02;
    private static final short INVALID = 0x40;
    private static final short ACTIVE = 0x20;
    private static final short OUT_OF_SYNCH = 0x80;

    private short state;

    public void setCanDrop() {
        state |= CAN_DROP;
    }

    public void setCantDrop() {
        state |= CANT_DROP;
    }

    public void setContentIdentified() {
        state |= CONTENT_IDENTIFIED;
    }

    public boolean isObjectIdentified() {
        return (state & CONTENT_IDENTIFIED) > 0;
    }

    public void setRootViewIdentified() {
        state |= ROOT_VIEW_IDENTIFIED;
    }

    public boolean isRootViewIdentified() {
        return (state & ROOT_VIEW_IDENTIFIED) > 0;
    }

    public void setViewIdentified() {
        state |= VIEW_IDENTIFIED;
    }

    public boolean isViewIdentified() {
        return (state & VIEW_IDENTIFIED) > 0;
    }

    public boolean canDrop() {
        return (state & CAN_DROP) == CAN_DROP;
    }

    public boolean cantDrop() {
        return (state & CANT_DROP) == CANT_DROP;
    }

    public void clearObjectIdentified() {
        state &= ~(CONTENT_IDENTIFIED | CAN_DROP | CANT_DROP);
    }

    public void clearRootViewIdentified() {
        state &= ~ROOT_VIEW_IDENTIFIED;
    }

    public void clearViewIdentified() {
        state &= ~(VIEW_IDENTIFIED | CONTENT_IDENTIFIED | CAN_DROP | CANT_DROP);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        String str = "";
        if (state == 0) {
            str = "Normal";
        } else {
            str += isObjectIdentified() ? "Object-Identified " : "";
            str += isViewIdentified() ? "View-identified " : "";
            str += isRootViewIdentified() ? "Root-view-identified " : "";
            str += canDrop() ? "Can-drop " : "";
            str += cantDrop() ? "Cant-drop " : "";
            str += isActive() ? "Active " : "";
            str += isInvalid() ? "Invalid " : "";
            str += isOutOfSynch() ? "Out-of-synch " : "";
            str += " " + Integer.toBinaryString(state);
        }
        return str;
    }

    public void setActive() {
        setFlag(ACTIVE);
    }

    public void setInactive() {
        resetFlag(ACTIVE);
    }

    public boolean isActive() {
        return isFlagSet(ACTIVE);
    }

    private boolean isFlagSet(final short flag) {
        return (state & flag) > 0;
    }

    public void clearInvalid() {
        resetFlag(INVALID);
    }

    private void setFlag(final short flag) {
        state |= flag;
    }

    public void setInvalid() {
        setFlag(INVALID);
    }

    private void resetFlag(final short flag) {
        state &= ~flag;
    }

    public boolean isInvalid() {
        return isFlagSet(INVALID);
    }

    public boolean isOutOfSynch() {
        return isFlagSet(OUT_OF_SYNCH);
    }

    public void setOutOfSynch() {
        setFlag(OUT_OF_SYNCH);
    }

    public void clearOutOfSynch() {
        resetFlag(OUT_OF_SYNCH);
    }

}
