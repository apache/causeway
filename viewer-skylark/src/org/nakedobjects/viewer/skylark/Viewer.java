package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ComponentLoader;
import org.nakedobjects.utility.Configuration;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.viewer.skylark.basic.ClassIconSpecification;
import org.nakedobjects.viewer.skylark.basic.EmptyField;
import org.nakedobjects.viewer.skylark.basic.RootIconSpecification;
import org.nakedobjects.viewer.skylark.basic.SubviewIconSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.DebugFrame;
import org.nakedobjects.viewer.skylark.core.DefaultPopupMenu;
import org.nakedobjects.viewer.skylark.special.BarchartSpecification;
import org.nakedobjects.viewer.skylark.special.DataFormSpecification;
import org.nakedobjects.viewer.skylark.special.FormSpecification;
import org.nakedobjects.viewer.skylark.special.GridSpecification;
import org.nakedobjects.viewer.skylark.special.ListSpecification;
import org.nakedobjects.viewer.skylark.special.RootWorkspaceSpecification;
import org.nakedobjects.viewer.skylark.special.TableSpecification;
import org.nakedobjects.viewer.skylark.special.TreeBrowserSpecification;
import org.nakedobjects.viewer.skylark.special.WorkspaceSpecification;
import org.nakedobjects.viewer.skylark.util.ViewFactory;
import org.nakedobjects.viewer.skylark.value.CheckboxField;
import org.nakedobjects.viewer.skylark.value.ColorField;
import org.nakedobjects.viewer.skylark.value.OptionSelectionField;
import org.nakedobjects.viewer.skylark.value.PercentageBarField;
import org.nakedobjects.viewer.skylark.value.TextField;
import org.nakedobjects.viewer.skylark.value.TimePeriodBarField;

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
    private String status;
    private PopupMenu popup;
	private boolean explorationMode;

    
	private ViewUpdateNotifier updateNotifier = new ViewUpdateNotifier();
    private View keyboardFocus;
    private Size internalDisplaySize;
    private Insets insets;
    private int statusBarHeight;

 
	
    static Location absoluteLocation(View view) {
//    	Assert.assertEquals(view.getView(), view);
    	
    	Location location = view.getLocation();
		while((view = view.getParent()) != null) {
    		Location parentLocation = view.getLocation();
    		location.move(parentLocation.x, parentLocation.y);
    		Padding padding = view.getPadding();
    		location.move(padding.getLeft(), padding.getTop());
    	}

		return location;
	}

	public void markDamaged(View view) {
		Size size = view.getBounds().getSize();
		Location location = absoluteLocation(view);
		markDamaged(new Bounds(location, size));
	}

	public void markDamaged(Bounds bounds ) {
		LOG.debug("damaged area " + bounds);
		synchronized(this) {
			redrawArea = redrawArea == null ? bounds : redrawArea.union(bounds);
		}
//		LOG.debug("total damaged area " + redrawArea);
	}

	public void disposeOverlayView() {
		if(overlayView != null) {
			overlayView.dispose();
		}		
	}
	
	public void clearOverlayView() {
		if(overlayView != null) {
			markDamaged(overlayView);
			if(overlayView == keyboardFocus) {
				keyboardFocus = null;
			}
			overlayView = null;
		}
	}

	public View getOverlayView() {
		return overlayView;
	}

	public UpdateNotifier getUpdateNotifier() {
		return updateNotifier;
	}

	public View identifyView(Location downAt, boolean includeOverlay) {
		if (includeOverlay && overlayView != null && overlayView.getBounds().contains(downAt)) {
			return overlayView;
		} else {
			View identified = identifyView(rootView, downAt);
			return identified;
		}
	}

	private View identifyView(View view, Location locationWithinParent) {
	   	Assert.assertEquals(view.getView(), view);
	    
		Location location = new Location(locationWithinParent);
//		LOG.debug("  checking " + view + " for " + location);
		Bounds bounds = view.getBounds();
		if(bounds.contains(location)) {
			Padding parentPadding = view.getPadding(); 
			bounds.translate(parentPadding.getLeft(), parentPadding.getTop());
			bounds.contract(parentPadding);
			if(bounds.contains(location)) {
				Location viewLocation = view.getLocation();
				location.move(-viewLocation.x, -viewLocation.y);
				//		LOG.debug("    checking " + location);
				location.move(-parentPadding.getLeft(), -parentPadding.getTop());
//				LOG.debug("    checking " + location);
				
				View views[] = view.getSubviews();
				
				for (int i = views.length - 1; i >= 0; i--) {
					View subview = views[i];
					View v = identifyView(subview, location);
					if(v != null) {
						return v;
					}
				}
				return view;
				
			} else {
				return view;
			}
			
		} else {
			return null;
		}
	}
	
	
	public void init(RenderingArea renderingArea, NakedObject object) throws ConfigurationException, ComponentException {
	    doubleBuffering = Configuration.getInstance().getBoolean(PROPERTY_BASE +
                "doublebuffering", true);
/*         background = (Background) ComponentLoader.loadComponent(PARAMETER_BASE +
                "background", Background.class);
   */     
        this.renderingArea = renderingArea; //new ViewerFrame(this, title);

		new ViewerAssistant(this, updateNotifier);
        
        popup = new DefaultPopupMenu();
        explorationMode = Configuration.getInstance().getBoolean(PROPERTY_BASE + "show-exploration");

        InteractionHandler interactionHandler = new InteractionHandler(this);
		renderingArea.addMouseMotionListener(interactionHandler);
		renderingArea.addMouseListener(interactionHandler);
		renderingArea.addKeyListener(interactionHandler);

		setupViewFactory();
		
		WorkspaceSpecification spec = (WorkspaceSpecification) ComponentLoader.loadComponent(SPECIFICATION_BASE + "root", RootWorkspaceSpecification.class, WorkspaceSpecification.class);
		rootView = spec.createView(new RootObject(object), null);
		rootView.invalidateContent();
   }

	public void paint(Graphics g) {
	    redrawCount++;
	    g.translate(insets.left, insets.top);
	    int w = internalDisplaySize.getWidth();
	    int h = internalDisplaySize.getHeight();
	    if (doubleBuffering) {
	        if ((doubleBuffer == null) || (bufferGraphics == null) ||
	                (doubleBuffer.getWidth(null) < w) || (doubleBuffer.getHeight(null) < h)) {
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
	    
	    // paint views
	    if(rootView != null) {
	        rootView.draw(c.createSubcanvas(rootView.getBounds()));
	    }
	    
	    // paint overlay
	    if(overlayView != null) {
	        overlayView.draw(c.createSubcanvas(overlayView.getBounds()));
	    }
	    
	    // paint status
	    paintStatus(bufferGraphics);
	    
	    // blat to screen
	    if (doubleBuffering) {
	        g.drawImage(doubleBuffer, 0, 0, null);
	    }
	    
	    if (AbstractView.DEBUG) {
	        g.setColor(Color.pink);
	        g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
	        g.drawString("#" + redrawCount,r.x + 3, r.y + 15 );
	    }
    }

    private void paintStatus(Graphics bufferCanvas) {
        bufferCanvas.setFont(Style.STATUS.getAwtFont());

        int top = internalDisplaySize.getHeight() - statusBarHeight;
        int baseline = top + Style.STATUS.getAscent();

        bufferCanvas.setColor(Color.lightGray);
        bufferCanvas.fillRect(0, top, internalDisplaySize.getWidth(), statusBarHeight);
        bufferCanvas.setColor(Color.darkGray);
        bufferCanvas.drawLine(0, top, internalDisplaySize.getWidth(), top);

        if (status != null) {
            bufferCanvas.drawString(status, 5, baseline);
        }
    }

    void repaint() {
    	rootView.layout();
    	if(redrawArea != null) {
	    	Bounds area;
	    	synchronized(this) {
				area = redrawArea;
		    	redrawArea = null;
		    	area.translate(insets.left, insets.top);
	    	}
	        renderingArea.repaint(area.x, area.y, area.width, area.height);
    	}
    }

    public void setCursor(Cursor cursor) {
        renderingArea.setCursor(cursor);
    }
    
	public void setOverlayView(View view) {
		disposeOverlayView();
		overlayView = view;
		markDamaged(overlayView);
	}

    /**
     * Sets the status string and refreshes that part of the screen.
     */
    public void setStatus(String status) {
        if (!status.equals(this.status)) {
            this.status = status;

            int statusBarHeight = 2 + Style.STATUS.getHeight() + 2;
            int top = internalDisplaySize.getHeight() - statusBarHeight;

            LOG.debug("show status " + status + " y=" + top);
	        renderingArea.repaint(insets.left, insets.top + top, internalDisplaySize.getWidth(), statusBarHeight);
        }
    }
    
    protected void popupMenu(Click click, View over) {
    	ViewAreaType type = click.getViewAreaType();
    	Location at = absoluteLocation(click.getView());
    	at.translate(click.getLocation());
    	boolean forView = type == ViewAreaType.VIEW;
    	
    	forView = (click.isCtrl() && ! click.isShift()) ^ forView;
    	boolean includeExploration = click.isShift() || explorationMode;
    	boolean includeDebug = click.isShift() && click.isCtrl();
    	popup.init(over, rootView, at, forView, includeExploration, includeDebug);
    	setOverlayView(popup);
    	
    	makeFocus(popup);
    }
    
    public void makeFocus(View view) {
    	if(view != null && view.canFocus()) {
	        if ((keyboardFocus != null) && (keyboardFocus != view)) {
	            keyboardFocus.focusLost();
	            keyboardFocus.markDamaged();
	        }

	        keyboardFocus = view;
	        keyboardFocus.focusRecieved();
	        
	        markDamaged(view);
    	}
    }

    public boolean hasFocus(View view) {
        return keyboardFocus == view;
    }

    protected View getFocus() {
    	return keyboardFocus;
    }



    private void setupViewFactory() throws ConfigurationException, ComponentException {
        ViewFactory viewFactory = ViewFactory.getViewFactory();

		LOG.debug("Setting up default views (provided by the framework)");

		viewFactory.addValueFieldSpecification(loadSpecification("field.color", ColorField.Specification.class));
		viewFactory.addValueFieldSpecification(loadSpecification("field.checkbox", CheckboxField.Specification.class));
		viewFactory.addValueFieldSpecification(loadSpecification("field.option",  OptionSelectionField.Specification.class));
		viewFactory.addValueFieldSpecification(loadSpecification("field.percentage", PercentageBarField.Specification.class));
		viewFactory.addValueFieldSpecification(loadSpecification("field.timeperiod", TimePeriodBarField.Specification.class));
		viewFactory.addValueFieldSpecification(loadSpecification("field.text", TextField.Specification.class));

		viewFactory.addWorkspaceSpecification(new org.nakedobjects.viewer.skylark.metal.WorkspaceSpecification());
        
		if(Configuration.getInstance().getBoolean(SPECIFICATION_BASE + "defaults", true)) {
			viewFactory.addCompositeRootViewSpecification(new FormSpecification());
			viewFactory.addCompositeRootViewSpecification(new DataFormSpecification());
			viewFactory.addCompositeRootViewSpecification(new ListSpecification());
			viewFactory.addCompositeRootViewSpecification(new TableSpecification());
			viewFactory.addCompositeRootViewSpecification(new BarchartSpecification());
			viewFactory.addCompositeRootViewSpecification(new GridSpecification());
		 	viewFactory.addCompositeRootViewSpecification(new TreeBrowserSpecification());
        }
       
		viewFactory.addEmptyFieldSpecification(loadSpecification("field.empty", EmptyField.Specification.class));
		
		viewFactory.addSubviewIconSpecification(loadSpecification("icon.subview", SubviewIconSpecification.class));
		viewFactory.addObjectIconSpecification(loadSpecification("icon.object", RootIconSpecification.class));
		viewFactory.addClassIconSpecification(loadSpecification("icon.class", ClassIconSpecification.class));
			
        String viewParams = Configuration.getInstance().getString(SPECIFICATION_BASE +
                "view");
        
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
    
    public void shutdown() {
        DebugFrame.disposeAll();
        renderingArea.dispose();
        NakedObjectManager.getInstance().shutdown();
    }

    private ViewSpecification loadSpecification(String name, Class cls) throws ConfigurationException, ComponentException {
        return  (ViewSpecification) ComponentLoader.loadComponent(SPECIFICATION_BASE + name, cls, ViewSpecification.class);
    }

    public void start() {
		sizeChange();
        setStatus("Viewer started " + this);
		repaint();
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
		((WorkspaceSpecification) rootView.getSpecification()).setRequiredSize(rootViewSize);
   }
    
    public String toString() {
		return "Viewer [renderingArea=" + renderingArea + ",redrawArea=" + redrawArea + 
			",rootView=" + rootView + "]";
	}

    public void translate(MouseEvent me) {
        me.translatePoint(-insets.left, -insets.top);
    }
}




/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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
