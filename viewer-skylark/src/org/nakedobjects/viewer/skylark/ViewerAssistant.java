package org.nakedobjects.viewer.skylark;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.undo.UndoStack;
import org.nakedobjects.utility.DebugFrame;
import org.nakedobjects.utility.InfoDebugFrame;
import org.nakedobjects.utility.StartupException;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.OverlayDebugFrame;

import java.awt.Cursor;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class ViewerAssistant {
    private static ViewerAssistant instance;
	private static final Logger LOG = Logger.getLogger(ViewerAssistant.class);

    public static ViewerAssistant getInstance() {
        return instance;
    }

    private ViewUpdateNotifier updateNotifier;
    private Viewer viewer;
    private final UndoStack undoStack = new UndoStack();
    private InteractionSpy debugFrame;
    
    public ViewerAssistant() {
        instance = this;    
    }
    
    public void setUpdateNotifier(ViewUpdateNotifier updateNotifier) {
        this.updateNotifier = updateNotifier;
    }
    
	/**
	 * Expose as a .NET property
	 * @property
	 */
    public void set_UpdateNotifier(ViewUpdateNotifier updateNotifier) {
        this.updateNotifier = updateNotifier;
    }
    
    public void setViewer(Viewer viewer) {
        this.viewer = viewer;
    }
    
	/**
	 * Expose as a .NET property
	 * @property
	 */
    public void set_Viewer(Viewer viewer) {
        this.viewer = viewer;
    }
    
    public void setDebugFrame(InteractionSpy debugFrame) {
        this.debugFrame = debugFrame;
    }
    
    /**
	 * Expose as a .NET property
	 * @property
	 */
    public void set_DebugFrame(InteractionSpy debugFrame) {
        this.debugFrame = debugFrame;
    }
    
    protected ViewerAssistant(Viewer topView,
        ViewUpdateNotifier updateNotifier, InteractionSpy debugFrame) {
        this.viewer = topView;
        this.updateNotifier = updateNotifier;
        this.debugFrame = debugFrame;
        instance = this;
    }

    public void addToNotificationList(View view) {
        updateNotifier.add(view);
    }

    public void clearOverlayView(View view) {
        if (viewer.getOverlayView() != view) {
            LOG.warn("No such view to remove: " + view);
        }

        viewer.clearOverlayView();
    }

    public void forceRepaint() {
        viewer.repaint();
    }

    public InteractionSpy getSpy() {
        return debugFrame;
    }
    
    public boolean hasFocus(View view) {
        return viewer.hasFocus(view);
    }

    private MenuOption loggingOption(String name, final Level level) {
        return new MenuOption("Log " + level + " " + name + "...") {
                public Consent disabled(View component) {
                    return AbstractConsent.allow(LogManager.getRootLogger().getLevel() != level);
                }

                public void execute(Workspace workspace, View view, Location at) {
                    LogManager.getRootLogger().setLevel(level);
                }
            };
    }

    public void makeFocus(View view) {
        viewer.makeFocus(view);
    }

    public void markDamaged(Bounds bounds) {
        viewer.markDamaged(bounds);
    }

    public void menuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.VIEW,
            new MenuOption("Quit") {
                public void execute(Workspace workspace, View view, Location at) {
                    viewer.close();
                }
            });

        String debug = "Debug graphics " + (AbstractView.DEBUG ? "off" : "on");
        options.add(MenuOptionSet.DEBUG,
            new MenuOption(debug) {
                public void execute(Workspace workspace, View view, Location at) {
                    AbstractView.DEBUG = !AbstractView.DEBUG;
                    view.markDamaged();
                }
            });

        options.add(MenuOptionSet.DEBUG,
            new MenuOption("Vew context details...") {
                public void execute(Workspace workspace, View view, Location at) {
                    InfoDebugFrame f = new InfoDebugFrame();
                    f.setInfo(ClientSession.getSession());
                    f.show(at.x + 50, workspace.getBounds().y + 6);
                }
            });

        options.add(MenuOptionSet.DEBUG,
            new MenuOption("List prototypes...") {
                public void execute(Workspace workspace, View view, Location at) {
                    InfoDebugFrame f = new InfoDebugFrame();
                    f.setInfo(Skylark.getViewFactory());
                    f.show(at.x + 50, workspace.getBounds().y + 6);
                }
            });


        options.add(MenuOptionSet.DEBUG,
                new MenuOption("Debug overlay...") {
                    public void execute(Workspace workspace, View view, Location at) {
                        DebugFrame f = new OverlayDebugFrame(viewer);
                        f.show(at.x + 50, workspace.getBounds().y + 6);
                    }
                });

        options.add(MenuOptionSet.DEBUG,
                new MenuOption("Restart object manager") {
                    public void execute(Workspace workspace, View view, Location at) {
                        try {
	                        NakedObjectManager om = NakedObjects.getObjectManager();
	                        om.shutdown();
                            om.init();
                        } catch (StartupException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });


        options.add(MenuOptionSet.DEBUG,
                new MenuOption("Debug object manager") {
                    public void execute(Workspace workspace, View view, Location at) {
                        NakedObjectManager om = NakedObjects.getObjectManager();
                        InfoDebugFrame f = new InfoDebugFrame();
                        f.setInfo(om);
                        f.show(at.x + 50, workspace.getBounds().y + 6);                   
                    }
                });

        options.add(MenuOptionSet.DEBUG,
                new MenuOption("List notification receivers...") {
                    public void execute(Workspace workspace, View view, Location at) {
                        InfoDebugFrame f = new InfoDebugFrame();
                        f.setInfo(updateNotifier);
                        f.show(at.x + 50, workspace.getBounds().y + 6);
                    }
                });
        
        String action = viewer.isShowingDeveloperStatus() ? "Hide" : "Show";
        options.add(MenuOptionSet.DEBUG, new MenuOption(action + " developer status") {
            public void execute(Workspace workspace, View view, Location at) {
                viewer.setShowDeveloperStatus(!viewer.isShowingDeveloperStatus());
            }
        });
        
        options.add(MenuOptionSet.DEBUG, loggingOption("Off", Level.OFF));
        options.add(MenuOptionSet.DEBUG, loggingOption("Error", Level.ERROR));
        options.add(MenuOptionSet.DEBUG, loggingOption("Warn", Level.WARN));
        options.add(MenuOptionSet.DEBUG, loggingOption("Info", Level.INFO));
        options.add(MenuOptionSet.DEBUG, loggingOption("Debug", Level.DEBUG));
    }

    public void removeFromNotificationList(View view) {
        updateNotifier.remove(view);
    }
    
    public void setOverlayView(View view) {
        viewer.setOverlayView(view);
    }

    public void setStatus(String status) {
        viewer.setStatus(status);
    }
    
    public void showArrowCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void showCrosshairCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void showDefaultCursor() {
    	showArrowCursor();
    }

    public void showHandCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void showMoveCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    public void showResizeDownCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
    }

    public void showResizeDownLeftCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
    }
    
    public void showResizeDownRightCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
    }

    public void showResizeLeftCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
    }

    public void showResizeRightCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
    }

    public void showResizeUpCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
    }

    public void showResizeUpLeftCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
    }

    public void showResizeUpRightCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
    }
    
    public void showTextCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    public void showWaitCursor() {
        viewer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public UndoStack getUndoStack() {
        return undoStack;
    }

    public void saveCurrentFieldEntry() {
        viewer.saveCurrentFieldEntry();
    }

    public Bounds getOverlayBounds() {
        return viewer.getOverlayBounds();
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
