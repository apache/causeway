package org.nakedobjects.viewer.skylark;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.defaults.LocalObjectManager;
import org.nakedobjects.object.defaults.SimpleOidGenerator;
import org.nakedobjects.object.defaults.TransientObjectStore;
import org.nakedobjects.object.security.User;

import java.applet.Applet;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ViewerApplet extends Applet implements RenderingArea {

    private Viewer viewer;

    public void destroy() {
        viewer.close();
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void init() {
        viewer = new Viewer();

        try {
	        LocalObjectManager objectManager = new LocalObjectManager(new TransientObjectStore(), viewer
	                .getUpdateNotifier(), new SimpleOidGenerator());
	
	        NakedObject root = (NakedObject) objectManager.allInstances(User.class.getName())
	                .elements().nextElement();
	
	        viewer.init(this, root, null);
	
            setBackground(Style.APPLICATION_BACKGROUND.getAwtColor());

            /*
             * compensate for change in tab handling in Java 1.4
             */
            Class c = getClass();
            Method m = c.getMethod("setFocusTraversalKeysEnabled",
                    new Class[] { Boolean.TYPE });
            m.invoke(this, new Object[] { Boolean.FALSE });
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException ignore) {
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ConfigurationException e2) {
            e2.printStackTrace();
        } catch (ComponentException e2) {
            e2.printStackTrace();
        }
    }

    /**
     * Calls <code>update()</code> to do double-buffered drawing of all views.
     * 
     * @see #update(Graphics)
     * @see java.awt.Component#paint(Graphics)
     */
    public final void paint(Graphics g) {
        update(g);
    }

    public void start() {
        viewer.start();
    }

    /**
     * Paints the double-buffered image. Calls the <code>draw()</code> method
     * on each top-level view.
     * 
     * @see java.awt.Component#update(Graphics)
     */
    public void update(Graphics g) {
        viewer.paint(g);
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