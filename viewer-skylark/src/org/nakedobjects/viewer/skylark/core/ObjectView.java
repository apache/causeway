package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedClassSpec;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.util.ViewFactory;

import org.apache.log4j.Logger;


public abstract class ObjectView extends AbstractView {
    private static final Logger LOG = Logger.getLogger(AbstractView.class);

    public ObjectView(Content content, ViewSpecification design, ViewAxis axis) {
        super(content, design, axis);

        if (!(content instanceof ObjectContent)) {
            throw new IllegalArgumentException(
                "Content must be ObjectContent or AssociateContent: " + content);
        }

        getViewManager().addToNotificationList(this);
    }

    public void dispose() {
        getViewManager().removeFromNotificationList(this);
        super.dispose();
    }
    
    public void dragIn(ContentDrag drag) {
        NakedObject source = ((ObjectContent) drag.getSourceContent()).getObject();

        NakedObject target = getObject();

        ActionSpecification action = dropAction(source, target); //object.getNakedClass().getObjectAction(Action.USER, null, new NakedClass[] {source.getNakedClass()});

        if (action != null) {
            About about = action.getAbout(Session.getSession().getContext(), target, source);

            if (about.canUse().isAllowed()) {
                getViewManager().setStatus(about.getDescription());
                getState().setCanDrop();
            } else {
                getViewManager().setStatus(about.getDescription() + ": " +
                    about.canUse().getReason());
                getState().setCantDrop();
            }
        } else {
            getViewManager().setStatus("");
            getState().setCantDrop();
        }
        markDamaged();
    }
    
    public void dragOut(ContentDrag drag) {
        getState().clearObjectIdentified();
        markDamaged();
    }

    /**
     * Called when a dragged object is dropped onto this view.  The default
     * behaviour implemented here calls the action method on the target, passing the
     * source object in as the only parameter.
     */
    public void drop(ContentDrag drag) {
        Assert.assertTrue(drag.getSourceContent() instanceof ObjectContent);

        NakedObject source = ((ObjectContent) drag.getSourceContent()).getObject();
        Assert.assertNotNull(source);

        NakedObject target = getObject();
        Assert.assertNotNull(target);

        ActionSpecification action = dropAction(source, target);
        
        if ((action != null) &&
                action.getAbout(Session.getSession().getContext(), target, source).canUse().isAllowed()) {
            NakedObject result = action.execute(target, source);

            if (result != null) {
                View view = ViewFactory.getViewFactory().createOpenRootView(result);
                Location location = drag.getPointerLocation();
                Location offset = getWorkspace().getLocationWithinViewer();
                location.move(-offset.getX(), -offset.getY());
                view.setLocation(location);
                getWorkspace().addView(view);
            }

            markDamaged();
        }
    }

    private ActionSpecification dropAction(NakedObject source, NakedObject target) {
        ActionSpecification action;
        if(target instanceof NakedClassSpec) {
            NakedObjectSpecification forNakedClass = ((NakedClassSpec) target).forNakedClass();
            action = forNakedClass.getClassAction(ActionSpecification.USER, null, new NakedObjectSpecification[] {source.getSpecification()});
        } else {
            action = target.getSpecification().getObjectAction(ActionSpecification.USER, null, new NakedObjectSpecification[] {source.getSpecification()});
        }
        return action;
    }

    protected NakedObject getObject() {
        return ((ObjectContent) getContent()).getObject();
    }

    public View pickup(ContentDrag drag) {
        View dragView;

        dragView = ViewFactory.getViewFactory().createContentDragIcon(drag);

    	getViewManager().setOverlayView(dragView);
        LOG.debug("drag object start " + drag.getSourceLocationWithinViewer());
        getViewManager().setStatus("Dragging " + getContent());

        return dragView;
    }

    public void viewMenuOptions(MenuOptionSet options) {
        super.viewMenuOptions(options);

        options.add(MenuOptionSet.VIEW,
            new MenuOption("New Workspace") {
                public void execute(Workspace workspace, View view, Location at) {
                    View newWorkspace;
                    newWorkspace = ViewFactory.getViewFactory().createWorkspace(getObject());
                    newWorkspace.setLocation(at);
                    getWorkspace().addView(newWorkspace);
                 //   newWorkspace.setSize(new Size(200, 100));
                    newWorkspace.markDamaged();
                }
            });
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/