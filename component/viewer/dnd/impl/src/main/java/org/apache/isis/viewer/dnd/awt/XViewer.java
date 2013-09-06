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

package org.apache.isis.viewer.dnd.awt;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.drawing.Background;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.help.HelpViewer;
import org.apache.isis.viewer.dnd.util.Properties;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.InteractionSpy;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.ShutdownListener;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UndoStack;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewUpdateNotifier;
import org.apache.isis.viewer.dnd.view.Viewer;
import org.apache.isis.viewer.dnd.view.base.NullView;
import org.apache.isis.viewer.dnd.view.border.BackgroundBorder;
import org.apache.isis.viewer.dnd.view.border.LineBorder;
import org.apache.isis.viewer.dnd.view.debug.LoggingOptions;
import org.apache.isis.viewer.dnd.view.menu.PopupMenuContainer;
import org.apache.isis.viewer.dnd.view.message.MessageContent;
import org.apache.isis.viewer.dnd.viewer.ApplicationOptions;

public class XViewer implements Viewer {
    private static final Size NO_SIZE = new Size(0, 0);
    private static final Logger LOG = LoggerFactory.getLogger(Viewer.class);
    private static final Logger UI_LOG = LoggerFactory.getLogger("ui." + Viewer.class.getName());
    private static final LoggingOptions LOGGING_OPTIONS = new LoggingOptions();
    private static final NullView CLEAR_OVERLAY = new NullView();
    private static final Bounds NO_REDRAW = new Bounds();

    private ApplicationOptions APPLICATION_OPTIONS;
    private final DebugOptions DEBUG_OPTIONS = new DebugOptions(this);

    private Graphics bufferGraphics;
    private Image doubleBuffer;
    private boolean doubleBuffering = false;
    private Insets insets;
    private Size internalDisplaySize = new Size(1, 1);
    private ShutdownListener listener;
    private View overlayView;
    private final Bounds redrawArea;
    private int redrawCount = 100000;
    private RenderingArea renderingArea;
    private View rootView;
    private String status;
    private boolean runningAsExploration;
    private boolean runningAsPrototype;
    private InteractionSpy spy;
    private int statusBarHeight;
    private final UndoStack undoStack = new UndoStack();
    protected ViewUpdateNotifier updateNotifier;
    private KeyboardManager keyboardManager;
    private HelpViewer helpViewer;
    private Background background;
    private Bounds statusBarArea;
    private XFeedbackManager feedbackManager;
    private boolean refreshStatus;
    public boolean showExplorationMenuByDefault;
    boolean showRepaintArea;
    private static Boolean isDotNetBool;

    private static boolean isDotNet() {
        if (isDotNetBool == null) {
            isDotNetBool = new Boolean(System.getProperty("java.version", "dotnet").equals("dotnet"));
        }
        return isDotNetBool.booleanValue();
    }

    public XViewer() {
        doubleBuffering = IsisContext.getConfiguration().getBoolean(Properties.PROPERTY_BASE + "double-buffer", true);
        showExplorationMenuByDefault = IsisContext.getConfiguration().getBoolean(Properties.PROPERTY_BASE + "exploration.show", true);
        overlayView = CLEAR_OVERLAY;
        redrawArea = new Bounds();
    }

    public void addSpyAction(final String actionMessage) {
        if (spy != null) {
            spy.addAction(actionMessage);
        }
    }

    @Override
    public void addToNotificationList(final View view) {
        updateNotifier.add(view.getView());
    }

    @Override
    public String selectFilePath(final String title, final String directory) {
        return renderingArea.selectFilePath(title, directory);
    }

    @Override
    public void setKeyboardFocus(final View view) {
        if (view == null) {
            return;
        }

        final FocusManager currentFocusManager = keyboardManager.getFocusManager();
        if (currentFocusManager != null && currentFocusManager.getFocus() != null && currentFocusManager.getFocus().getParent() != null) {
            currentFocusManager.getFocus().getParent().markDamaged();
        }

        if (currentFocusManager != null) {
            final View currentFocus = currentFocusManager.getFocus();
            if (currentFocus != null && currentFocus != view) {
                currentFocus.focusLost();
            }
        }

        final FocusManager focusManager = view.getFocusManager();
        if (focusManager != null) {
            focusManager.setFocus(view);
            if (view.getParent() != null) {
                view.getParent().markDamaged();
            }
        }
        if (focusManager == null) {
            LOG.warn("No focus manager for " + view);
        } else {
            keyboardManager.setFocusManager(focusManager);
        }
    }

    @Override
    public void clearOverlayView() {
        overlayView.markDamaged();
        overlayView = CLEAR_OVERLAY;
    }

    @Override
    public void clearOverlayView(final View view) {
        if (this.getOverlayView() != view) {
            LOG.warn("no such view to remove: " + view);
        }
        this.clearOverlayView();
    }

    /*
     * public void clearStatus() { setStatus(""); }
     */
    public void quit() {
        if (spy != null) {
            spy.close();
        }
        DebugFrame.disposeAll();
        if (listener != null) {
            listener.quit();
        }
        close();
    }

    // TODO remove this method; use clearOverlay instead
    public void disposeOverlayView() {
        clearOverlayView();
    }

    @Override
    public void disposeUnneededViews() {
        updateNotifier.removeViewsForDisposedObjects();
    }

    public View dragFrom(final Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.dragFrom(location);
        } else {
            return rootView.dragFrom(location);
        }
    }

    public DragEvent dragStart(final DragStart start) {
        if (onOverlay(start.getLocation())) {
            start.subtract(overlayView.getLocation());
            return overlayView.dragStart(start);
        } else {
            return rootView.dragStart(start);
        }
    }

    public void firstClick(final Click click) {
        /*
         * for (int i = 0; i < panes.length; i++) {
         * if(panes[i].respondsTo(click.getLocation())) {
         * panes[i].firstClick(click); return; } }
         */
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.firstClick(click);
        } else {
            rootView.firstClick(click);
        }
    }

    private FocusManager getFocusManager() {
        return overlayView == CLEAR_OVERLAY ? keyboardManager.getFocusManager() : overlayView.getFocusManager();
    }

    public Bounds getOverlayBounds() {
        final Bounds bounds = new Bounds(createSize(renderingArea.getSize()));
        final Insets in = renderingArea.getInsets();
        bounds.contract(in.left + in.right, in.top + in.bottom);
        bounds.contract(0, statusBarHeight);
        return bounds;
    }

    private Size createSize(final Dimension size) {
        return new Size(size.width, size.height);
    }

    public View getOverlayView() {
        return overlayView;
    }

    @Override
    public InteractionSpy getSpy() {
        return spy;
    }

    @Override
    public UndoStack getUndoStack() {
        return undoStack;
    }

    @Override
    public boolean hasFocus(final View view) {
        final FocusManager focusManager = keyboardManager.getFocusManager();
        return focusManager != null && focusManager.getFocus() == view;
    }

    public View identifyView(final Location location, final boolean includeOverlay) {
        if (includeOverlay && onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.identify(location);
        } else {
            return rootView.identify(location);
        }
    }

    public void init() {
        if (updateNotifier == null) {
            throw new NullPointerException("No update notifier set for " + this);
        }
        if (rootView == null) {
            throw new NullPointerException("No root view set for " + this);
        }

        insets = new Insets(0, 0, 0, 0);

        spy = new InteractionSpy(new SpyWindow());

        keyboardManager = new KeyboardManager(this);
        final InteractionHandler interactionHandler = new InteractionHandler(this, feedbackManager, keyboardManager, spy);
        renderingArea.addMouseMotionListener(interactionHandler);
        renderingArea.addMouseListener(interactionHandler);
        renderingArea.addKeyListener(interactionHandler);

        if (IsisContext.getConfiguration().getBoolean(Properties.PROPERTY_BASE + "show-mouse-spy", false)) {
            spy.open();
        }

        setKeyboardFocus(rootView);

        APPLICATION_OPTIONS = new ApplicationOptions(listener);
    }

    @Override
    public boolean isRunningAsExploration() {
        return runningAsExploration;
    }

    @Override
    public boolean isRunningAsPrototype() {
        return runningAsPrototype;
    }

    public boolean isShowingMouseSpy() {
        return spy.isVisible();
    }

    @Override
    public void markDamaged(final Bounds bounds) {
        if (spy != null) {
            spy.addDamagedArea(bounds);
        }

        synchronized (redrawArea) {
            if (redrawArea.equals(NO_REDRAW)) {
                redrawArea.setBounds(bounds);
                UI_LOG.debug("damage - new area " + redrawArea);
            } else {
                if (!bounds.getSize().equals(NO_SIZE)) {
                    redrawArea.union(bounds);
                    UI_LOG.debug("damage - extend area " + redrawArea + " - to include " + bounds);
                }
            }
        }
    }

    public void menuOptions(final UserActionSet options) {
    }

    public void mouseDown(final Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.mouseDown(click);
        } else {
            rootView.mouseDown(click);
        }
    }

    public void mouseMoved(final Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            overlayView.mouseMoved(location);
        } else {
            rootView.mouseMoved(location);
        }
    }

    public void mouseUp(final Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.mouseUp(click);
        } else {
            rootView.mouseUp(click);
        }
    }

    private boolean onOverlay(final Location mouse) {
        return overlayView.getBounds().contains(mouse);
    }

    public void paint(final Graphics graphic) {
        redrawCount++;
        graphic.translate(insets.left, insets.top);
        final Rectangle paintArea = graphic.getClipBounds();
        final Rectangle layoutArea = layoutViews();
        if (layoutArea != null) {
            paintArea.union(layoutArea);
        }

        if (spy != null) {
            spy.redraw(paintArea.toString(), redrawCount);
        }
        if (UI_LOG.isDebugEnabled()) {
            UI_LOG.debug("------ repaint viewer #" + redrawCount + " " + paintArea.x + "," + paintArea.y + " " + paintArea.width + "x" + paintArea.height);
        }

        final Canvas c = createCanvas(graphic, paintArea);
        if (background != null) {
            background.draw(c.createSubcanvas(), rootView.getSize());
        }

        // paint views
        if (rootView != null) {
            rootView.draw(c.createSubcanvas());
        }
        // paint overlay

        final Bounds bounds = overlayView.getBounds();
        if (paintArea.intersects(new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()))) {
            overlayView.draw(c.createSubcanvas(bounds));
        }

        /*
         * for (int i = 0; i < panes.length; i++) {
         * panes[i].draw(c.createSubcanvas()); }
         */
        // paint status
        // paintUserStatus(bufferGraphics);
        // blat to screen
        if (doubleBuffering) {
            graphic.drawImage(doubleBuffer, 0, 0, null);
        }
        if (showRepaintArea) {
            graphic.setColor(((AwtColor) Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_REPAINT)).getAwtColor());
            graphic.drawRect(paintArea.x, paintArea.y, paintArea.width - 1, paintArea.height - 1);
            graphic.drawString("#" + redrawCount, paintArea.x + 3, paintArea.y + 15);
        }

        // paint status
        paintStatus(graphic);
    }

    private Canvas createCanvas(final Graphics graphic, final Rectangle paintArea) {
        final int w = internalDisplaySize.getWidth();
        final int h = internalDisplaySize.getHeight();
        if (doubleBuffering) {
            if ((doubleBuffer == null) || (bufferGraphics == null) || (doubleBuffer.getWidth(null) < w) || (doubleBuffer.getHeight(null) < h)) {
                doubleBuffer = renderingArea.createImage(w, h);
                LOG.debug("buffer sized to " + doubleBuffer.getWidth(null) + "x" + doubleBuffer.getHeight(null));
            }
            bufferGraphics = doubleBuffer.getGraphics().create();
        } else {
            bufferGraphics = graphic;
        }

        bufferGraphics.clearRect(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        bufferGraphics.clearRect(0, 0, w, h);

        bufferGraphics.setClip(paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        final Canvas c = new AwtCanvas(bufferGraphics, renderingArea, paintArea.x, paintArea.y, paintArea.width, paintArea.height);
        // Canvas c = new Canvas(bufferGraphics, 0, 0, w, h);
        return c;
    }

    /**
     * Lays out the invalid views and returns the area to be repainted.
     */
    private Rectangle layoutViews() {
        if (!Thread.currentThread().getName().startsWith("AWT-EventQueue") && !isDotNet()) {
            // REVIEW remove this check and exception when problem with multiple
            // field drawing is resolved
            // (Bug 1)
            throw new IsisException("Drawing with wrong thread: " + Thread.currentThread());
        }
        // overlayView.layout(new Size(rootView.getSize()));
        // rootView.layout(new Size(rootView.getSize()));
        final Size rootViewSize = rootView.getSize();
        overlayView.layout();
        rootView.layout();
        synchronized (redrawArea) {
            if (!redrawArea.equals(NO_REDRAW)) {
                final Rectangle r2 = new Rectangle(redrawArea.getX(), redrawArea.getY(), redrawArea.getWidth(), redrawArea.getHeight());
                redrawArea.setBounds(NO_REDRAW);
                return r2;
            }
        }
        return null;
    }

    private void paintStatus(final Graphics graphic) {
        final int height = internalDisplaySize.getHeight();
        final int top = height - statusBarHeight;
        if (refreshStatus || graphic.getClip().getBounds().getY() + graphic.getClip().getBounds().getHeight() > top) {
            refreshStatus = false;
            UI_LOG.debug("changed user status " + status + " " + statusBarArea);

            final int width = internalDisplaySize.getWidth();
            graphic.setClip(0, top, width, statusBarHeight);
            graphic.setColor(((AwtColor) Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3)).getAwtColor());
            final AwtText textStyle = (AwtText) Toolkit.getText(ColorsAndFonts.TEXT_STATUS);
            graphic.setFont(textStyle.getAwtFont());
            final int baseline = top + textStyle.getAscent();
            graphic.fillRect(0, top, width, statusBarHeight);
            graphic.setColor(((AwtColor) Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1)).getAwtColor());
            graphic.drawLine(0, top, internalDisplaySize.getWidth(), top);
            // graphic.drawRect(0, top, width - 1, statusBarHeight - 1);
            graphic.setColor(((AwtColor) Toolkit.getColor(ColorsAndFonts.COLOR_BLACK)).getAwtColor());
            graphic.drawString(status, 5, baseline + ViewConstants.VPADDING);
        }
    }

    public View pickupContent(final Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.pickupContent(location);
        } else {
            return rootView.pickupContent(location);
        }
    }

    public View pickupView(final Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.pickupView(location);
        } else {
            return rootView.pickupView(location);
        }
    }

    public void popupMenu(final View over, final Location at, final boolean forView, final boolean includeExploration, final boolean includeDebug) {
        feedbackManager.setBusy(over, null);
        saveCurrentFieldEntry();
        final PopupMenuContainer menu = new PopupMenuContainer(over, at);
        if (over == rootView) {
            menu.addMenuOptions(APPLICATION_OPTIONS);
            menu.addMenuOptions(LOGGING_OPTIONS);
            menu.addMenuOptions(DEBUG_OPTIONS);
        }
        final boolean showExplorationOptions = includeExploration || showExplorationMenuByDefault;
        final boolean showPrototypeOptions = isRunningAsPrototype();
        menu.show(forView, includeDebug, showExplorationOptions, showPrototypeOptions);
        feedbackManager.clearBusy(over);
    }

    @Override
    public void removeFromNotificationList(final View view) {
        updateNotifier.remove(view);
    }

    /**
     * Force a repaint of the damaged area of the viewer.
     */
    @Override
    public void scheduleRepaint() {
        updateNotifier.invalidateViewsForChangedObjects();
        synchronized (redrawArea) {
            if (!redrawArea.equals(NO_REDRAW) || refreshStatus) {
                UI_LOG.debug("repaint viewer " + redrawArea);
                final Bounds area = new Bounds(redrawArea);
                area.translate(insets.left, insets.top);
                renderingArea.repaint(area.getX(), area.getY(), area.getWidth(), area.getHeight());
                redrawArea.setBounds(NO_REDRAW);
            }
        }
    }

    @Override
    public void saveCurrentFieldEntry() {
        final FocusManager focusManager = getFocusManager();
        if (focusManager != null) {
            final View focus = focusManager.getFocus();
            if (focus != null) {
                focus.editComplete(false, false);
                // change should be marked by the field being completed
                // focus.markDamaged();
            }
        }
    }

    public void secondClick(final Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.secondClick(click);
        } else {
            rootView.secondClick(click);
        }
    }

    @Override
    public void setBackground(final Background background) {
        this.background = background;
    }

    void setCursor(final Cursor cursor) {
        renderingArea.setCursor(cursor);
    }

    public void setExploration(final boolean asExploration) {
        this.runningAsExploration = asExploration;
    }

    public void setPrototype(final boolean asPrototype) {
        this.runningAsPrototype = asPrototype;
    }

    public void setListener(final ShutdownListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOverlayView(final View view) {
        disposeOverlayView();
        overlayView = view;
        // TODO ensure that the view is laid out properly; hence is the right
        // size to begin with.
        // view.limitSize(rootView.getSize());

        final Size size = view.getRequiredSize(rootView.getSize());
        // size.ensureWidth(getSize().getWidth());
        view.setSize(size);
        view.layout();

        view.limitBoundsWithin(getOverlaySize());
        overlayView.markDamaged();
    }

    @Override
    public Size getOverlaySize() {
        return rootView.getSize();
    }

    @Override
    public void showInOverlay(final Content content, final Location location) {
        View view;
        view = Toolkit.getViewFactory().createView(new ViewRequirement(content, ViewRequirement.OPEN));
        view = new LineBorder(2, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2), new BackgroundBorder(Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3), view));
        final Size size = view.getRequiredSize(Size.createMax());
        location.subtract(size.getWidth() / 2, size.getHeight() / 2);
        view.setLocation(location);
        setOverlayView(view);
    }

    public void setRenderingArea(final RenderingArea renderingArea) {
        this.renderingArea = renderingArea;
    }

    public void setRootView(final View rootView) {
        this.rootView = rootView;
        rootView.invalidateContent();
    }

    public void setHelpViewer(final HelpViewer helpViewer) {
        this.helpViewer = helpViewer;
    }

    public void setShowMouseSpy(final boolean showDeveloperStatus) {
        if (spy.isVisible()) {
            spy.close();
        } else {
            spy.open();
        }
    }

    public void setUpdateNotifier(final ViewUpdateNotifier updateNotifier) {
        this.updateNotifier = updateNotifier;
    }

    public void showSpy() {
        spy.open();
    }

    public void sizeChange() {
        initSize();
        final View subviews[] = rootView.getSubviews();
        for (final View subview : subviews) {
            subview.invalidateLayout();
        }

        final Bounds bounds = new Bounds(internalDisplaySize);
        markDamaged(bounds);
        scheduleRepaint();

        Properties.saveSizeOption(Properties.PROPERTY_BASE + "initial.size", bounds.getSize());
    }

    public void locationChange(final int x, final int y) {
        Properties.saveLocationOption(Properties.PROPERTY_BASE + "initial.location", new Location(x, y));
    }

    public void initSize() {
        internalDisplaySize = createSize(renderingArea.getSize());
        insets = renderingArea.getInsets();
        LOG.debug("  insets " + insets);
        internalDisplaySize.contract(insets.left + insets.right, insets.top + insets.bottom);
        LOG.debug("  internal " + internalDisplaySize);

        final Size rootViewSize = new Size(internalDisplaySize);
        final Text text = Toolkit.getText(ColorsAndFonts.TEXT_STATUS);
        statusBarHeight = text.getLineHeight() + text.getDescent();
        rootViewSize.contractHeight(statusBarHeight);
        statusBarArea = new Bounds(insets.left, insets.top + rootViewSize.getHeight(), rootViewSize.getWidth(), statusBarHeight);
        rootView.setSize(rootViewSize);
    }

    public void thirdClick(final Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.thirdClick(click);
        } else {
            rootView.thirdClick(click);
        }
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("renderingArea", renderingArea);
        str.append("redrawArea", redrawArea);
        str.append("rootView", rootView);
        return str.toString();
    }

    public void translate(final MouseEvent me) {
        me.translatePoint(-insets.left, -insets.top);
    }

    public ViewAreaType viewAreaType(final Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.viewAreaType(location);
        } else {
            return rootView.viewAreaType(location);
        }
    }

    public boolean isOverlayAvailable() {
        return overlayView != CLEAR_OVERLAY;
    }

    public void makeRootFocus() {
        // makeFocus(rootView);
    }

    public void openHelp(final View forView) {
        if (forView != null) {
            String description = null;
            String help = null;
            String name = null;

            if (forView != null && forView.getContent() != null) {
                final Content content = forView.getContent();
                description = content.getDescription();
                help = content.getHelp();
                name = content.getId();
                name = name == null ? content.title() : name;
            }

            helpViewer.open(forView.getAbsoluteLocation(), name, description, help);

        }

    }

    @Override
    public Object getClipboard(final Class<?> cls) {
        if (cls == String.class) {

            final Clipboard cb = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            final Transferable content = cb.getContents(this);

            String value = "illegal value";
            try {
                value = ((String) content.getTransferData(DataFlavor.stringFlavor));
            } catch (final Throwable e) {
                LOG.error("invalid clipboard operation " + e);
            }
            return value;
        } else {
            return null;
        }
    }

    @Override
    public void setClipboard(final String clip, final Class<?> class1) {
        final Clipboard cb = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(new StringSelection(clip), null);
    }

    public void forcePaintOfStatusBar() {
        status = feedbackManager.getStatusBarOutput();
        refreshStatus = true;
        scheduleRepaint();
    }

    public void showDialog(final MessageContent content) {
        final ViewRequirement requirement = new ViewRequirement(content, ViewRequirement.OPEN);
        final View view = Toolkit.getViewFactory().createView(requirement);
        rootView.getWorkspace().addDialog(view, new Placement(Placement.CENTER));
        scheduleRepaint();
    }

    @Override
    public void showDebugFrame(final DebuggableWithTitle[] info, final Location at) {
        final InfoDebugFrame f = new InfoDebugFrame();
        f.setInfo(info);
        f.show(at.getX(), at.getY());

    }

    @Override
    public void clearAction() {
        feedbackManager.clearAction();
        clearOverlayView();
        // feedbackManager.showDefaultCursor();
    }

    public void setFeedbackManager(final XFeedbackManager feedbackManager) {
        this.feedbackManager = feedbackManager;
    }

    public void close() {
        renderingArea.dispose();
    }

    @Override
    public void saveOpenObjects() {
        final List<ObjectAdapter> objects = new ArrayList<ObjectAdapter>();
        for (final View view : rootView.getSubviews()) {
            final Content content = view.getContent();
            if (content instanceof ObjectContent) {
                objects.add(((ObjectContent) content).getAdapter());
            }
        }
        IsisContext.getUserProfileLoader().saveSession(objects);
    }

}
