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
package org.nakedobjects.viewer.lightweight;

import org.nakedobjects.ObjectViewingMechanism;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.object.collection.ArbitraryCollection;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.value.Logical;
import org.nakedobjects.object.value.Option;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ComponentLoader;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.utility.Configuration;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;
import org.nakedobjects.viewer.lightweight.view.Browser;
import org.nakedobjects.viewer.lightweight.view.BrowserBorder;
import org.nakedobjects.viewer.lightweight.view.CheckboxField;
import org.nakedobjects.viewer.lightweight.view.ClassIcon;
import org.nakedobjects.viewer.lightweight.view.InstanceList;
import org.nakedobjects.viewer.lightweight.view.InternalList;
import org.nakedobjects.viewer.lightweight.view.ObjectIcon;
import org.nakedobjects.viewer.lightweight.view.OpenClassView;
import org.nakedobjects.viewer.lightweight.view.OpenFieldBorder;
import org.nakedobjects.viewer.lightweight.view.OptionField;
import org.nakedobjects.viewer.lightweight.view.RootBorder;
import org.nakedobjects.viewer.lightweight.view.StandardForm;
import org.nakedobjects.viewer.lightweight.view.Table;
import org.nakedobjects.viewer.lightweight.view.TextField;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


/**
 * A Window wrapper that contains the views of the objects that the user has
 * chosen to work with.  This provides a vector of object views, the popup
 * menu, a status buffer, and double-buffering.
 *
 * The graphical representation is made of a number of layers:
 *                 background -  the lowest layer that provides the background to all the layers above
 *                 icon - show all the iconized views, particularly the class icons
 *                 window - shows all the objects open as windows
 *                 drag - working above all the others it rhe drag layer in which there is only ever one view, the current dragged view
 */
public class Viewer implements ObjectViewingMechanism {
    private static final Logger LOG = Logger.getLogger(Viewer.class);
    private static final String PARAMETER_BASE = "viewer.lightweight.";
    private Graphics bufferGraphic;
    private Image doubleBuffer;
    private NakedObjectManager objectManager;
    private String status;
    private ViewManager viewManager;
    private ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();
    private Window window;
    private Workspace workspace;
    private boolean doubleBuffering = false;
	private String title;
	private Naked rootObject;

    public void setCursor(Cursor cursor) {
        window.setCursor(cursor);
    }

    public void setTitle(String title) {
    	this.title = title;
	}
    
    public DragHandler getDragHandler() {
        return viewManager.getDragHandler();
    }

    public DragView getDraggingView() {
        return viewManager.getDraggingView();
    }

    public Location getLocation() {
        return new Location(window.getLocation());
    }

    public void setObjectManager(NakedObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public Padding getPadding() {
        Padding insets = new Padding(window.getInsets());

        insets.bottom += Style.STATUS.getHeight();

        return insets;
    }

    public Size getSize() {
        return new Size(window.getSize());
    }

    /**
     * Sets the status string and refreshes that part of the screen.
     * @param status
     */
    public void setStatus(String status) {
        if (!status.equals(this.status)) {
            this.status = status;

			int height = getSize().height; // getHeight()
			int width = getSize().width; // getWidth()
//			LOG.debug("Repaint request for status... " + status);
			repaint(0, height - 20, width, 20);
        }
    }

    public UpdateNotifier getUpdateNotifier() {
        return updateNotifier;
    }

    public void addNotificationView(ObjectView view) {
        updateNotifier.add(view);
    }

    /**
     * Sets the status string and refreshes that part of the screen.
     */
    public void clearStatus() {
        setStatus("");
    }

    public boolean hasFocus(KeyboardAccessible view) {
        return viewManager.keyboardFocus == view;
    }

    public void init(NakedObject rootObject) throws ComponentException {
     //   this.classList = set;
    	this.rootObject = rootObject;
    	
        window = new ApplicationFrame(this, title);
        workspace = new Workspace(this);

        try {
            Background background = (Background) ComponentLoader.loadComponentIfSpecified(PARAMETER_BASE +
                    "background", Background.class);

            if (background != null) {
                workspace.setBackground(background);
            }
        } catch (ConfigurationException e) {
            LOG.warn(e);
        }

        viewManager = new ViewManager(workspace, new PopupMenu());
        
		window.addMouseMotionListener(viewManager);
		window.addMouseListener(viewManager);
		window.addKeyListener(viewManager);

        doubleBuffering = Configuration.getInstance().getBoolean(PARAMETER_BASE +
                "doublebuffering", true);

        setupViewFactory();
        
        workspace.validateLayout();
    }

    public void makeFocus(KeyboardAccessible view) {
        viewManager.makeFocus(view);
    }

    public void menuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.VIEW,
            new MenuOption("Quit") {
                public void execute(Workspace frame, View view, Location at) {
                    shutdown();
                }
            });

        String debug = "Debug graphics " + (AbstractView.DEBUG ? "off" : "on");
        options.add(MenuOptionSet.DEBUG,
            new MenuOption(debug) {
                public void execute(Workspace frame, View view, Location at) {
                    AbstractView.DEBUG = !AbstractView.DEBUG;
                    repaint();
                }
            });

        options.add(MenuOptionSet.DEBUG,
            new MenuOption("List prototypes...") {
                public void execute(Workspace frame, View view, Location at) {
                    DebugFrame f = new DebugFrame();
                    f.setInfo(ViewFactory.getViewFactory());
                    f.show(at.x + 50, frame.getBounds().y + 6);
                }
            });

        options.add(MenuOptionSet.DEBUG,
            new MenuOption("List observers...") {
                public void execute(Workspace frame, View view, Location at) {
                    DebugFrame f = new DebugFrame();
                    f.setInfo(updateNotifier);
                    f.show(at.x + 50, frame.getBounds().y + 6);
                }
            });

        options.add(MenuOptionSet.DEBUG,
            new MenuOption("Object store state...") {
                public void execute(Workspace frame, View view, Location at) {
                    DebugFrame f = new DebugFrame();
                    f.setInfo(objectManager);
                    f.show(at.x + 50, frame.getBounds().y + 6);
                }
            });
    }

    public void paint(Graphics g) {
        LOG.debug("painting ");

        if (doubleBuffering) {
            int w = getSize().width;
            int h = getSize().height;

            if ((doubleBuffer == null) || (bufferGraphic == null) ||
                    (doubleBuffer.getWidth(null) < w) || (doubleBuffer.getHeight(null) < h)) {
                doubleBuffer = window.createImage(w, h);
            }

            bufferGraphic = doubleBuffer.getGraphics().create();
        } else {
            bufferGraphic = g;
        }

        // restricts the repainting to the clipping area
        Rectangle r = g.getClipBounds();
        bufferGraphic.clearRect(r.x, r.y, r.width, r.height);
        bufferGraphic.setClip(r.x, r.y, r.width, r.height);

        // paint views
        workspace.draw(new Canvas(bufferGraphic, r.width, r.height));

        // paint status
        paintStatus(bufferGraphic);

        // blat to screen
        if (doubleBuffering) {
            g.drawImage(doubleBuffer, 0, 0, null);

            if (AbstractView.DEBUG) {
                g.setColor(Color.black);
                g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
            }
        }
    }

    public void removeFromNotificationList(ObjectView view) {
        updateNotifier.remove(view);
    }

    public void repaint(int x, int y, int width, int height) {
        window.repaint(x, y, width, height);
    }

    public void repaint() {
        window.repaint();
    }

    public void shutdown() {
        DebugFrame.disposeAll();
        window.dispose();
        objectManager.shutdown();
    }

    public void start() {
        window.setBounds(50, 10, 800, 700);
        RootView view = ViewFactory.getViewFactory().createRootView(rootObject);
        workspace.addRootView(view);
        window.show();
        window.repaint();
    }

    public void status(String text) {
        setStatus(text);
    }

    /**
     * Creates a series of class views from the classList help by this object.
     * @param frame
     */
 /*   private void addClassViews(Workspace workspace) {
        Enumeration e = classList.elements();

        while (e.hasMoreElements()) {
            NakedClass cls = (NakedClass) e.nextElement();

            DesktopView ci = ViewFactory.getViewFactory().createClassView(cls);
            workspace.addIcon(ci);
        }
		workspace.layoutIcons();
    }
*/
    private void paintStatus(Graphics bufferCanvas) {
        bufferCanvas.setFont(Style.STATUS.getAwtFont());

        Padding insets = getPadding();
        FontMetrics fm = bufferCanvas.getFontMetrics();

        int height = getPadding().bottom;
        int top = getSize().height - height;
        int baseline = top + fm.getAscent();

        bufferCanvas.setColor(Color.lightGray);
        bufferCanvas.fillRect(insets.left, top, getSize().width - 1, height - 1);
        bufferCanvas.setColor(Color.gray);
        bufferCanvas.drawLine(insets.left, top, getSize().width - 1, top);

        if (status != null) {
            bufferCanvas.setColor(Style.IN_FOREGROUND.getAwtColor());
            bufferCanvas.drawString(status, 5, baseline);
        }
    }

    private void setupViewFactory() {
        ViewFactory viewFactory = ViewFactory.getViewFactory();

		LOG.debug("Setting up default views (provided by the framework)");

        // top-level border
        Border topLevelBorder = new RootBorder();

        // instances - list, icon
        RootView instanceList = new InstanceList();
        instanceList.setBorder(topLevelBorder);
        viewFactory.addRootViewPrototype(InstanceCollection.class, instanceList);

        viewFactory.addClosedPrototype(InstanceCollection.class, new ObjectIcon());

        // classes - list
 //       viewFactory.addRootViewPrototype(NakedClassList.class, instanceList);

        // arbitrary collection - list
        RootView arbitraryList = new InstanceList();
        arbitraryList.setBorder(topLevelBorder);
        viewFactory.addRootViewPrototype(ArbitraryCollection.class, arbitraryList);

        // class - form, icon
        RootView classView = new OpenClassView();
        classView.setBorder(topLevelBorder);
        viewFactory.addRootViewPrototype(NakedClass.class, classView);

        // class - main icon
        viewFactory.addClassPrototype(new ClassIcon());

        // object - form, internal form, icon
        RootView objectForm = new StandardForm();
        objectForm.setBorder(topLevelBorder);
        viewFactory.addRootViewPrototype(NakedObject.class, objectForm);

        InternalView form = new StandardForm();
        form.setBorder(new OpenFieldBorder());
        viewFactory.addInternalViewPrototype(NakedObject.class, form);

        viewFactory.addClosedPrototype(NakedObject.class, new ObjectIcon());

        // internal collection (no top-level)
        viewFactory.addInternalViewPrototype(InternalCollection.class, new InternalList());
        
        // tree browser
        Browser instancesBrowser = new Browser();
        Border browserBorder = new BrowserBorder();
        instancesBrowser.setBorder(browserBorder);
        viewFactory.addRootViewPrototype(InstanceCollection.class, instancesBrowser);
        
        Browser objectBrowser = new Browser();
        objectBrowser.setBorder(browserBorder);
        viewFactory.addRootViewPrototype(NakedObject.class, objectBrowser);
        
        // table
        Table instanceTable = new Table();
        Border instanceBorder = new RootBorder();
        instanceTable.setBorder(instanceBorder);
        viewFactory.addRootViewPrototype(InstanceCollection.class, instanceTable);
        
        
        Table table = new Table();
        viewFactory.addInternalViewPrototype(InternalCollection.class, table);
        
        // values
        viewFactory.addClosedPrototype(NakedValue.class, new TextField());

        viewFactory.addClosedPrototype(Logical.class, new CheckboxField());

		viewFactory.addClosedPrototype(Option.class, new OptionField());
		
		// dragging view
		viewFactory.addDragPrototype(new ObjectIcon());
		viewFactory.addDragOutlinePrototype(new DragOutline());

        // read from config file
		LOG.debug("Setting up externally requested views (specified in the configuration file)");
        String viewParams = Configuration.getInstance().getString(PARAMETER_BASE +
                "view");

        if (viewParams != null) {
            StringTokenizer st = new StringTokenizer(viewParams, ",");

            while (st.hasMoreTokens()) {
                String views = (String) st.nextToken();

                if (views != null) {
                    String clsName = views.substring(0, views.indexOf('=')).trim();
                    String viewName = views.substring(views.indexOf('=') + 1).trim();
                    LOG.debug("Adding " + viewName
                    		 +  " for " + clsName + " objects");
                    Class cls;

                    try {
                        cls = Class.forName(clsName);

                        View view = (View) Class.forName(viewName).newInstance();

                        if (view instanceof RootView) {
                            viewFactory.addRootViewPrototype(cls, (RootView) view);
                        }

                        if (view instanceof InternalView) {
                            viewFactory.addInternalViewPrototype(cls, (InternalView) view);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e1) {
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
