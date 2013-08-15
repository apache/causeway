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

package org.apache.isis.viewer.dnd;

import java.awt.Dimension;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfigurationException;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.exploration.AuthenticationRequestExploration;
import org.apache.isis.core.runtime.fixtures.authentication.AuthenticationRequestLogonFixture;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.viewer.IsisViewerAbstract;
import org.apache.isis.viewer.dnd.awt.AwtImageFactory;
import org.apache.isis.viewer.dnd.awt.AwtToolkit;
import org.apache.isis.viewer.dnd.awt.LoginDialog;
import org.apache.isis.viewer.dnd.awt.ViewerFrame;
import org.apache.isis.viewer.dnd.awt.XViewer;
import org.apache.isis.viewer.dnd.calendar.CalendarSpecification;
import org.apache.isis.viewer.dnd.combined.ExpandableListSpecification;
import org.apache.isis.viewer.dnd.combined.FormWithTableSpecification;
import org.apache.isis.viewer.dnd.combined.TwoPartViewSpecification;
import org.apache.isis.viewer.dnd.configurable.ConfigurableObjectViewSpecification;
import org.apache.isis.viewer.dnd.configurable.GridListSpecification;
import org.apache.isis.viewer.dnd.configurable.NewViewSpecification;
import org.apache.isis.viewer.dnd.configurable.PanelViewSpecification;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.field.CheckboxField;
import org.apache.isis.viewer.dnd.field.ColorField;
import org.apache.isis.viewer.dnd.field.DateFieldSpecification;
import org.apache.isis.viewer.dnd.field.EmptyField;
import org.apache.isis.viewer.dnd.field.FieldOfSpecification;
import org.apache.isis.viewer.dnd.field.ImageField;
import org.apache.isis.viewer.dnd.field.PasswordFieldSpecification;
import org.apache.isis.viewer.dnd.field.TextFieldSpecification;
import org.apache.isis.viewer.dnd.form.ExpandableFormSpecification;
import org.apache.isis.viewer.dnd.form.FormSpecification;
import org.apache.isis.viewer.dnd.form.FormWithDetailSpecification;
import org.apache.isis.viewer.dnd.form.InternalFormSpecification;
import org.apache.isis.viewer.dnd.form.SummaryFormSpecification;
import org.apache.isis.viewer.dnd.grid.GridSpecification;
import org.apache.isis.viewer.dnd.help.HelpViewer;
import org.apache.isis.viewer.dnd.help.InternalHelpViewer;
import org.apache.isis.viewer.dnd.histogram.HistogramSpecification;
import org.apache.isis.viewer.dnd.icon.LargeIconSpecification;
import org.apache.isis.viewer.dnd.icon.RootIconSpecification;
import org.apache.isis.viewer.dnd.icon.SubviewIconSpecification;
import org.apache.isis.viewer.dnd.list.InternalListSpecification;
import org.apache.isis.viewer.dnd.list.SimpleListSpecification;
import org.apache.isis.viewer.dnd.service.PerspectiveContent;
import org.apache.isis.viewer.dnd.service.ServiceIconSpecification;
import org.apache.isis.viewer.dnd.table.WindowTableSpecification;
import org.apache.isis.viewer.dnd.tree.ListWithDetailSpecification;
import org.apache.isis.viewer.dnd.tree.TreeSpecification;
import org.apache.isis.viewer.dnd.tree.TreeWithDetailSpecification;
import org.apache.isis.viewer.dnd.tree2.CollectionTreeNodeSpecification;
import org.apache.isis.viewer.dnd.tree2.TreeNodeSpecification;
import org.apache.isis.viewer.dnd.util.Properties;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.ShutdownListener;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.ViewUpdateNotifier;
import org.apache.isis.viewer.dnd.view.base.ViewUpdateNotifierImpl;
import org.apache.isis.viewer.dnd.view.message.DetailedMessageViewSpecification;
import org.apache.isis.viewer.dnd.view.message.MessageDialogSpecification;
import org.apache.isis.viewer.dnd.viewer.SkylarkViewFactory;
import org.apache.isis.viewer.dnd.viewer.basic.DragContentSpecification;
import org.apache.isis.viewer.dnd.viewer.basic.InnerWorkspaceSpecification;
import org.apache.isis.viewer.dnd.viewer.basic.RootWorkspaceSpecification;
import org.apache.isis.viewer.dnd.viewer.basic.WrappedTextFieldSpecification;

public class DndViewer extends IsisViewerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(DndViewer.class);
    private static final String SPECIFICATION_BASE = Properties.PROPERTY_BASE + "specification.";
    private ViewUpdateNotifier updateNotifier;
    private ViewerFrame frame;
    private XViewer viewer;
    private ShutdownListener shutdownListener;
    private Bounds bounds;
    private HelpViewer helpViewer;
    private boolean acceptingLogIns = true;

    // ////////////////////////////////////
    // shutdown
    // ////////////////////////////////////

    @Override
    public void shutdown() {
        System.exit(0);
    }

    private Bounds calculateInitialWindowSize(final Dimension screenSize) {
        int maxWidth = screenSize.width;
        final int maxHeight = screenSize.height;

        if ((screenSize.width / screenSize.height) >= 2) {
            final int f = screenSize.width / screenSize.height;
            maxWidth = screenSize.width / f;
        }

        final int width = maxWidth - 80;
        final int height = maxHeight - 80;
        final int x = 100;
        final int y = 100;

        final Size defaultWindowSize = new Size(width, height);
        defaultWindowSize.limitWidth(800);
        defaultWindowSize.limitHeight(600);

        final Size size = Properties.getSize(Properties.PROPERTY_BASE + "initial.size", defaultWindowSize);
        final Location location = Properties.getLocation(Properties.PROPERTY_BASE + "initial.location", new Location(x, y));
        return new Bounds(location, size);
    }

    private ViewSpecification loadSpecification(final String name, final Class<?> cls) {
        final String factoryName = IsisContext.getConfiguration().getString(SPECIFICATION_BASE + name);
        ViewSpecification spec;
        if (factoryName != null) {
            spec = InstanceUtil.createInstance(factoryName, ViewSpecification.class);
        } else {
            spec = InstanceUtil.createInstance(cls.getName(), ViewSpecification.class);
        }
        return spec;
    }

    private synchronized void logOut() {
        LOG.info("user log out");
        saveDesktop();
        final AuthenticationSession session = IsisContext.getAuthenticationSession();
        getAuthenticationManager().closeSession(session);
        viewer.close();
        notify();
    }

    private void saveDesktop() {
        if (!IsisContext.inSession()) {
            // can't do anything
            return;
        }
        viewer.saveOpenObjects();
    }

    protected void quit() {
        LOG.info("user quit");
        saveDesktop();
        acceptingLogIns = false;
        shutdown();
    }

    @Override
    public synchronized void init() {
        super.init();

        new AwtImageFactory(IsisContext.getTemplateImageLoader());
        new AwtToolkit();

        setShutdownListener(new ShutdownListener() {
            @Override
            public void logOut() {
                DndViewer.this.logOut();
            }

            @Override
            public void quit() {
                DndViewer.this.quit();
            }
        });

        updateNotifier = new ViewUpdateNotifierImpl();

        if (updateNotifier == null) {
            throw new NullPointerException("No update notifier set for " + this);
        }
        if (shutdownListener == null) {
            throw new NullPointerException("No shutdown listener set for " + this);
        }

        while (acceptingLogIns) {
            if (login()) {
                openViewer();
                try {
                    wait();
                } catch (final InterruptedException e) {
                }
            } else {
                quit();
            }
        }
    }

    // ////////////////////////////////////
    // login
    // ////////////////////////////////////

    // TODO: nasty
    private boolean loggedInUsingLogonFixture = false;

    /**
     * TODO: there is similar code in
     * <tt>AuthenticationSessionLookupStrategyDefault</tt>; should try to
     * combine somehow...
     */
    private boolean login() {
        final AuthenticationRequest request = determineRequestIfPossible();

        // we may have enough to get a session
        AuthenticationSession session = getAuthenticationManager().authenticate(request);
        clearAuthenticationRequestViaArgs();

        if (session == null) {
            session = loginDialogPrompt(request);
        }
        if (session == null) {
            return false;
        } else {
            IsisContext.openSession(session);
            return true;
        }
    }

    private AuthenticationSession loginDialogPrompt(final AuthenticationRequest request) {
        AuthenticationSession session;
        final LoginDialog dialog = new LoginDialog(getAuthenticationManager());
        if (request != null) {
            dialog.setUserName(request.getName());
        }
        dialog.setVisible(true);
        dialog.toFront();
        dialog.login();
        dialog.setVisible(false);
        dialog.dispose();
        session = dialog.getSession();
        return session;
    }

    private AuthenticationRequest determineRequestIfPossible() {

        // command line args
        AuthenticationRequest request = getAuthenticationRequestViaArgs();
        ;

        // exploration & (optionally) logon fixture provided
        if (request == null) {
            if (getDeploymentType().isExploring()) {
                request = new AuthenticationRequestExploration(getLogonFixture());
            }
        }

        // logon fixture provided
        if (request == null) {
            if (getLogonFixture() != null && !loggedInUsingLogonFixture) {
                loggedInUsingLogonFixture = true;
                request = new AuthenticationRequestLogonFixture(getLogonFixture());
            }
        }
        return request;
    }

    private void openViewer() {
        frame = new ViewerFrame();

        if (bounds == null) {
            bounds = calculateInitialWindowSize(frame.getToolkit().getScreenSize());
        }

        frame.pack(); // forces insets to be calculated, hence need to then set
                      // bounds
        frame.setBounds(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

        viewer = (XViewer) Toolkit.getViewer();
        viewer.setRenderingArea(frame);
        viewer.setUpdateNotifier(updateNotifier);
        viewer.setListener(shutdownListener);
        viewer.setExploration(isInExplorationMode());
        viewer.setPrototype(isInPrototypeMode());

        if (helpViewer == null) {
            helpViewer = new InternalHelpViewer(viewer);
        }
        viewer.setHelpViewer(helpViewer);

        frame.setViewer(viewer);

        final AuthenticationSession currentSession = IsisContext.getAuthenticationSession();
        if (currentSession == null) {
            throw new NullPointerException("No session for " + this);
        }

        setupViewFactory();

        final UserProfile userProfiler = IsisContext.getUserProfile();

        // TODO viewer should be shown during init() (so login can take place on
        // main window, and can quit
        // before
        // logging in), and should be updated during start to show context.

        // TODO resolving should be done by the views?
        // resolveApplicationContextCollection(rootObject, "services");
        // resolveApplicationContextCollection(rootObject, "objects");
        final RootWorkspaceSpecification spec = new RootWorkspaceSpecification();
        final PerspectiveContent content = new PerspectiveContent(userProfiler.getPerspective());
        if (spec.canDisplay(new ViewRequirement(content, ViewRequirement.CLOSED))) {
            // View view = spec.createView(new RootObject(rootObject), null);
            final View view = spec.createView(content, new Axes(), -1);
            viewer.setRootView(view);
        } else {
            throw new IsisException();
        }

        viewer.init();

        final String name = userProfiler.getPerspective().getName();
        frame.setTitle(name);
        frame.init();

        viewer.initSize();
        viewer.scheduleRepaint();

        frame.setVisible(true);
        frame.toFront();
    }

    private boolean isInExplorationMode() {
        return getDeploymentType().isExploring();
    }

    private boolean isInPrototypeMode() {
        return getDeploymentType().isPrototyping();
    }

    public void setBounds(final Bounds bounds) {
        this.bounds = bounds;
    }

    public void setHelpViewer(final HelpViewer helpViewer) {
        this.helpViewer = helpViewer;
    }

    public void setShutdownListener(final ShutdownListener shutdownListener) {
        this.shutdownListener = shutdownListener;
    }

    private void setupViewFactory() throws IsisConfigurationException, InstanceCreationException {
        final SkylarkViewFactory viewFactory = (SkylarkViewFactory) Toolkit.getViewFactory();

        LOG.debug("setting up default views (provided by the framework)");

        /*
         * viewFactory.addValueFieldSpecification(loadSpecification("field.option"
         * , OptionSelectionField.Specification.class));
         * viewFactory.addValueFieldSpecification
         * (loadSpecification("field.percentage",
         * PercentageBarField.Specification.class));
         * viewFactory.addValueFieldSpecification
         * (loadSpecification("field.timeperiod",
         * TimePeriodBarField.Specification.class));
         */
        viewFactory.addSpecification(loadSpecification("field.image", ImageField.Specification.class));
        viewFactory.addSpecification(loadSpecification("field.color", ColorField.Specification.class));
        viewFactory.addSpecification(loadSpecification("field.password", PasswordFieldSpecification.class));
        viewFactory.addSpecification(loadSpecification("field.wrappedtext", WrappedTextFieldSpecification.class));
        viewFactory.addSpecification(loadSpecification("field.checkbox", CheckboxField.Specification.class));
        viewFactory.addSpecification(loadSpecification("field.date", DateFieldSpecification.class));
        viewFactory.addSpecification(loadSpecification("field.text", TextFieldSpecification.class));
        viewFactory.addSpecification(new RootWorkspaceSpecification());
        viewFactory.addSpecification(new InnerWorkspaceSpecification());

        if (IsisContext.getConfiguration().getBoolean(SPECIFICATION_BASE + "defaults", true)) {
            viewFactory.addSpecification(new FieldOfSpecification());

            viewFactory.addSpecification(new InternalListSpecification());
            viewFactory.addSpecification(new SimpleListSpecification());
            viewFactory.addSpecification(new GridSpecification());
            // TBA viewFactory.addSpecification(new
            // ListWithExpandableElementsSpecification());
            // TBA
            viewFactory.addSpecification(new CalendarSpecification());
            viewFactory.addSpecification(new ListWithDetailSpecification());
            viewFactory.addSpecification(new HistogramSpecification());

            viewFactory.addSpecification(new TreeWithDetailSpecification());
            viewFactory.addSpecification(new FormSpecification());
            viewFactory.addSpecification(new FormWithTableSpecification());
            viewFactory.addSpecification(new WindowTableSpecification());
            // TBA
            viewFactory.addSpecification(new ExpandableFormSpecification());
            viewFactory.addSpecification(new InternalFormSpecification());
            viewFactory.addSpecification(new TwoPartViewSpecification());
            // TBA
            viewFactory.addSpecification(new FormWithDetailSpecification());

            viewFactory.addSpecification(new SummaryFormSpecification());

            viewFactory.addSpecification(new TreeSpecification());
            // TODO allow window form to be used for objects with limited number
            // of collections
            // viewFactory.addSpecification(new TreeWithDetailSpecification(0,
            // 3));

            viewFactory.addDesignSpecification(new GridListSpecification());
            viewFactory.addDesignSpecification(new ConfigurableObjectViewSpecification());
            viewFactory.addDesignSpecification(new PanelViewSpecification());
            viewFactory.addDesignSpecification(new NewViewSpecification());
        }

        viewFactory.addSpecification(new MessageDialogSpecification());
        viewFactory.addSpecification(new DetailedMessageViewSpecification());

        viewFactory.addEmptyFieldSpecification(loadSpecification("field.empty", EmptyField.Specification.class));

        viewFactory.addSpecification(loadSpecification("icon.object", RootIconSpecification.class));
        viewFactory.addSpecification(loadSpecification("icon.subview", SubviewIconSpecification.class));
        viewFactory.addSpecification(loadSpecification("icon.collection", ExpandableListSpecification.class));
        viewFactory.addSpecification(new LargeIconSpecification());

        viewFactory.addSpecification(loadSpecification("icon.service", ServiceIconSpecification.class));
        viewFactory.setDragContentSpecification(loadSpecification("drag-content", DragContentSpecification.class));

        // TODO remove or move to better position
        final ViewSpecification[] specifications = CollectionTreeNodeSpecification.create();
        viewFactory.addSpecification(specifications[0]);
        viewFactory.addSpecification(specifications[1]);
        viewFactory.addSpecification(new TreeNodeSpecification());

        installSpecsFromConfiguration(viewFactory);

        viewFactory.loadUserViewSpecifications();
    }

    private void installSpecsFromConfiguration(final SkylarkViewFactory viewFactory) {
        final String viewParams = IsisContext.getConfiguration().getString(SPECIFICATION_BASE + "view");
        if (viewParams != null) {
            final StringTokenizer st = new StringTokenizer(viewParams, ",");
            while (st.hasMoreTokens()) {
                final String specName = st.nextToken().trim();
                if (specName != null && !specName.trim().equals("")) {
                    viewFactory.addSpecification(specName);
                }
            }
        }
    }

}
