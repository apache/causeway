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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.viewer.dnd.view.Selectable;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAxis;

public class SelectableViewAxis implements ViewAxis {
    private View selectedView;
    private final Selectable target;

    public SelectableViewAxis(final Selectable view) {
        target = view;
    }

    public void selected(final View view) {
        selectedView = view;
        target.setSelectedNode(selectedView);
    }

    public boolean isSelected(final View view) {
        return selectedView == view;
    }

    @Override
    public String toString() {
        final ToString s = new ToString(this);
        s.append("target", target.getId());
        s.append("selected", selectedView == null ? "none" : selectedView.getId());
        return s.toString();
    }
}
