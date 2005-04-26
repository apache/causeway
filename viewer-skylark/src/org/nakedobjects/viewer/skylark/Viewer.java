package org.nakedobjects.viewer.skylark;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ComponentLoader;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.utility.DebugFrame;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;
import org.nakedobjects.viewer.skylark.basic.EmptyField;
import org.nakedobjects.viewer.skylark.basic.RootIconSpecification;
import org.nakedobjects.viewer.skylark.basic.SubviewIconSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.DefaultPopupMenu;
import org.nakedobjects.viewer.skylark.metal.ClassIcon;
import org.nakedobjects.viewer.skylark.metal.FormSpecification;
import org.nakedobjects.viewer.skylark.metal.ListSpecification;
import org.nakedobjects.viewer.skylark.metal.TableSpecification;
import org.nakedobjects.viewer.skylark.metal.TreeBrowserSpecification;
import org.nakedobjects.viewer.skylark.special.DataFormSpecification;
import org.nakedobjects.viewer.skylark.special.InnerWorkspaceSpecification;
import org.nakedobjects.viewer.skylark.special.RootWorkspaceSpecification;
import org.nakedobjects.viewer.skylark.special.WorkspaceSpecification;
import org.nakedobjects.viewer.skylark.util.ViewFactory;
import org.nakedobjects.viewer.skylark.value.CheckboxField;
import org.nakedobjects.viewer.skylark.value.ColorField;
import org.nakedobjects.viewer.skylark.value.TextField;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


public class Viewer {
    private static final Logger LOG = Logger.getLogger(Viewer.class);
    public static final String PROPERTY_BASE = "viewer.skylark.";
    private static final String SPECIFICATION_BASE = PROPERTY_BASE + "specification.";
    private Graphics bufferGraphics;
    private Image doubleBuffer;
    private boolean doubleBuffering = false;
    private View overlayView;
    private Bounds redrawArea;
    private int redrawCount = 100000;
    private RenderingArea renderingArea;
    private View rootView;
    private String userStatus;
    private PopupMenu popup;
    private boolean explorationMode;
    private ObjectViewingMechanismListener listener;

    private ViewUpdateNotifier updateNotifier;
    private View keyboardFocus;
    private View windowFocus;
    private Size internalDisplaySize;
    private Insets insets;
    private int statusBarHeight;
    private Bounds statusBarArea;
    private InteractionSpy spy;

    public Viewer() {
        doubleBuffering = NakedObjects.getConfiguration().getBoolean(PROPERTY_BASE + "doublebuffering", true);
    
    }
    
    public void markDamaged(Bounds bounds) {
        spy.addDamagedArea(bounds);
        synchronized (this) {
            redrawArea = redrawArea == null ? bounds : redrawArea.union(bounds);
        }
        //		LOG.debug("total damaged area " + redrawArea);
    }

    public void disposeOverlayView() {
        if (overlayView != null) {
            overlayView.dispose();
        }
    }

    public void clearOverlayView() {
        if (overlayView != null) {
            overlayView.markDamaged();
            if (overlayView == keyboardFocus) {
                keyboardFocus = null;
            }
            overlayView = null;
        }
    }

    public View getOverlayView() {
        return overlayView;
    }

    public void init(RenderingArea renderingArea, NakedObject object, ObjectViewingMechanismListener listener)
            throws ConfigurationException, ComponentException {
        init(renderingArea, listener);

        WorkspaceSpecification spec = (WorkspaceSpecification) ComponentLoader.loadComponent(SPECIFICATION_BASE + "root",
                RootWorkspaceSpecification.class, WorkspaceSpecification.class);
        View view = spec.createView(new RootObject(object), null);
        setRootView(view);
    }

    public void init(RenderingArea renderingArea, ObjectViewingMechanismListener listener) throws ConfigurationException,
            ComponentException {
 
        /*
         * background = (Background)
         * ComponentLoader.loadComponent(PARAMETER_BASE + "background",
         * Background.class);
         */
        this.renderingArea = renderingArea;
        this.listener = listener;
        
        spy = new InteractionSpy();
        new ViewerAssistant(this, updateNotifier, spy);

    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
        rootView.invalidateContent();
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
        Canvas c = new Canvas(bufferGraphics, r.x, r.y, r.width, r.height);
        // Canvas c = new Canvas(bufferGraphics, 0, 0, w, h);

        if (AbstractView.debug) {
            LOG.debug("------ repaint viewer #" + redrawCount + " " + r.x + "," +  r.y + " " + r.width + "x" + r.height);
        }

        //paint icons

        // paint views
        if (rootView != null) {
            rootView.draw(c.createSubcanvas()); //rootView.getBounds()));
        }

        // paint overlay
        if (overlayView != null) {
            overlayView.draw(c.createSubcanvas(overlayView.getBounds()));
        }

        // paint status
        paintUserStatus(bufferGraphics);

        // blat to screen
        if (doubleBuffering) {
            g.drawImage(doubleBuffer, 0, 0, null);
        }
        if (AbstractView.debug) {
            g.setColor(Color.green);
            g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
            g.drawString("#" + redrawCount, r.x + 3, r.y + 15);
        }
    }

    private void paintStatus(Graphics bufferCanvas, int top, String text) {
        bufferCanvas.setFont(Style.STATUS.getAwtFont());
        int baseline = top + Style.STATUS.getAscent();
        bufferCanvas.fillRect(0, top, internalDisplaySize.getWidth(), statusBarHeight);
        bufferCanvas.setColor(Color.darkGray);
        bufferCanvas.drawLine(0, top, internalDisplaySize.getWidth(), top);
        if (text != null) {
            bufferCanvas.drawString(text, 5, baseline);
        }
    }

    private void paintUserStatus(Graphics bufferCanvas) {
        int top = internalDisplaySize.getHeight() - statusBarHeight;
        bufferCanvas.setColor(Color.lightGray);
        paintStatus(bufferCanvas, top, userStatus);
    }

    void repaint() {
        LOG.debug("Repaint viewer");
        updateNotifier.invalidateViewsForChangedObjects();
        rootView.layout();
        if (redrawArea != null) {
            Bounds area;
            synchronized (this) {
                area = redrawArea;
                redrawArea = null;
                area.translate(insets.left, insets.top);
            }
            renderingArea.repaint(area.x, area.y, area.width, area.height);
        }
    }

    public boolean isShowingDeveloperStatus() {
        return spy.isVisible();
    }

    public void setShowDeveloperStatus(boolean showDeveloperStatus) {
        if (spy.isVisible()) {
            spy.close();
        } else {
            spy.open();
        }
    }

    public void setCursor(Cursor cursor) {
        renderingArea.setCursor(cursor);
    }

    public void setOverlayView(View view) {
        disposeOverlayView();
        overlayView = view;
        view.limitBoundsWithin(rootView.getBounds());
        overlayView.markDamaged();
    }

    /**
     * Sets the status string and refreshes that part of the screen.
     */
    public void setStatus(String status) {
        if (!status.equals(this.userStatus)) {
            this.userStatus = status;
            LOG.debug("changed user status " + status + " " + statusBarArea);
            renderingArea.repaint(statusBarArea.x, statusBarArea.y, statusBarArea.width, statusBarArea.height);
        }
    }

    protected void popupMenu(View over, Click click) {
        Location at = click.getLocation();
        boolean forView = rootView.viewAreaType(new Location(click.getLocation())) == ViewAreaType.VIEW;

/*        forView = (click.isCtrl() && !click.isShift()) ^ forView;
        boolean includeExploration = click.isShift() || explorationMode;
        boolean includeDebug = click.isShift() && click.isCtrl();
*/
        
        forView = click.isAlt() ^ forView;
        boolean includeExploration = click.isCtrl() || explorationMode;
        boolean includeDebug = click.isShift() && click.isCtrl();
     /*   
        forView = click.button4() ^ forView;
        boolean includeExploration = click.button5();//click.isShift() || explorationMode;
        boolean includeDebug = click.button6(); //click.isShift() && click.isCtrl();
        */

        popup.init(over, rootView, at, forView, includeExploration, includeDebug);
        setOverlayView(popup);

        makeFocus(popup);
    }

    public void makeFocus(View view) {
        if (view != null && view.canFocus()) {
            if ((keyboardFocus != null) && (keyboardFocus != view)) {
                keyboardFocus.focusLost();
                keyboardFocus.markDamaged();
            }

            keyboardFocus = view;
            keyboardFocus.focusRecieved();

            view.markDamaged();
        }
    }

    public void makeWindowFocus(View view) {
        if (view != null && view.canFocus()) {
            windowFocus = view;
            keyboardFocus.focusLost();
            keyboardFocus = null;
        }
    }
    
    public boolean hasFocus(View view) {
        return keyboardFocus == view;
    }

    protected View getFocus() {
        return keyboardFocus;
    }

    private void setupViewFactory() throws ConfigurationException, ComponentException {
        ViewFactory viewFactory = Skylark.getViewFactory();

        LOG.debug("Setting up default views (provided by the framework)");

/*
        viewFactory.addValueFieldSpecification(loadSpecification("field.option", OptionSelectionField.Specification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.percentage", PercentageBarField.Specification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.timeperiod", TimePeriodBarField.Specification.class));
*/
        viewFactory.addValueFieldSpecification(loadSpecification("field.color", ColorField.Specification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.checkbox", CheckboxField.Specification.class));
        viewFactory.addValueFieldSpecification(loadSpecification("field.text", TextField.Specification.class));
        viewFactory.addRootWorkspaceSpecification(new org.nakedobjects.viewer.skylark.metal.WorkspaceSpecification());
        viewFactory.addWorkspaceSpecification(new InnerWorkspaceSpecification());

        if (NakedObjects.getConfiguration().getBoolean(SPECIFICATION_BASE + "defaults", true)) {
            viewFactory.addCompositeRootViewSpecification(new FormSpecification());
            viewFactory.addCompositeRootViewSpecification(new DataFormSpecification());
            viewFactory.addCompositeRootViewSpecification(new ListSpecification());
            viewFactory.addCompositeRootViewSpecification(new TableSpecification());
//            viewFactory.addCompositeRootViewSpecification(new BarchartSpecification());
//           viewFactory.addCompositeRootViewSpecification(new GridSpecification());
            viewFactory.addCompositeRootViewSpecification(new TreeBrowserSpecification());
        }

        viewFactory.addEmptyFieldSpecification(loadSpecification("field.empty", EmptyField.Specification.class));

        viewFactory.addSubviewIconSpecification(loadSpecification("icon.subview", SubviewIconSpecification.class));
        viewFactory.addObjectIconSpecification(loadSpecification("icon.object", RootIconSpecification.class));
        viewFactory.addClassIconSpecification(loadSpecification("icon.class", ClassIcon.Specification.class));

        String viewParams = NakedObjects.getConfiguration().getString(SPECIFICATION_BASE + "view");

        if (viewParams != null) {
            StringTokenizer st = new StringTokenizer(viewParams, ",");

            while (st.hasMoreTokens()) {
                String specName = (String) st.nextToken();

                if (specName != null) {
                    try {
                        ViewSpecification spec;
                        spec = (ViewSpecification) Class.forName(specName).newInstance();
                        LOG.info("Adding view specification: " + spec);

                        viewFactory.addCompositeRootViewSpecification(spec);
                    } catch (ClassNotFoundException e) {
                        LOG.error("Failed to find view specification class " + specName);
                    } catch (InstantiationException e1) {
                        LOG.error("Failed to instantiate view specification " + specName);
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

    }

    public void close() {
        if(spy != null) {
            spy.close();
        }
        DebugFrame.disposeAll();
        renderingArea.dispose();
        if (listener != null) {
            listener.viewerClosing();
        }
    }

    private ViewSpecification loadSpecification(String name, Class cls) throws ConfigurationException, ComponentException {
        return (ViewSpecification) ComponentLoader.loadComponent(SPECIFICATION_BASE + name, cls, ViewSpecification.class);
    }

    public void start() {
//        spy = new InteractionSpy();
//        new ViewerAssistant(this, updateNotifier, spy);

        popup = new DefaultPopupMenu();
        explorationMode = NakedObjects.getConfiguration().getBoolean(PROPERTY_BASE + "show-exploration");

        InteractionHandler interactionHandler = new InteractionHandler(this, spy);
        renderingArea.addMouseMotionListener(interactionHandler);
        renderingArea.addMouseListener(interactionHandler);
        renderingArea.addKeyListener(interactionHandler);

        try {
            setupViewFactory();
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ComponentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (NakedObjects.getConfiguration().getBoolean(PROPERTY_BASE + "debugstatus", false)) {
            spy.open();
        }

   //
   //      sizeChange();
    //     repaint();
        
 //       insets = renderingArea.getInsets();
    }

    public void sizeChange() {
        internalDisplaySize = new Size(renderingArea.getSize());
        LOG.debug("size changed: frame " + internalDisplaySize);
        insets = renderingArea.getInsets();
        LOG.debug("  insets " + insets);
        //rootView.setLocation(new Location(insets.left, insets.top));
        internalDisplaySize.contract(insets.left + insets.right, insets.top + insets.bottom);
        LOG.debug("  internal " + internalDisplaySize);

        Size rootViewSize = new Size(internalDisplaySize);
        statusBarHeight = 2 + Style.STATUS.getHeight() + 2;
        rootViewSize.contractHeight(statusBarHeight);
        statusBarArea = new Bounds(insets.left, insets.top + rootViewSize.height, rootViewSize.width, statusBarHeight);
        ((WorkspaceSpecification) rootView.getSpecification()).setRequiredSize(rootViewSize);
        rootView.invalidateLayout();

        Bounds bounds = new Bounds(internalDisplaySize);
        markDamaged(bounds);
        repaint();
    }

    public String toString() {
        return "Viewer [renderingArea=" + renderingArea + ",redrawArea=" + redrawArea + ",rootView=" + rootView + "]";
    }

    public void translate(MouseEvent me) {
        me.translatePoint(-insets.left, -insets.top);
    }

    public View identifyView(Location location, boolean includeOverlay) {
        if (includeOverlay && onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.identify(location);
        } else {
            return rootView.identify(location);
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

    private boolean onOverlay(Location mouse) {
        return overlayView != null && overlayView.getBounds().contains(mouse);
    }

    public void firstClick(Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.firstClick(click);
        } else {
            rootView.firstClick(click);
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

    public void thirdClick(Click click) {
        if (onOverlay(click.getLocation())) {
            click.subtract(overlayView.getLocation());
            overlayView.thirdClick(click);
        } else {
            rootView.thirdClick(click);
        }
    }

    public ViewAreaType viewAreaType(Location location) {
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.viewAreaType(location);
        } else {
            return rootView.viewAreaType(location);
        }
    }
 
    public View dragFrom(Location location) {    
        if (onOverlay(location)) {
            location.subtract(overlayView.getLocation());
            return overlayView.dragFrom(location);
        } else {
            return rootView.dragFrom(location);
        }
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
    
    public void showDefaultCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    InteractionSpy getSpy() {
        return spy;
    }

    public Drag dragStart(DragStart start) {
        if (onOverlay(start.getLocation())) {
            return null;
        } else {
            return rootView.dragStart(start);
        }
    }
    
    public void saveCurrentFieldEntry() {
         if (keyboardFocus != null) {
            keyboardFocus.editComplete();
            keyboardFocus.markDamaged();
        }
    }

    public Bounds getOverlayBounds() {
        Bounds bounds = new Bounds(new Size(renderingArea.getSize()));
        Insets in = renderingArea.getInsets();
        bounds.contract(in.left + in.right, in.top + in.bottom);
        bounds.contract(0, statusBarHeight);
        return bounds;
    }
    
    public void setRenderingArea(RenderingArea renderingArea) {
        this.renderingArea = renderingArea;
    }
    
    public void setListener(ObjectViewingMechanismListener listener) {
        this.listener = listener;
    }
    
    public void setSpy(InteractionSpy spy) {
        this.spy = spy;
    }

    public void setUpdateNotifier(ViewUpdateNotifier updateNotifier) {
        this.updateNotifier = updateNotifier;
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
