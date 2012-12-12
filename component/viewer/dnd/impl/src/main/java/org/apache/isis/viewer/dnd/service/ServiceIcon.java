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

package org.apache.isis.viewer.dnd.service;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.icon.Icon;
import org.apache.isis.viewer.dnd.util.Properties;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.action.OptionFactory;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.option.CloseViewOption;
import org.apache.isis.viewer.dnd.view.text.ObjectTitleText;

public class ServiceIcon extends Icon {
    private final static int ICON_SIZE;
    private final static int LARGE_ICON_SIZE = 34;
    private final static String LARGE_ICON_SIZE_PROPERTY;

    static {
        LARGE_ICON_SIZE_PROPERTY = Properties.PROPERTY_BASE + "large-icon-size";
        ICON_SIZE = IsisContext.getConfiguration().getInteger(LARGE_ICON_SIZE_PROPERTY, LARGE_ICON_SIZE);
    }

    public ServiceIcon(final Content content, final ViewSpecification specification) {
        super(content, specification);
        setTitle(new ObjectTitleText(this, Toolkit.getText(ColorsAndFonts.TEXT_ICON)));
        setSelectedGraphic(new IconGraphic(this, ICON_SIZE));
        setVertical(true);
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
        options.setColor(Toolkit.getColor(ColorsAndFonts.COLOR_MENU_CONTENT));
        OptionFactory.addObjectMenuOptions(getContent().getAdapter(), options);
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        options.setColor(Toolkit.getColor(ColorsAndFonts.COLOR_MENU_VIEW));

        options.add(new CloseViewOption() {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final PerspectiveContent parent = (PerspectiveContent) view.getParent().getContent();
                final PerspectiveEntry perspective = parent.getPerspective();
                final ServiceObject serviceContent = (ServiceObject) view.getContent();
                final ObjectAdapter element = serviceContent.getObject();
                perspective.removeFromServices(element);
                super.execute(workspace, view, at);
            }
        });
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        return Toolkit.getViewFactory().createDragContentOutline(this, drag.getLocation());
    }
}
