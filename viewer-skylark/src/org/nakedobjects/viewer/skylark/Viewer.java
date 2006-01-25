package org.nakedobjects.viewer.skylark;

import org.nakedobjects.event.ObjectViewingMechanismListener;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.undo.UndoStack;
import org.nakedobjects.utility.DebugFrame;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.InfoDebugFrame;
import org.nakedobjects.utility.ToString;
import org.nakedobjects.utility.configuration.ComponentException;
import org.nakedobjects.utility.configuration.ComponentLoader;
import org.nakedobjects.utility.configuration.ConfigurationException;
import org.nakedobjects.viewer.skylark.basic.EmptyField;
import org.nakedobjects.viewer.skylark.basic.NullView;
import org.nakedobjects.viewer.skylark.basic.RootIconSpecification;
import org.nakedobjects.viewer.skylark.basic.SubviewIconSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.DebugDumpSnapshotOption;
import org.nakedobjects.viewer.skylark.core.DebugOption;
import org.nakedobjects.viewer.skylark.core.DefaultPopupMenu;
import org.nakedobjects.viewer.skylark.core.DrawingCanvas;
import org.nakedobjects.viewer.skylark.core.OverlayDebugFrame;
import org.nakedobjects.viewer.skylark.metal.ClassIcon;
import org.nakedobjects.viewer.skylark.metal.DetailedMessageViewSpecification;
import org.nakedobjects.viewer.skylark.metal.ExceptionMessageContent;
import org.nakedobjects.viewer.skylark.metal.FormSpecification;
import org.nakedobjects.viewer.skylark.metal.ListSpecification;
import org.nakedobjects.viewer.skylark.metal.MessageDialogSpecification;
import org.nakedobjects.viewer.skylark.metal.PasswordFieldSpecification;
import org.nakedobjects.viewer.skylark.metal.TableSpecification;
import org.nakedobjects.viewer.skylark.metal.TextFieldSpecification;
import org.nakedobjects.viewer.skylark.metal.TextMessageContent;
import org.nakedobjects.viewer.skylark.metal.TreeBrowserSpecification;
import org.nakedobjects.viewer.skylark.metal.WrappedTextFieldSpecification;
import org.nakedobjects.viewer.skylark.special.DataFormSpecification;
import org.nakedobjects.viewer.skylark.special.InnerWorkspaceSpecification;
import org.nakedobjects.viewer.skylark.special.WorkspaceSpecification;
import org.nakedobjects.viewer.skylark.util.ViewFactory;
import org.nakedobjects.viewer.skylark.value.CheckboxField;
import org.nakedobjects.viewer.skylark.value.ColorField;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class Viewer {
    private static final NullView CLEAR_OVERLAY = new NullView();
    private static final AbstractUserAction DEBUG_OPTION = new DebugOption();
    private static Viewer instance;
    private static final Logger LOG = Logger.getLogger(Viewer.class);
    private static final Bounds NO_REDRAW = new Bounds();
    public static final String PROPERTY_BASE = "nakedobjects.viewer.skylark.";
    private static final String SPECIFICATION_BASE = PROPERTY_BASE + "specification.";

    private static final Logger UI_LOG = Logger.getLogger("ui." + Viewer.class.getName());

    public static Viewer getInstance() {
        return instance;
    }

    private Graphics bufferGraphics;
    private Vector busy = new Vector();
    private Image doubleBuffer;
    private boolean doubleBuffering = false;
    private Insets insets;
    private Size internalDisplaySize = new Size(1, 1);
    private ObjectViewingMechanismListener listener;
    private View overlayView;
    private final Bounds redrawArea;
    private int redrawCount = 100000;
    private RenderingArea renderingArea;
    private View rootView;
    private boolean runningAsExploration;
    private boolean showExplorationMenuByDefault;
    private boolean showRepaintArea;
    private InteractionSpy spy;
    private Bounds statusBarArea = new Bounds();
    private int statusBarHeight;
    private final UndoStack undoStack = new UndoStack();
    protected ViewUpdateNotifier updateNotifier;

    private String userStatus;
    private KeyboardManager keyboardManager;

    public Viewer() {
        instance = this;
        doubleBuffering = NakedObjects.getConfiguration().getBoolean(PROPERTY_BASE + "doublebuffering", true);
        showExplorationMenuByDefault = NakedObjects.getConfiguration().getBoolean(PROPERTY_BASE + "show-exploration", true);
        overlayView = CLEAR_OVERLAY;
        redrawArea = new Bounds();
    }

    public void addSpyAction(String actionMessage) {
        if (spy != null) {
            spy.addAction(actionMessage);
        }
    }

    public void addToNotificationList(View view) {
        updateNotifier.add(view);
    }

    public void clearBusy(View view) {
        showDefaultCursor();
        busy.removeElement(view);
    }

    /**
     * Removes the focus from the specified view; assuming the the specified view has the focus.
     */
    public void clearKeyboardFocus() {
  //      setKeyboardFocus(null);
    }

    public void setKeyboardFocus(View view) {
        if(view == null) {
            throw new NullPointerException();
        }
        
        if(keyboardManager.getFocusManager() != null &&  keyboardManager.getFocusManager().getFocus() != null &&  keyboardManager.getFocusManager().getFocus().getParent() != null) {
            keyboardManager.getFocusManager().getFocus().getParent().markDamaged();
        }
        
        FocusManager focusManager = view.getFocusManager();
        if(focusManager != null) {
            focusManager.setFocus(view);
            if(view.getParent() != null) {
                view.getParent().markDamaged();
            }
        }
        keyboardManager.setFocusManager(focusManager);
     //   keyboardFocus = view == null ? rootView : view;
    }

    public void clearOverlayView() {
        overlayView.markDamaged();
   //     if (overlayView == keyboardFocus) {
       //     clearKeyboardFocus();
     //   }
        overlayView = CLEAR_OVERLAY;
    }

    public void clearOverlayView(View view) {
        if (this.getOverlayView() != view) {
            LOG.warn("no such view to remove: " + view);
        }
        this.clearOverlayView();
    }

    public void clearStatus() {
        setStatus("");
    }

    public void close() {
        if (spy != null) {
            spy.close();
        }
        DebugFrame.disposeAll();
        renderingArea.dispose();
        if (listener != null) {
            listener.viewerClosing();
        }
    }

    // TODO remove this method; use clearOverlay instead
    public void disposeOverlayView() {
        /*
         * if (overlayView != null) { overlayView.dispose(); }
         */
        clearOverlayView();
    }

    public View dragFrom(Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.dragFrom(location);
        } else {
            return rootView.dragFrom(location);
        }
    }

    public Drag dragStart(DragStart start) {
        if (onOverlay(start.getLocation())) {
            start.subtract(overlayView.getLocation());
            return overlayView.dragStart(start);
        } else {
            return rootView.dragStart(start);
        }
    }

    public void firstClick(Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.firstClick(click);
        } else {
            rootView.firstClick(click);
        }
    }

    public void forceRepaint() {
        this.repaint();
    }

    private FocusManager getFocusManager() {
        return overlayView == CLEAR_OVERLAY ?  keyboardManager.getFocusManager(): overlayView.getFocusManager();
    }

    public Bounds getOverlayBounds() {
        Bounds bounds = new Bounds(new Size(renderingArea.getSize()));
        Insets in = renderingArea.getInsets();
        bounds.contract(in.left + in.right, in.top + in.bottom);
        bounds.contract(0, statusBarHeight);
        return bounds;
    }

    public View getOverlayView() {
        return overlayView;
    }

    public InteractionSpy getSpy() {
        return spy;
    }

    public UndoStack getUndoStack() {
        return undoStack;
    }

    public boolean hasFocus(View view) {
        FocusManager focusManager = keyboardManager.getFocusManager();
        return focusManager  != null && focusManager.getFocus() == view;
    }

    public View identifyView(Location location, boolean includeOverlay) {
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

        spy = new InteractionSpy();

        keyboardManager = new KeyboardManager(this);
        InteractionHandler interactionHandler = new InteractionHandler(this, keyboardManager, spy);
        renderingArea.addMouseMotionListener(interactionHandler);
        renderingArea.addMouseListener(interactionHandler);
        renderingArea.addKeyListener(interactionHandler);

        setupViewFactory();

        if (NakedObjects.getConfiguration().getBoolean(PROPERTY_BASE + "show-mouse-spy", false)) {
            spy.open();
        }
        
        setKeyboardFocus(rootView);
    }

    public boolean isBusy(View view) {
        // return busy.contains(view);
        return busy.size() > 0;
    }

    public boolean isRunningAsExploration() {
        return runningAsExploration;
    }

    public boolean isShowingMouseSpy() {
        return spy.isVisible();
    }

    private ViewSpecification loadSpecification(String name, Class cls) {
        String factoryName = NakedObjects.getConfiguration().getString(SPECIFICATION_BASE + name);
        ViewSpecification spec;
        if (factoryName != null) {
            spec = (ViewSpecification) ComponentLoader.loadComponent(factoryName, ViewSpecification.class);
        } else {
            spec = (ViewSpecification) ComponentLoader.loadComponent(cls.getName(), ViewSpecification.class);
        }
        return spec;
    }

    private AbstractUserAction loggingOption(String name, final Level level) {
        return new AbstractUserAction("Log level " + level, UserAction.DEBUG) {
            public Consent disabled(View component) {
                return AbstractConsent.allow(LogManager.getRootLogger().getLevel() != level);
            }

            public void execute(Workspace workspace, View view, Location at) {
                LogManager.getRootLogger().setLevel(level);
            }
        };
    }
/*
    public void makeFocus(View view) {
        if (view != null && view.canFocus()) {
            View focus = getFocus();
            if ((focus != null) && (focus != view)) {
                focus.focusLost();
                focus.markDamaged();
            }

            setKeyboardFocus(view);
            focus.focusReceived();

            view.markDamaged();
        }
    }
*/
    public void markDamaged(Bounds bounds) {
        if (spy != null) {
            spy.addDamagedArea(bounds);
        }
        synchronized (redrawArea) {
            if (redrawArea.equals(NO_REDRAW)) {
                redrawArea.setBounds(bounds);
                UI_LOG.debug("damage - new area " + redrawArea);
            } else {
                redrawArea.union(bounds);
                UI_LOG.debug("damage - extend area " + redrawArea + " - to include " + bounds);
            }
        }
    }

    public void menuOptions(UserActionSet options) {
        options.add(new AbstractUserAction("Quit") {
            public void execute(Workspace workspace, View view, Location at) {
                Viewer.this.close();
            }
        });

        options.add(loggingOption("Off", Level.OFF));
        options.add(loggingOption("Error", Level.ERROR));
        options.add(loggingOption("Warn", Level.WARN));
        options.add(loggingOption("Info", Level.INFO));
        options.add(loggingOption("Debug", Level.DEBUG));

        String showExplorationMenu = "Always show exploration menu " + (showExplorationMenuByDefault ? "off" : "on");
        options.add(new AbstractUserAction(showExplorationMenu, UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                showExplorationMenuByDefault = !showExplorationMenuByDefault;
                view.markDamaged();
            }
        });

        String repaint = "Show painting area  " + (showRepaintArea ? "off" : "on");
        options.add(new AbstractUserAction(repaint, UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                showRepaintArea = !showRepaintArea;
                view.markDamaged();
            }
        });

        String debug = "Debug graphics " + (AbstractView.debug ? "off" : "on");
        options.add(new AbstractUserAction(debug, UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                AbstractView.debug = !AbstractView.debug;
                view.markDamaged();
            }
        });

        String action = this.isShowingMouseSpy() ? "Hide" : "Show";
        options.add(new AbstractUserAction(action + " mouse spy", UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                Viewer.this.setShowMouseSpy(!Viewer.this.isShowingMouseSpy());
            }
        });

        options.add(new AbstractUserAction("Restart object loader/persistor", UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                NakedObjects.getObjectPersistor().reset();
                NakedObjects.getObjectLoader().reset();
            }
        });

        options.add(new AbstractUserAction("Debug system...", UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                InfoDebugFrame f = new InfoDebugFrame();
                f.setInfo(new DebugInfo[] { NakedObjects.debug(), NakedObjects.getObjectPersistor(),
                        NakedObjects.getObjectLoader(), NakedObjects.getConfiguration(), NakedObjects.getSpecificationLoader(),
                        updateNotifier });
                f.show(at.x + 50, workspace.getBounds().y + 6);
            }
        });

        options.add(new AbstractUserAction("Debug viewer...", UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                InfoDebugFrame f = new InfoDebugFrame();
                f.setInfo(new DebugInfo[] { Skylark.getViewFactory(), updateNotifier });
                f.show(at.x + 50, workspace.getBounds().y + 6);
            }
        });

        options.add(new AbstractUserAction("Debug overlay...", UserAction.DEBUG) {
            public void execute(Workspace workspace, View view, Location at) {
                DebugFrame f = new OverlayDebugFrame(Viewer.this);
                f.show(at.x + 50, workspace.getBounds().y + 6);
            }
        });

        options.add(new DebugDumpSnapshotOption());
    }

    public void mouseDown(Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.mouseDown(click);
        } else {
            rootView.mouseDown(click);
        }
    }

    public void mouseMoved(Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            overlayView.mouseMoved(location);
        } else {
            rootView.mouseMoved(location);
        }
    }

    public void mouseUp(Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.mouseUp(click);
        } else {
            rootView.mouseUp(click);
        }
    }

    private boolean onOverlay(Location mouse) {
        return overlayView.getBounds().contains(mouse);
    }

    public void paint(Graphics g) {
        redrawCount++;
        g.translate(insets.left, insets.top);
        int w = internalDisplaySize.getWidth();
        int h = internalDisplaySize.getHeight();
        if (doubleBuffering) {
            if ((doubleBuffer == null) || (bufferGraphics == null) || (doubleBuffer.getWidth(null) < w)
                    || (doubleBuffer.getHeight(null) < h)) {
                doubleBuffer = renderingArea.createImage(w, h);
                LOG.debug("buffer sized to " + doubleBuffer.getWidth(null) + "x" + doubleBuffer.getHeight(null));
            }
            bufferGraphics = doubleBuffer.getGraphics().create();
        } else {
            bufferGraphics = g;
        }

        // restricts the repainting to the clipping area
        Rectangle r = g.getClipBounds();

        bufferGraphics.clearRect(r.x, r.y, r.width, r.height);
        bufferGraphics.clearRect(0, 0, w, h);

        bufferGraphics.setClip(r.x, r.y, r.width, r.height);
        Canvas c = new DrawingCanvas(bufferGraphics, r.x, r.y, r.width, r.height);
        // Canvas c = new Canvas(bufferGraphics, 0, 0, w, h);

        if (spy != null) {
            spy.redraw(r, redrawCount);
        }
        if (AbstractView.debug) {
            LOG.debug("------ repaint viewer #" + redrawCount + " " + r.x + "," + r.y + " " + r.width + "x" + r.height);
        }

        // paint icons

        // paint views
        if (rootView != null) {
            rootView.draw(c.createSubcanvas()); // rootView.getBounds()));
        }

        // paint overlay
        overlayView.draw(c.createSubcanvas(overlayView.getBounds()));

        // paint status
        paintUserStatus(bufferGraphics);

        // blat to screen
        if (doubleBuffering) {
            g.drawImage(doubleBuffer, 0, 0, null);
        }
        if (showRepaintArea) {
            g.setColor(Color.DEBUG_REPAINT_BOUNDS.getAwtColor());
            g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
            g.drawString("#" + redrawCount, r.x + 3, r.y + 15);
        }
    }

    private void paintStatus(Graphics bufferCanvas, int top, String text) {
        bufferCanvas.setFont(Style.STATUS.getAwtFont());
        int baseline = top + Style.STATUS.getAscent();
        bufferCanvas.fillRect(0, top, internalDisplaySize.getWidth(), statusBarHeight);
        bufferCanvas.setColor(Style.SECONDARY1.getAwtColor());
        bufferCanvas.drawLine(0, top, internalDisplaySize.getWidth(), top);
        bufferCanvas.setColor(Style.BLACK.getAwtColor());
        if (text != null) {
            bufferCanvas.drawString(text, 5, baseline + View.VPADDING);
        }
    }

    private void paintUserStatus(Graphics bufferCanvas) {
        int top = internalDisplaySize.getHeight() - statusBarHeight;
        bufferCanvas.setColor(Style.SECONDARY3.getAwtColor());
        paintStatus(bufferCanvas, top, userStatus);
    }

    public View pickupContent(Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.pickupContent(location);
        } else {
            return rootView.pickupContent(location);
        }
    }

    public View pickupView(Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.pickupView(location);
        } else {
            return rootView.pickupView(location);
        }
    }

    protected void popupMenu(View over, Location at, boolean forView, boolean includeExploration, boolean includeDebug) {
        saveCurrentFieldEntry();

        includeExploration = runningAsExploration && (includeExploration || showExplorationMenuByDefault);
        
        popupStatus(over, forView, includeExploration, includeDebug);

        UserActionSet optionSet = new UserActionSet(includeExploration, includeDebug, UserAction.USER);
        if (forView) {
            over.viewMenuOptions(optionSet);
        } else {
            over.contentMenuOptions(optionSet);
        }
        optionSet.add(DEBUG_OPTION);

        // MenuContainer menuContainer = new MenuContainer(over);
        // setOverlayView(menuContainer);

        /*
         * menuContainer.add(optionSet, new Location()); menuContainer.layout();
         */
        DefaultPopupMenu menuContainer = new DefaultPopupMenu();
        setOverlayView(menuContainer);
        menuContainer.init(over, optionSet.getMenuOptions(), optionSet.getColor());
        // menuContainer.layout();

        Size size = menuContainer.getRequiredSize();
        menuContainer.setSize(size);
        Location location = new Location(at);
        location.move(-14, -10);
        menuContainer.setLocation(location);
        menuContainer.limitBoundsWithin(getOverlayBounds());

//        makeFocus(menuContainer);
    }

    /*
     * protected void popupMenu2(View over, Click click) { saveCurrentFieldEntry();
     * 
     * Location at = click.getLocation(); boolean forView = rootView.viewAreaType(new
     * Location(click.getLocation())) == ViewAreaType.VIEW;
     * 
     * forView = click.isAlt() ^ forView; boolean includeExploration = runningAsExploration && (click.isCtrl() ||
     * showExplorationMenuByDefault); boolean includeDebug = click.isShift() && click.isCtrl();
     * 
     * 
     * MenuOptionSet optionSet = new MenuOptionSet(includeExploration, includeDebug); if (forView) {
     * over.viewMenuOptions(optionSet); } else { over.contentMenuOptions(optionSet); }
     * optionSet.add(MenuOptionSet.DEBUG, DEBUG_OPTION);
     * 
     * popupStatus(over, forView, includeExploration, includeDebug);
     * 
     * 
     * 
     * 
     * 
     * popupMenu(optionSet, at, over, includeExploration, includeDebug); } /* public void
     * popupMenu(MenuOptionSet optionSet, Location at, View over, boolean includeExploration, boolean
     * includeDebug) { DefaultPopupMenu popup = new DefaultPopupMenu(); popup.init(over, includeExploration,
     * includeDebug, optionSet);
     * 
     * DefaultPopupMenu overlay = popup;
     * 
     * Size size = overlay.getRequiredSize(); overlay.setSize(size); Location location = new Location(at);
     * location.move(-14, -10); overlay.setLocation(location); overlay.limitBoundsWithin(getOverlayBounds());
     * 
     * setOverlayView(overlay);
     * 
     * makeFocus(overlay); }
     * 
     */
    private void popupStatus(View over, boolean forView, boolean includeExploration, boolean includeDebug) {
        StringBuffer status = new StringBuffer("Menu for ");
        if (forView) {
            status.append("view ");
            status.append(AbstractView.name(over));
        } else {
            status.append("object: ");
            Content content = over.getContent();
            if (content != null) {
                status.append(content.title());
            }
        }
        if (includeDebug || includeExploration) {
            status.append(" (includes ");
            if (includeExploration) {
                status.append("exploration");
            }
            if (includeDebug) {
                if (includeExploration) {
                    status.append(" & ");
                }
                status.append("debug");
            }
            status.append(" options)");
        }
        setStatus(status.toString());
    }

    public void removeFromNotificationList(View view) {
        updateNotifier.remove(view);
    }

    void repaint() {
        updateNotifier.invalidateViewsForChangedObjects();
        synchronized (redrawArea) {
            rootView.layout();
            overlayView.layout();
            if (!redrawArea.equals(NO_REDRAW)) {
                UI_LOG.debug("repaint viewer " + redrawArea);
                Bounds area = new Bounds(redrawArea);
                area.translate(insets.left, insets.top);
                renderingArea.repaint(area.x, area.y, area.width, area.height);
                redrawArea.setBounds(NO_REDRAW);
            }
        }
    }

    public void saveCurrentFieldEntry() {
        View focus = getFocusManager().getFocus();
        if (focus  != null) {
            focus.editComplete();
            focus.markDamaged();
        }
    }

    public void secondClick(Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.secondClick(click);
        } else {
            rootView.secondClick(click);
        }
    }

    public void setBusy(View view) {
        showWaitCursor();
        busy.addElement(view);
    }

    public void setCursor(Cursor cursor) {
        renderingArea.setCursor(cursor);
    }

    public void setExploration(boolean asExploration) {
        this.runningAsExploration = asExploration;
    }

    public void setListener(ObjectViewingMechanismListener listener) {
        this.listener = listener;
    }

    public void setOverlayView(View view) {
        disposeOverlayView();
        overlayView = view;
        FocusManager focusManager = view.getFocusManager();
        if(focusManager != null) {
            keyboardManager.setFocusManager(focusManager);
        }
        view.limitBoundsWithin(rootView.getBounds());
        overlayView.markDamaged();
    }

    public void setRenderingArea(RenderingArea renderingArea) {
        this.renderingArea = renderingArea;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
        rootView.invalidateContent();
    }

    public void setShowMouseSpy(boolean showDeveloperStatus) {
        if (spy.isVisible()) {
            spy.close();
        } else {
            spy.open();
        }
    }

    /**
     * Sets the status string and refreshes that part of the screen.
     */
    public void setStatus(String status) {
        if (!status.equals(this.userStatus)) {
            this.userStatus = status;
            UI_LOG.debug("changed user status " + status + " " + statusBarArea);
            renderingArea.repaint(statusBarArea.x, statusBarArea.y, statusBarArea.width, statusBarArea.height);
        }
    }

    public void setUpdateNotifier(ViewUpdateNotifier updateNotifier) {
        this.updateNotifier = updateNotifier;
    }

    private void setupViewFactory() throws ConfigurationException, ComponentException {
        ViewFactory viewFactory = Skylark.getViewFactory();

        LOG.debug("setting up default views (provided by the framework)");

        /*
         * viewFactory.addValueFieldSpecification(loadSpecification("field.option",
         * OptionSelectionField.Specification.class));
         * viewFactory.addValueFieldSpecification(loadSpecification("field.percentage",
         * PercentageBarField.Specification.class));
         * viewFactory.addValueFieldSpecification(loadSpecification("field.timeperiod",
         * TimePeriodBarField.Specification.class));
         */
        viewFactory.addValueFieldSpecification(loadSpecification("field.color", ColorField.Specification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.checkbox", CheckboxField.Specification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.password", PasswordFieldSpecification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.wrappedtext", WrappedTextFieldSpecification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.text", TextFieldSpecification.class));
        viewFactory.addRootWorkspaceSpecification(new org.nakedobjects.viewer.skylark.metal.WorkspaceSpecification());
        viewFactory.addWorkspaceSpecification(new InnerWorkspaceSpecification());

        if (NakedObjects.getConfiguration().getBoolean(SPECIFICATION_BASE + "defaults", true)) {
            viewFactory.addCompositeRootViewSpecification(new FormSpecification());
            viewFactory.addCompositeRootViewSpecification(new DataFormSpecification());
            viewFactory.addCompositeRootViewSpecification(new ListSpecification());
            viewFactory.addCompositeRootViewSpecification(new TableSpecification());
            

            // viewFactory.addCompositeRootViewSpecification(new
            // BarchartSpecification());
            // viewFactory.addCompositeRootViewSpecification(new
            // GridSpecification());
            viewFactory.addCompositeRootViewSpecification(new TreeBrowserSpecification());
        }

        viewFactory.addCompositeRootViewSpecification(new MessageDialogSpecification());
        viewFactory.addCompositeRootViewSpecification(new DetailedMessageViewSpecification());

        viewFactory.addEmptyFieldSpecification(loadSpecification("field.empty", EmptyField.Specification.class));

        viewFactory.addSubviewIconSpecification(loadSpecification("icon.subview", SubviewIconSpecification.class));
        viewFactory.addObjectIconSpecification(loadSpecification("icon.object", RootIconSpecification.class));
        viewFactory.addClassIconSpecification(loadSpecification("icon.class", ClassIcon.Specification.class));

        String viewParams = NakedObjects.getConfiguration().getString(SPECIFICATION_BASE + "view");

        if (viewParams != null) {
            StringTokenizer st = new StringTokenizer(viewParams, ",");

            while (st.hasMoreTokens()) {
                String specName = (String) st.nextToken();

                if (specName != null && !specName.trim().equals("")) {
                    try {
                        ViewSpecification spec;
                        spec = (ViewSpecification) Class.forName(specName).newInstance();
                        LOG.info("adding view specification: " + spec);

                        viewFactory.addCompositeRootViewSpecification(spec);
                    } catch (ClassNotFoundException e) {
                        LOG.error("failed to find view specification class " + specName);
                    } catch (InstantiationException e1) {
                        LOG.error("failed to instantiate view specification " + specName);
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

    }

    public void showArrowCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void showCrosshairCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void showDefaultCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void showException(Throwable e) {
        ExceptionMessageContent content = new ExceptionMessageContent(e);
        View view = Skylark.getViewFactory().createWindow(content);
        
        locateInCentre(view);
        rootView.getWorkspace().addView(view);
    }

    private void locateInCentre(View view) {
        Size rootSize = rootView.getSize();
        Location location = new Location(rootSize.getWidth() / 2, rootSize.getHeight() / 2);
        Size dialogSize = view.getRequiredSize();
        location.subtract(dialogSize.getWidth() / 2, dialogSize.getHeight() / 2);
        
        view.setLocation(location);
    }

    public void showHandCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void showMessages() {
        String[] messages = NakedObjects.getMessageBroker().getMessages();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < messages.length; i++) {
            if (i > 0) {
                buf.append("; ");
            }
            buf.append(messages[i]);
        }
        setStatus(buf.toString());

        String[] warnings = NakedObjects.getMessageBroker().getWarnings();
        for (int i = 0; i < warnings.length; i++) {
            TextMessageContent content = new TextMessageContent("Warning", warnings[i]);
            View view = Skylark.getViewFactory().createWindow(content);
            locateInCentre(view);
            rootView.getWorkspace().addView(view);
       }

    }

    public void showMoveCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    public void showResizeDownCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
    }

    public void showResizeDownLeftCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
    }

    public void showResizeDownRightCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
    }

    public void showResizeLeftCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
    }

    public void showResizeRightCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
    }

    public void showResizeUpCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
    }

    public void showResizeUpLeftCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
    }

    public void showResizeUpRightCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
    }

    public void showSpy() {
        spy.open();
    }

    public void showTextCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    public void showWaitCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public void sizeChange() {
        internalDisplaySize = new Size(renderingArea.getSize());
        insets = renderingArea.getInsets();
        LOG.debug("  insets " + insets);
        internalDisplaySize.contract(insets.left + insets.right, insets.top + insets.bottom);
        LOG.debug("  internal " + internalDisplaySize);

        Size rootViewSize = new Size(internalDisplaySize);
        statusBarHeight = Style.STATUS.getLineHeight();
        rootViewSize.contractHeight(statusBarHeight);
        statusBarArea = new Bounds(insets.left, insets.top + rootViewSize.height, rootViewSize.width, statusBarHeight);
        ((WorkspaceSpecification) rootView.getSpecification()).setRequiredSize(rootViewSize);
        View subviews[] = rootView.getSubviews();
        for (int i = 0; i < subviews.length; i++) {
            subviews[i].invalidateLayout();
        }

        Size size = rootView.getRequiredSize();
        rootView.setSize(size);

        Bounds bounds = new Bounds(internalDisplaySize);
        markDamaged(bounds);
        repaint();
    }

    public void thirdClick(Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.thirdClick(click);
        } else {
            rootView.thirdClick(click);
        }
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("renderingArea", renderingArea);
        str.append("redrawArea", redrawArea);
        str.append("rootView", rootView);
        return str.toString();
    }

    public void translate(MouseEvent me) {
        me.translatePoint(-insets.left, -insets.top);
    }

    public ViewAreaType viewAreaType(Location location) {
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
   //     makeFocus(rootView);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
