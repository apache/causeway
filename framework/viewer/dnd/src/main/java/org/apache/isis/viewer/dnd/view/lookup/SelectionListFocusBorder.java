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

package org.apache.isis.viewer.dnd.view.lookup;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

// TODO incorporate this big hack into the rewrite of the focus mechanism
class SelectionListFocusBorder extends AbstractBorder {
    final SelectionListAxis axis;

    protected SelectionListFocusBorder(final View view, final SelectionListAxis axis) {
        super(view);
        this.axis = axis;
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (key.getKeyCode() == KeyEvent.VK_DOWN) {
            final View[] subviews = getSubviews();
            for (int i = 0; i < subviews.length; i++) {
                if (subviews[i].getState().isViewIdentified() || i == subviews.length - 1) {
                    subviews[i].exited();
                    subviews[i + 1 >= subviews.length ? 0 : i + 1].entered();
                    break;
                }
            }
        } else if (key.getKeyCode() == KeyEvent.VK_UP) {
            final View[] subviews = getSubviews();
            for (int i = 0; i < subviews.length; i++) {
                if (subviews[i].getState().isViewIdentified() || i == subviews.length - 1) {
                    subviews[i].exited();
                    subviews[i == 0 ? subviews.length - 1 : i - 1].entered();
                    break;
                }
            }
        } else if (key.getKeyCode() == KeyEvent.VK_ENTER) {
            selectOption();
        } else if (key.getKeyCode() == KeyEvent.VK_TAB) {
            selectOption();

            final View view = axis.getOriginalView();
            final View parentView = view.getParent();
            if (key.getModifiers() == InputEvent.SHIFT_MASK) {
                parentView.getFocusManager().focusPreviousView();
            } else {
                parentView.getFocusManager().focusNextView();
            }
        }
    }

    private void selectOption() {
        final View[] subviews = getSubviews();
        for (final View subview : subviews) {
            if (subview.getState().isViewIdentified()) {
                axis.setSelection((OptionContent) subview.getContent());
                final View view = axis.getOriginalView();
                final View parentView = view.getParent();

                // remember which view has the focus so we can re-establish it
                // later
                final View[] parentsSubviews = parentView.getSubviews();
                int index = 0;
                for (int j = 0; j < parentsSubviews.length; j++) {
                    if (view == parentsSubviews[j]) {
                        index = j;
                        break;
                    }
                }

                parentView.updateView();
                parentView.invalidateContent();
                parentView.getFocusManager().setFocus(parentView.getSubviews()[index]);
                getView().dispose();
                break;
            }
        }
    }

    @Override
    public void keyTyped(final KeyboardAction action) {
        final View[] subviews = getSubviews();
        int i;
        int old = 0;
        for (i = 0; i < subviews.length; i++) {
            if (subviews[i].getState().isViewIdentified()) {
                old = i;
                i = i + 1 >= subviews.length ? 0 : i + 1;
                break;
            }
        }
        if (i == subviews.length) {
            i = 0;
        }

        final String startsWith = ("" + action.getKeyCode()).toLowerCase();
        for (int j = i; j < subviews.length; j++) {
            if (subviews[j].getContent().title().toLowerCase().startsWith(startsWith)) {
                subviews[old].exited();
                subviews[j].entered();
                return;
            }
        }
        for (int j = 0; j < i; j++) {
            if (subviews[j].getContent().title().toLowerCase().startsWith(startsWith)) {
                subviews[old].exited();
                subviews[j].entered();
                return;
            }
        }

    }

}
