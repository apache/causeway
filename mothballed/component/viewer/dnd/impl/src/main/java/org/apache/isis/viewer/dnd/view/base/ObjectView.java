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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.interaction.ViewDragImpl;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewSpecification;

public abstract class ObjectView extends AbstractView {

    public ObjectView(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        final Consent consent = getContent().canDrop(drag.getSourceContent());
        final String description = getContent().getDescription();
        if (consent.isAllowed()) {
            getFeedbackManager().setAction(consent.getDescription() + " " + description);
            getState().setCanDrop();
        } else {
            getFeedbackManager().setAction(consent.getReason() + " " + description);
            getState().setCantDrop();
        }
        markDamaged();
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        getState().clearObjectIdentified();
        markDamaged();
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        final View subview = subviewFor(drag.getLocation());
        if (subview != null) {
            drag.subtract(subview.getLocation());
            return subview.dragStart(drag);
        } else {
            if (drag.isCtrl()) {
                final View dragOverlay = new DragViewOutline(getView());
                return new ViewDragImpl(this, new Offset(drag.getLocation()), dragOverlay);
            } else {
                return Toolkit.getViewFactory().createDragContentOutline(this, drag.getLocation());
            }
        }
    }

    /**
     * Called when a dragged object is dropped onto this view. The default
     * behaviour implemented here calls the action method on the target, passing
     * the source object in as the only parameter.
     */
    @Override
    public void drop(final ContentDrag drag) {
        final ObjectAdapter result = getContent().drop(drag.getSourceContent());
        if (result != null) {
            objectActionResult(result, new Placement(this));
        }
        getState().clearObjectIdentified();
        getFeedbackManager().showMessagesAndWarnings();

        markDamaged();
    }

    @Override
    public void firstClick(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.firstClick(click);
        } else {
            if (click.button2()) {
                final Location location = new Location(click.getLocationWithinViewer());
                getViewManager().showInOverlay(getContent(), location);
            }
        }
    }

    @Override
    public void invalidateContent() {
        super.invalidateLayout();
    }

    @Override
    public void secondClick(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.secondClick(click);
        } else {
            final Location location = getAbsoluteLocation();
            location.translate(click.getLocation());
            getWorkspace().addWindowFor(getContent().getAdapter(), new Placement(this));
        }
    }


    // ///////////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////////////////

    private static Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
