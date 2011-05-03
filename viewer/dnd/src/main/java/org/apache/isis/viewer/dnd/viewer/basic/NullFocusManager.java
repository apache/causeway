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

package org.apache.isis.viewer.dnd.viewer.basic;

import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.View;

public class NullFocusManager implements FocusManager {
    private View focus;

    @Override
    public void focusNextView() {
    }

    @Override
    public void focusPreviousView() {
    }

    @Override
    public void focusParentView() {
    }

    @Override
    public void focusFirstChildView() {
    }

    @Override
    public void focusLastChildView() {
    }

    @Override
    public void focusInitialChildView() {
    }

    @Override
    public View getFocus() {
        return focus;
    }

    @Override
    public void setFocus(final View view) {
        focus = view;
    }

}
