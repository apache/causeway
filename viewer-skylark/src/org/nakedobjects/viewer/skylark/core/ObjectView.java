package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.UserContext;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.AbstractPermission;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.AssociationSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewDrag;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.DragContentIcon;
import org.nakedobjects.viewer.skylark.util.ViewFactory;


public abstract class ObjectView extends AbstractView {
    public ObjectView(Content content, ViewSpecification design, ViewAxis axis) {
        super(content, design, axis);

        if (!(content instanceof ObjectContent)) {
            throw new IllegalArgumentException("Content must be ObjectContent or AssociateContent: " + content);
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
        ActionSpecification action = dropAction(source, target);
        if (action != null) {
            About about = action.getAbout(ClientSession.getSession(), target, source);

            if (about.canUse().isAllowed()) {
                getViewManager().setStatus(about.getDescription());
                getState().setCanDrop();
            } else {
                getViewManager().setStatus(about.getDescription() + ": " + about.canUse().getReason());
                getState().setCantDrop();
            }
        } else {
            getViewManager().setStatus("");
            getState().setCantDrop();

            FieldSpecification[] fields = target.getSpecification().getVisibleFields(target, ClientSession.getSession());       
            for (int i = 0; i < fields.length; i++) {
                if(source.getSpecification().isOfType(fields[i].getType()) && ((AssociationSpecification) fields[i]).get(target) == null) {
                    getState().setCanDrop();
                    getViewManager().setStatus("Set field " + fields[i].getLabel());
                    break;
                }
            }
        }
        markDamaged();
    }

    public void dragOut(ContentDrag drag) {
        getState().clearObjectIdentified();
        markDamaged();
    }

    public Drag dragStart(DragStart drag) {
        View subview = subviewFor(drag.getLocation());
        if (subview != null) {
            drag.subtract(subview.getLocation());
            return subview.dragStart(drag);
        } else {
            if (drag.isCtrl()) {
                View dragOverlay = new DragViewOutline(getView());
                return new ViewDrag(this, new Offset(drag.getLocation()), dragOverlay);
            } else {
                View dragOverlay = new DragContentIcon(getContent());
                return new ContentDrag(this, drag.getLocation(), dragOverlay);
            }
        }
    }

    /**
     * Called when a dragged object is dropped onto this view. The default
     * behaviour implemented here calls the action method on the target, passing
     * the source object in as the only parameter.
     */
    public void drop(ContentDrag drag) {
        Assert.assertTrue(drag.getSourceContent() instanceof ObjectContent);

        NakedObject source = ((ObjectContent) drag.getSourceContent()).getObject();
        Assert.assertNotNull(source);

        NakedObject target = getObject();
        Assert.assertNotNull(target);

        ActionSpecification action = dropAction(source, target);

        if ((action != null) && action.getAbout(ClientSession.getSession(), target, source).canUse().isAllowed()) {
            NakedObject result = action.execute(target, source);

            if (result != null) {
                View view = ViewFactory.getViewFactory().createOpenRootView(result);
                Location location = new Location();
                location.move(10, 10);
                view.setLocation(location);
                getWorkspace().addView(view);
            }

            markDamaged();
        } else {
            FieldSpecification[] fields = target.getSpecification().getVisibleFields(target, ClientSession.getSession());
            for (int i = 0; i < fields.length; i++) {
                if(source.getSpecification().isOfType(fields[i].getType()) && ((AssociationSpecification) fields[i]).get(target) == null) {
                    ((AssociationSpecification) fields[i]).setAssociation(target, source);
                    invalidateContent();
                    break;
                }
            }
        }
    }

    private ActionSpecification dropAction(NakedObject source, NakedObject target) {
        ActionSpecification action;
        if (target instanceof NakedClass) {
            NakedObjectSpecification forNakedClass = ((NakedClass) target).forNakedClass();
            action = forNakedClass.getClassAction(ActionSpecification.USER, null, new NakedObjectSpecification[] { source
                    .getSpecification() });
        } else {
            action = target.getSpecification().getObjectAction(ActionSpecification.USER, null,
                    new NakedObjectSpecification[] { source.getSpecification() });
        }
        return action;
    }

    public void firstClick(Click click) {
        View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.firstClick(click);
        } else {
            if (click.isButton2() || click.isButton1() && click.isAlt()) {
                View view = ViewFactory.getViewFactory().createOpenRootView(getObject());
                Size size = view.getRequiredSize();
                view.setSize(size);
                Location location = new Location(click.getLocationWithinViewer());
                location.subtract(size.getWidth() / 2, size.getHeight() / 2);
                view.setLocation(location);
                getViewManager().setOverlayView(view);
            }
        }
    }

    protected NakedObject getObject() {
        return ((ObjectContent) getContent()).getObject();
    }

    public void viewMenuOptions(MenuOptionSet options) {
        if (getObject() instanceof UserContext) {
            options.add(MenuOptionSet.VIEW, new MenuOption("New Workspace") {
                public Permission disabled(View component) {
                    return AbstractPermission.allow(getObject() instanceof UserContext);
                }

                public void execute(Workspace workspace, View view, Location at) {
                    View newWorkspace;
                    newWorkspace = ViewFactory.getViewFactory().createInnerWorkspace(getObject());
                    newWorkspace.setLocation(at);
                    getWorkspace().addView(newWorkspace);
                    newWorkspace.markDamaged();
                }
            });
        }

        super.viewMenuOptions(options);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */