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

package org.apache.isis.viewer.dnd.view.option;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;

public class CloseAllViewsForObjectOption extends UserActionAbstract {
    public CloseAllViewsForObjectOption() {
        super("Close all views for this object");
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
        final ObjectAdapter object = view.getContent().getAdapter();
        final View views[] = workspace.getSubviews();
        for (final View v : views) {
            if (v.getContent().getAdapter() == object) {
                v.dispose();
            }
        }
    }

    @Override
    public String getDescription(final View view) {
        final String title = view.getContent().title();
        return "Close all views for '" + title + "'";
    }

}
