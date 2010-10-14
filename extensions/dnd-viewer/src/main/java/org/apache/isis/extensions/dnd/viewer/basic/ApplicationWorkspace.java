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


package org.apache.isis.extensions.dnd.viewer.basic;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectList;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Allow;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Padding;
import org.apache.isis.extensions.dnd.service.PerspectiveContent;
import org.apache.isis.extensions.dnd.service.ServiceObject;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Click;
import org.apache.isis.extensions.dnd.view.CompositeViewSpecification;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ContentDrag;
import org.apache.isis.extensions.dnd.view.DragEvent;
import org.apache.isis.extensions.dnd.view.DragStart;
import org.apache.isis.extensions.dnd.view.Feedback;
import org.apache.isis.extensions.dnd.view.Look;
import org.apache.isis.extensions.dnd.view.ObjectContent;
import org.apache.isis.extensions.dnd.view.Placement;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewDrag;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.composite.CompositeViewUsingBuilder;
import org.apache.isis.extensions.dnd.view.content.FieldContent;
import org.apache.isis.extensions.dnd.view.look.LookFactory;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;
import org.apache.isis.extensions.dnd.view.window.DialogBorder;
import org.apache.isis.extensions.dnd.view.window.SubviewFocusManager;
import org.apache.isis.extensions.dnd.view.window.WindowBorder;
import org.apache.isis.runtime.authentication.standard.exploration.MultiUserExplorationSession;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.userprofile.PerspectiveEntry;


public final class ApplicationWorkspace extends CompositeViewUsingBuilder implements Workspace {
    protected Vector<View> serviceViews;
    protected Vector<View> iconViews;

    public ApplicationWorkspace(final Content content, final Axes axes, final CompositeViewSpecification specification, Layout layout, ApplicationWorkspaceBuilder builder) {
        super(content, specification, axes, layout, builder);
        serviceViews = new Vector<View>();
        iconViews = new Vector<View>();
        LookFactory.init();
    }

    public void addDialog(final View dialogContent, Placement placement) {
        DialogBorder dialogView = new DialogBorder(dialogContent, false);
        addView(dialogView);
        placement.position(this, dialogView);
        // dialogView.setFocusManager( new SubviewFocusManager(dialogView));
    }

    public void addWindow(View containedView, Placement placement) {
        boolean scrollable = !containedView.getSpecification().isResizeable();
        WindowBorder windowView = new WindowBorder(containedView, scrollable);
        addView(windowView);
        placement.position(this, windowView);
        windowView.setFocusManager(new SubviewFocusManager(windowView));
    }

    @Override
    public void addView(final View view) {
        super.addView(view);
        getViewManager().setKeyboardFocus(view);
        view.getFocusManager().focusFirstChildView();
    }

    @Override
    public void replaceView(final View toReplace, final View replacement) {
        if (replacement.getSpecification().isOpen()) {
            boolean scrollable = !replacement.getSpecification().isResizeable();
            WindowBorder windowView = new WindowBorder(replacement, scrollable);
            super.replaceView(toReplace, windowView);
        } else {
            super.replaceView(toReplace, replacement);
        }
    }

    public View addWindowFor(final ObjectAdapter object, Placement placement) {
        final Content content = Toolkit.getContentFactory().createRootContent(object);
        final View view = Toolkit.getViewFactory().createView(new ViewRequirement(content, ViewRequirement.OPEN));
        addWindow(view, placement);
        getViewManager().setKeyboardFocus(view);
        return view;
    }

    public View addIconFor(final ObjectAdapter object, Placement placement) {
        final Content content = Toolkit.getContentFactory().createRootContent(object);
        View icon = Toolkit.getViewFactory().createView(
                new ViewRequirement(content, ViewRequirement.CLOSED | ViewRequirement.ROOT));
        add(iconViews, icon);
        placement.position(this, icon);
        return icon;
    }

    public void addServiceIconFor(final ObjectAdapter service) {
        final Content content = new ServiceObject(service);
        final View serviceIcon = Toolkit.getViewFactory().createView(
                new ViewRequirement(content, ViewRequirement.CLOSED | ViewRequirement.SUBVIEW));
        add(serviceViews, serviceIcon);
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        final View subview = subviewFor(drag.getLocation());
        if (subview != null) {
            drag.subtract(subview.getLocation());
            return subview.dragStart(drag);
        } else {
            return null;
        }
    }

    // TODO check the dragging in of objects, flag to user that object cannot be dropped
    @Override
    public void drop(final ContentDrag drag) {
        getFeedbackManager().showDefaultCursor();

        if (!drag.getSourceContent().isObject()) {
            return;
        }

        if (drag.getSourceContent().getAdapter() == getPerspective()) {
            getFeedbackManager().setAction("can' drop self on workspace");
            return;
        }

        final ObjectAdapter source = ((ObjectContent) drag.getSourceContent()).getObject();
        if (source.getSpecification().isService()) {
            getPerspective().addToServices(source.getObject());
            invalidateContent();
        } else {
            if (!drag.isShift()) {
                getPerspective().addToObjects(source.getObject());
            }
        }

        View newView;
        if (source.getSpecification().isService()) {
            return;
        } else {
            final Location dropLocation = drag.getTargetLocation();
            dropLocation.subtract(drag.getOffset());

            if (drag.isShift()) {
                newView = Toolkit.getViewFactory().createView(new ViewRequirement(getContent(), ViewRequirement.OPEN | ViewRequirement.SUBVIEW));
                drag.getTargetView().addView(newView);
                newView.setLocation(dropLocation);
            } else {
                // place object onto desktop as icon
                final View sourceView = drag.getSource();
                if (!sourceView.getSpecification().isOpen()) {
                    final View[] subviews = getSubviews();
                    for (int i = 0; i < subviews.length; i++) {
                        if (subviews[i] == sourceView) {
                            sourceView.markDamaged();
                            sourceView.setLocation(dropLocation);
                            sourceView.markDamaged();
                            return;
                        }
                    }
                } else {
                    for (View view : iconViews) {
                        if (view.getContent().getAdapter() == source) {
                            view.markDamaged();
                            view.setLocation(dropLocation);
                            view.markDamaged();
                            return;
                        }
                    }
                }
                addIconFor(source, new Placement(dropLocation));
            }
        }
    }

    @Override
    public void entered() {
    // prevents status details about "Persective..."
    }

    private PerspectiveEntry getPerspective() {
        return ((PerspectiveContent) getContent()).getPerspective();
    }

    @Override
    public void drop(final ViewDrag drag) {
        getFeedbackManager().showDefaultCursor();

        final View sourceView = drag.getSourceView();
        final Location newLocation = drag.getViewDropLocation();
        if (sourceView.getSpecification() != null && sourceView.getSpecification().isSubView()) {
            if (sourceView.getSpecification().isOpen() && sourceView.getSpecification().isReplaceable()) {
                // TODO remove the open view from the container and place on
                // workspace; replace the internal view with an icon
            } else if (sourceView.getContent() instanceof FieldContent) {
                ViewRequirement requirement = new ViewRequirement(sourceView.getContent(), ViewRequirement.OPEN);
                View view = Toolkit.getViewFactory().createView(requirement);
                addWindow(view, new Placement(newLocation));
                sourceView.getState().clearViewIdentified();
            } else {
                addWindowFor(sourceView.getContent().getAdapter(), new Placement(newLocation));
                sourceView.getState().clearViewIdentified();
            }
        } else {
            sourceView.markDamaged();
            sourceView.setLocation(newLocation);
            sourceView.limitBoundsWithin(getSize());
            sourceView.markDamaged();
        }
    }

    @Override
    public Padding getPadding() {
        return new Padding();
    }

    @Override
    public Workspace getWorkspace() {
        return this;
    }

    public void lower(final View view) {
        if (views.contains(view)) {
            views.removeElement(view);
            views.insertElementAt(view, 0);
            markDamaged();
        }
    }

    public void raise(final View view) {
        if (views.contains(view)) {
            views.removeElement(view);
            views.addElement(view);
            markDamaged();
        }
    }

    @Override
    public void removeView(final View view) {
        view.markDamaged();
        if (iconViews.contains(view)) {
            iconViews.remove(view);
            getViewManager().removeFromNotificationList(view);
            removeObject(view.getContent().getAdapter());
        } else if (serviceViews.contains(view)) {
            serviceViews.remove(view);
            getViewManager().removeFromNotificationList(view);
            removeService(view.getContent().getAdapter());
        } else {
            super.removeView(view);
        }
    }

    private void removeService(ObjectAdapter object) {
        getPerspective().removeFromServices(object.getObject());
    }

    private void removeObject(final ObjectAdapter object) {
        getPerspective().removeFromObjects(object.getObject());
    }

    @Override
    public void secondClick(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            // ignore double-click on self - don't open up new view
            super.secondClick(click);
        }
    }

    @Override
    public String toString() {
        return "Workspace" + getId();
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        options.setColor(Toolkit.getColor(ColorsAndFonts.COLOR_MENU_WORKSPACE));

        options.add(new UserActionAbstract("Close all") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final View views[] = getWindowViews();
                for (int i = 0; i < views.length; i++) {
                    final View v = views[i];
                    // if (v.getSpecification().isOpen()) {
                    v.dispose();
                    // }
                }
                markDamaged();
            }
        });

        options.add(new UserActionAbstract("Tidy up windows") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                tidyViews(getWindowViews());
            }
        });

        options.add(new UserActionAbstract("Tidy up icons") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                tidyViews(getObjectIconViews());
            }
        });

        options.add(new UserActionAbstract("Tidy up all") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                tidyViews(getObjectIconViews());
                tidyViews(getWindowViews());
            }
        });

        options.add(new UserActionAbstract("Services...") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final List<Object> services = IsisContext.getServices();
                ObjectAdapter[] serviceObjects = new ObjectAdapter[services.size()];
                int i = 0;
                for (Object object : services) {
                    AdapterManager adapterManager = IsisContext.getPersistenceSession().getAdapterManager();
                    serviceObjects[i++] = adapterManager.adapterFor(object);
                }
                final ObjectSpecification spec = getSpecificationLoader().loadSpecification(Object.class);
                final ObjectList collection = new ObjectList(spec, serviceObjects);
                addWindowFor(getAdapterManager().adapterFor(collection), new Placement(at));
            }
        });

        menuForChangingLook(options);

        menuForChangingUsers(options);
        
        options.add(new UserActionAbstract("Save User Profile", ObjectActionType.USER) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                Feedback feedbackManager = getFeedbackManager();
                feedbackManager.showBusyState(ApplicationWorkspace.this);
                getViewManager().saveOpenObjects();
                feedbackManager.addMessage("Profile saved");
                feedbackManager.showBusyState(ApplicationWorkspace.this);
            }
        });
    }

    private void menuForChangingLook(UserActionSet options) {
        final UserActionSet set = options.addNewActionSet("Change Look", ObjectActionType.USER);
        for (Look look : LookFactory.getAvailableLooks()) {
            menuOptionForChangingLook(set, look, look.getName());
        }
   }

    private void menuOptionForChangingLook(final UserActionSet set, final Look look, final String name) {
        set.add(new UserActionAbstract(name) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                LookFactory.setLook(look);
                ApplicationWorkspace.this.invalidateLayout();
                ApplicationWorkspace.this.markDamaged();
           }

            @Override
            public Consent disabled(View view) {
                return LookFactory.getInstalledLook() == look ? new Veto("Current look") : Allow.DEFAULT;
            }
        });
    }


    private void menuForChangingUsers(final UserActionSet options) {
        // TODO pick out users from the perspectives, but only show when in exploration mode
        if (getAuthenticationSession() instanceof MultiUserExplorationSession) {
            MultiUserExplorationSession session = (MultiUserExplorationSession) getAuthenticationSession();

            Set<String> users = session.getUserNames();
            final UserActionSet set = options.addNewActionSet("Change user", ObjectActionType.EXPLORATION);
            for (String user : users) {
                menuOptionForChangingUser(set, user, session.getUserName());
            }
        }
    }

    private void menuOptionForChangingUser(final UserActionSet set, final String user, final String currentUser) {
        set.add(new UserActionAbstract(user) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final MultiUserExplorationSession session = (MultiUserExplorationSession) getAuthenticationSession();
                session.setCurrentSession(user);
            }

            @Override
            public Consent disabled(View view) {
                return user.equals(currentUser) ? new Veto("Current user") : Allow.DEFAULT;
            }
        });
    }

    @Override
    protected View[] subviews() {
        Object[] viewsCopy = views.toArray();
        Object[] serviceViewsCopy = serviceViews.toArray();
        Object[] iconViewsCopy = iconViews.toArray();

        View v[] = new View[viewsCopy.length + serviceViewsCopy.length + iconViewsCopy.length];
        int offset = 0;
        Object[] src = serviceViewsCopy;
        System.arraycopy(src, 0, v, offset, src.length);
        offset += src.length;
        src = iconViewsCopy;
        System.arraycopy(src, 0, v, offset, src.length);
        offset += src.length;
        src = viewsCopy;
        System.arraycopy(src, 0, v, offset, src.length);

        return v;
    }

    public void clearServiceViews() {
        final Enumeration e = serviceViews.elements();
        while (e.hasMoreElements()) {
            final View view = (View) e.nextElement();
            view.markDamaged();
        }
        serviceViews.clear();
    }

    protected View[] getWindowViews() {
        return createArrayOfViews(views);
    }

    private View[] createArrayOfViews(final Vector views) {
        final View[] array = new View[views.size()];
        views.copyInto(array);
        return array;
    }

    protected View[] getServiceIconViews() {
        return createArrayOfViews(serviceViews);
    }

    protected View[] getObjectIconViews() {
        return createArrayOfViews(iconViews);
    }

    private void tidyViews(final View[] views) {
        for (int i = 0; i < views.length; i++) {
            final View v = views[i];
            v.setLocation(ApplicationWorkspaceBuilder.UNPLACED);
        }
        invalidateLayout();
        markDamaged();
    }

    // //////////////////////////////////////////////////////////////////
    // Dependencies (from singleton)
    // //////////////////////////////////////////////////////////////////

    private SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

}
