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

package org.apache.isis.viewer.dnd.view.window;

import java.util.Enumeration;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.border.ButtonBorder;
import org.apache.isis.viewer.dnd.view.border.SaveTransientObjectBorder;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;
import org.apache.isis.viewer.dnd.view.option.CloseAllViewsForObjectOption;
import org.apache.isis.viewer.dnd.view.option.CloseAllViewsOption;
import org.apache.isis.viewer.dnd.view.option.CloseOtherViewsForObjectOption;
import org.apache.isis.viewer.dnd.view.option.CloseViewOption;
import org.apache.isis.viewer.dnd.view.option.IconizeViewOption;
import org.apache.isis.viewer.dnd.view.option.ReplaceViewOption;

public class WindowBorder extends AbstractWindowBorder {
    private static final UserAction CLOSE_ALL_OPTION = new CloseAllViewsOption();
    private static final UserAction CLOSE_OPTION = new CloseViewOption();
    private static final UserAction CLOSE_VIEWS_FOR_OBJECT = new CloseAllViewsForObjectOption();
    private static final UserAction CLOSE_OTHER_VIEWS_FOR_OBJECT = new CloseOtherViewsForObjectOption();
    private static final IconizeViewOption iconizeOption = new IconizeViewOption();

    public WindowBorder(final View wrappedView, final boolean scrollable) {
        super(addTransientBorderIfNeccessary(scrollable ? new ScrollBorder(wrappedView) : wrappedView));

        if (isTransient()) {
            setControls(new WindowControl[] { new CloseWindowControl(this) });
        } else {
            setControls(new WindowControl[] { new IconizeWindowControl(this), new ResizeWindowControl(this), new CloseWindowControl(this) });
        }
    }

    private static View addTransientBorderIfNeccessary(final View view) {
        final Content content = view.getContent();
        if (content.isPersistable() && content.isTransient()) {
            return new SaveTransientObjectBorder(view);
        } else {
            return view;
        }
    }

    /* TODO fix focus management and remove this hack */
    public View[] getButtons() {
        if (wrappedView instanceof ButtonBorder) {
            return ((ButtonBorder) wrappedView).getButtons();
        } else {
            return new View[0];
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        if (isTransient()) {
            borderRender.drawTransientMarker(canvas, getSize());
        }
    }

    private boolean isTransient() {
        final Content content = getContent();
        return content.isPersistable() && content.isTransient();
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        menuOptions.add(iconizeOption);
        menuOptions.add(CLOSE_OPTION);
        menuOptions.add(CLOSE_ALL_OPTION);
        menuOptions.add(CLOSE_VIEWS_FOR_OBJECT);
        menuOptions.add(CLOSE_OTHER_VIEWS_FOR_OBJECT);

        super.viewMenuOptions(menuOptions);

        final Content content = getContent();
        final UserActionSet suboptions = menuOptions.addNewActionSet("Replace with");
        replaceOptions(Toolkit.getViewFactory().availableViews(new ViewRequirement(content, ViewRequirement.OPEN)), suboptions);
        replaceOptions(Toolkit.getViewFactory().availableViews(new ViewRequirement(content, ViewRequirement.CLOSED)), suboptions);
    }

    protected void replaceOptions(final Enumeration possibleViews, final UserActionSet options) {
        if (possibleViews.hasMoreElements()) {
            while (possibleViews.hasMoreElements()) {
                final ViewSpecification specification = (ViewSpecification) possibleViews.nextElement();
                if (specification != getSpecification()) {
                    options.add(new ReplaceViewOption(specification));
                }
            }
        }
    }

    @Override
    public void secondClick(final Click click) {
        if (overBorder(click.getLocation())) {
            iconizeOption.execute(getWorkspace(), getView(), getAbsoluteLocation());
        } else {
            super.secondClick(click);
        }
    }

    @Override
    protected String title() {
        return getContent().windowTitle();
    }

}
