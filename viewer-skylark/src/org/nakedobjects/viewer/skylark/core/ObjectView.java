 package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.UserContext;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.Color;
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

import org.apache.log4j.Logger;


public abstract class ObjectView extends AbstractView {
    private static final Logger LOG = Logger.getLogger(ObjectView.class);
    
    public ObjectView(Content content, ViewSpecification design, ViewAxis axis) {
        super(content, design, axis);

        if (!(content instanceof ObjectContent) && !(content instanceof CollectionContent)) {
            throw new IllegalArgumentException("Content must be ObjectContent or AssociateContent: " + content);
        }

        getViewManager().addToNotificationList(this);
    }

    private Consent canDrop(NakedObject source, NakedObject target) {
        Action action = dropAction(source, target);
        Session session = ClientSession.getSession();
        if (action != null) {
            Hint about = target.getHint(session, action, new NakedObject[] {source});
            return about.canUse();

        } else {
            if (target.getOid() != null && source.getOid() == null) {
                return new Veto("Can't set field in persistent object with reference to non-persistent object");

            } else {
                NakedObjectField[] fields = target.getSpecification().getVisibleFields(target, session);
                for (int i = 0; i < fields.length; i++) {
                    if (source.getSpecification().isOfType(fields[i].getSpecification())) {
                        if(target.getField(fields[i]) == null) {
                            return new Allow("Set field " + target.getLabel(session, fields[i]));
                        }
                    }
                }
                return new Veto("No empty field accepting object of type " + source.getSpecification().getSingularName());

            }
        }
    }

    public void dispose() {
        getViewManager().removeFromNotificationList(this);
        super.dispose();
    }

    public void dragIn(ContentDrag drag) {
        NakedObject source = ((ObjectContent) drag.getSourceContent()).getObject();
        NakedObject target = (NakedObject) getObject();
        Consent perm = canDrop(source, target);
        if (perm.isAllowed()) {
            getViewManager().setStatus(perm.getReason());
            getState().setCanDrop();
        } else {
            getViewManager().setStatus(perm.getReason());
            getState().setCantDrop();
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

    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (AbstractView.DEBUG) {
            Size size = getSize();
            canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, size.getHeight() / 2, size.getWidth() - 1, size.getHeight() / 2, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, getBaseline(), size.getWidth() - 1, getBaseline(), Color.DEBUG_BASELINE);
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

        NakedObject target = (NakedObject) getObject();
        Assert.assertNotNull(target);

        if (canDrop(source, target).isAllowed()) {
            Action action = dropAction(source, target);

            if ((action != null) && target.getHint(ClientSession.getSession(), action, new NakedObject[] {source}).canUse().isAllowed()) {
                Naked result = target.execute(action, new NakedObject[] {source});

                if (result != null) {
                    View view = ViewFactory.getViewFactory().createOpenRootView(result);
                    Location location = new Location();
                    location.move(10, 10);
                    view.setLocation(location);
                    getWorkspace().addView(view);
                }

                markDamaged();
            } else {
                NakedObjectField[] fields = target.getSpecification().getVisibleFields(target, ClientSession.getSession());
                for (int i = 0; i < fields.length; i++) {
                    if (source.getSpecification().isOfType(fields[i].getSpecification())
                            && target.getField(fields[i]) == null) {
                        target.setAssociation(((NakedObjectAssociation) fields[i]), source);
                        invalidateContent();
                        break;
                    }
                }
            }
        }
    }

    private Action dropAction(NakedObject source, NakedObject target) {
        Action action;
        if (target.getObject() instanceof NakedClass) {
            NakedObjectSpecification forNakedClass = ((NakedClass) target.getObject()).forObjectType();
            action = forNakedClass.getClassAction(Action.USER, null, new NakedObjectSpecification[] { source
                    .getSpecification() });
        } else {
            action = target.getSpecification().getObjectAction(Action.USER, null,
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

    protected Naked getObject() {
        return getContent().getNaked();
    }

    public void secondClick(Click click) {
        View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.secondClick(click);
        } else {
            Location location = getAbsoluteLocation();
            location.translate(click.getLocation());
            getWorkspace().addOpenViewFor(getObject(), location);
        }
    }
    
    public void viewMenuOptions(MenuOptionSet options) {
        if (getObject() instanceof UserContext) {
            options.add(MenuOptionSet.VIEW, new MenuOption("New Workspace") {
                public Consent disabled(View component) {
                    return AbstractConsent.allow(getObject() instanceof UserContext);
                }

                public void execute(Workspace workspace, View view, Location at) {
                    View newWorkspace;
                    newWorkspace = ViewFactory.getViewFactory().createInnerWorkspace((NakedObject) getObject());
                    newWorkspace.setLocation(at);
                    getWorkspace().addView(newWorkspace);
                    newWorkspace.markDamaged();
                }
            });
        }
        
        options.add(MenuOptionSet.DEBUG, new MenuOption("Class") {
            public void execute(Workspace workspace, View view, Location at) {
	 /* TODO reimplement
                return getObjectManager().getNakedClass(getObject().getSpecification());
                */
            }
        });

        options.add(MenuOptionSet.DEBUG, new MenuOption("Clone") {
            public void execute(Workspace workspace, View view, Location at) {
                /* TODO reimplement
                AbstractNakedObject clone = (AbstractNakedObject) createInstance(getClass());
    	        clone.copyObject(this);
    	        clone.objectChanged();
    	        
    	        ViewFactory.getViewFactory().createInnerWorkspace(clone);
                newWorkspace.setLocation(at);
                getWorkspace().addView(newWorkspace);
                newWorkspace.markDamaged();
                */
            }
        });


        super.viewMenuOptions(options);
    }
    
    public void updateView() {
        if(((NakedObject) getObject()).isViewDirty()) {
            LOG.debug("object changed; view updated: " + getView());
            getView().refresh();
            ((NakedObject) getObject()).clearViewDirty();
        }
        super.updateView();
    }
    
    public String toString() {
       return super.toString() + ": " + getContent(); 
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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