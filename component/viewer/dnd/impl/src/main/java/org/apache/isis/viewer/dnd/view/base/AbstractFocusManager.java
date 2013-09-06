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

package org.apache.isis.viewer.dnd.view.base;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.View;

/**
 * Abstract focus manager that uses the set of views to move focus between from
 * the concrete subclass.
 * 
 * @see #getChildViews()
 */
public abstract class AbstractFocusManager implements FocusManager {
    // TODO container to go in subclass ??
    protected View container;
    protected View focus;
    private final View initialFocus;

    public AbstractFocusManager(final View container) {
        this(container, null);
    }

    public AbstractFocusManager(final View container, final View initalFocus) {
        Assert.assertNotNull(container);
        this.container = container;
        this.initialFocus = initalFocus;
        focus = initalFocus;
    }

    /**
     * Throws a ObjectAdapterRuntimeException if the specified view is available
     * to this focus manager.
     */
    private void checkCanFocusOn(final View view) {
        final View[] views = getChildViews();
        boolean valid = view == container.getView();
        for (int j = 0; valid == false && j < views.length; j++) {
            if (views[j] == view) {
                valid = true;
            }
        }

        if (!valid) {
            // throw new ObjectAdapterRuntimeException("No view " + view +
            // " to focus on in " +
            // container.getView());
        }
    }

    @Override
    public void focusFirstChildView() {
        final View[] views = getChildViews();
        for (final View view : views) {
            if (view.canFocus()) {
                setFocus(view);
                return;
            }
        }
        // no other focusable view; stick with the view we've got
        return;
    }

    @Override
    public void focusInitialChildView() {
        if (initialFocus == null) {
            focusFirstChildView();
        } else {
            setFocus(initialFocus);
        }
    }

    @Override
    public void focusLastChildView() {
        final View[] views = getChildViews();
        for (int j = views.length - 1; j > 0; j--) {
            if (views[j].canFocus()) {
                setFocus(views[j]);
                return;
            }
        }
        // no other focusable view; stick with the view we've got
        return;
    }

    @Override
    public void focusNextView() {
        final View[] views = getChildViews();
        for (int i = 0; i < views.length; i++) {
            if (testView(views, i)) {
                for (int j = i + 1; j < views.length; j++) {
                    if (views[j].canFocus()) {
                        setFocus(views[j]);
                        return;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (views[j].canFocus()) {
                        setFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        // throw new ObjectAdapterRuntimeException();
    }

    private boolean testView(final View[] views, final int i) {
        final View view = views[i];
        return view == focus;
    }

    @Override
    public void focusParentView() {
        container.getFocusManager().setFocus(container.getFocusManager().getFocus());
    }

    @Override
    public void focusPreviousView() {
        final View[] views = getChildViews();
        if (views.length > 1) {
            for (int i = 0; i < views.length; i++) {
                if (testView(views, i)) {
                    for (int j = i - 1; j >= 0; j--) {
                        if (views[j].canFocus()) {
                            setFocus(views[j]);
                            return;
                        }
                    }
                    for (int j = views.length - 1; j > i; j--) {
                        if (views[j].canFocus()) {
                            setFocus(views[j]);
                            return;
                        }
                    }
                    // no other focusable view; stick with the view we've got
                    return;
                }
            }

            // Don't move to any view
            // throw new
            // ObjectAdapterRuntimeException("Can't move to previous peer from "
            // + focus);
        }
    }

    protected abstract View[] getChildViews();

    @Override
    public View getFocus() {
        return focus;
    }

    @Override
    public void setFocus(final View view) {
        checkCanFocusOn(view);

        if (view != null && view.canFocus()) {
            if ((focus != null) && (focus != view)) {
                focus.focusLost();
                focus.markDamaged();
            }

            focus = view;
            focus.focusReceived();

            view.markDamaged();
        }
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("container", container);
        str.append("initialFocus", initialFocus);
        str.append("focus", focus);
        return str.toString();
    }

}
