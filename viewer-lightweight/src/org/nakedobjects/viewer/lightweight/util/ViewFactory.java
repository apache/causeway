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
package org.nakedobjects.viewer.lightweight.util;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.viewer.lightweight.DesktopView;
import org.nakedobjects.viewer.lightweight.DragView;
import org.nakedobjects.viewer.lightweight.FallbackView;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.view.EmptyField;


/**
 * This class holds all the different view types that all the different objects can be
 * viewed as.
 */
public class ViewFactory implements DebugInfo {
    private static final Logger LOG = Logger.getLogger(ViewFactory.class);
    private static ViewFactory viewFactory;
    private final Prototypes closedPrototypes = new Prototypes("closed");
    private final Prototypes subLevelCollectionPrototypes = new Prototypes("sub level collection");
    private final Prototypes subLevelObjectPrototypes = new Prototypes("sub level object");
    private final Prototypes topLevelCollectionPrototypes = new Prototypes("top level collection");
    private final Prototypes topLevelObjectPrototypes = new Prototypes("top level object");
    private final View fallbackView = new FallbackView();
    private DesktopView classProtoype;
    private InternalView emptyFieldProtoype = new EmptyField();
    private View dragViewPrototype;
	private View dragOutlinePrototype;
	
    private ViewFactory() {
    }

    public static ViewFactory getViewFactory() {
        if (viewFactory == null) {
            viewFactory = new ViewFactory();
        }

        return viewFactory;
    }

    public String getDebugData() {
        StringBuffer sb = new StringBuffer();

        debugData(sb, topLevelObjectPrototypes);
        debugData(sb, topLevelCollectionPrototypes);
        debugData(sb, subLevelObjectPrototypes);
        debugData(sb, subLevelCollectionPrototypes);
        debugData(sb, closedPrototypes);

        sb.append("\nDRAGGABLE VIEW\n");
        sb.append("--------------\n");
        sb.append(dragViewPrototype);
        sb.append("\n\nEMPTY FIELD VIEWS\n");
        sb.append("-----------------\n");
        sb.append(emptyFieldProtoype);
        sb.append("\n\n");

        sb.append("\n\n");

        return sb.toString();
    }

    public String getDebugTitle() {
        return "View factory entries";
    }

    public void addClassPrototype(DesktopView view) {
        if (view != null) {
            classProtoype = view;
        }
    }

    public void addClosedPrototype(Class forType, InternalView view) {
        getPrototypes(closedPrototypes, forType).addElement(view);
        LOG.info("Added second level view " + view + " to " + forType + " prototypes");
    }

	public void addDragPrototype(View view) {
		dragViewPrototype = view;
	}

	public void addDragOutlinePrototype(View view) {
		dragOutlinePrototype = view;
	}

    public void addEmptyFieldPrototype(InternalView view) {
        if (view != null) {
            emptyFieldProtoype = view;
        }
    }

    public void addInternalViewPrototype(Class forType, InternalView view) {
        Prototypes prototypes = (NakedCollection.class.isAssignableFrom(forType))
            ? subLevelCollectionPrototypes : subLevelObjectPrototypes;
        getPrototypes(prototypes, forType).addElement(view);
        LOG.info("Added second level view " + view.getName() + " to " + forType + " prototypes");
    }

    public void addRootViewPrototype(Class forType, RootView view) {
        Prototypes prototypes = (NakedCollection.class.isAssignableFrom(forType))
            ? topLevelCollectionPrototypes : topLevelObjectPrototypes;
        getPrototypes(prototypes, forType).addElement(view);
        LOG.info("Added top level view " + view.getName() + " to " + forType + " prototypes");
    }

    public Enumeration closedViews(NakedObject object) {
        return prototypesFor(object, closedPrototypes).elements();
    }

    public DesktopView createClassView(NakedClass object) {
        DesktopView view = (DesktopView) create(classProtoype, object, null);
        LOG.debug(view + " created for " + object);

        return view;
    }

    public DragView createDragView(Naked object) {
        View view = create(dragViewPrototype, object, null);
        LOG.debug(view + " created for dragging " + object);

        return (DragView) view;
    }

	
	public DragView createDragOutline(Naked object) {
		View view = create(dragOutlinePrototype, object, null);
		LOG.debug(view + " created for dragging " + object);

		return (DragView) view;
	}

    public InternalView createFieldView(Naked object, Field field) {
        View prototype = defaultPrototypeFor(object, closedPrototypes);
        View view = create(prototype, object, field);
        LOG.debug(view + " created for " + object);

        return (InternalView) view;
    }

    public DesktopView createIconView(Naked object, Field field) {
        View prototype = defaultPrototypeFor(object, closedPrototypes);
        View view = create(prototype, object, field);
        LOG.debug(view + " created for " + object);

        return (DesktopView) view;
    }

    public InternalView createInternalView(Naked forObject, Field asField, boolean iconized) {
        if (forObject == null) {
            View view = create(emptyFieldProtoype, forObject, asField);
            LOG.debug(view + " created for " + forObject);

            return (InternalView) view;
        }

        if (iconized) {
            return createFieldView(forObject, asField);
        } else {
            return createOpenView(forObject, asField);
        }
    }

    public InternalView createOpenView(Naked object, Field field) {
        View prototype = defaultPrototypeFor(object, subLevelCollectionPrototypes,
                subLevelObjectPrototypes);
        View view = create(prototype, object, field);
        LOG.debug(view + " created for " + object);

        return (InternalView) view;
    }

    public RootView createRootView(Naked object) {
        View prototype = defaultPrototypeFor(object, topLevelCollectionPrototypes,
                topLevelObjectPrototypes);
        View window = create(prototype, object, null);
        LOG.debug(window + " created for " + object);

        return (RootView) window;
    }

    public boolean hasClosedViews(Naked object) {
        return prototypesFor(object, closedPrototypes).size() > 0;
    }

    public boolean hasInternalViews(Naked object) {
        return prototypesFor(object, subLevelCollectionPrototypes, subLevelObjectPrototypes).size() > 0;
    }

    public boolean hasRootViews(Naked object) {
        return prototypesFor(object, topLevelCollectionPrototypes, topLevelObjectPrototypes).size() > 0;
    }

    public Enumeration internalViews(NakedObject object) {
        return prototypesFor(object, subLevelCollectionPrototypes, subLevelObjectPrototypes)
                   .elements();
    }

    public Enumeration rootViews(NakedObject object) {
        return prototypesFor(object, topLevelCollectionPrototypes, topLevelObjectPrototypes)
                   .elements();
    }

    private Vector getPrototypes(Prototypes prototypes, Class forType) {
        return prototypes.get(forType);
    }

    private void addPrototypes(Vector views, Vector prototypes) {
        for (int i = 0; i < prototypes.size(); i++) {
            views.addElement(prototypes.elementAt(i));
        }
    }

    private View create(View prototype, Naked object, Field field) {
        try {
            View newView = prototype.makeView(object, field);

            if (newView == null) {
                throw new RuntimeException("Failed to create clone in " + prototype);
            }

            if (prototype == newView) {
                throw new RuntimeException("Clone is the same objects");
            }

            return newView;
        } catch (CloneNotSupportedException e) {
            /*
             * This should never occur as the View interface is an extension of Cloneable
             */
            e.printStackTrace();
            throw new RuntimeException("Failed to clone the view " + prototype);
        }
    }

    private void debugData(StringBuffer sb, Prototypes prototypes) {
        Enumeration keys = prototypes.classes();

        sb.append(prototypes.getName().toUpperCase() + " VIEWS\n");
        sb.append("--------------------------------".substring(0, prototypes.getName().length()) +
            "-----\n");

        while (keys.hasMoreElements()) {
            Class key = (Class) keys.nextElement();
            sb.append(key.getName());
            sb.append("\n");

            Vector views = new Vector();
            prototypesFor(key, views, prototypes);

            for (int i = 0; i < views.size(); i++) {
                sb.append("    ");
                sb.append(views.elementAt(i).getClass().getName());
                sb.append("\n");
            }

            sb.append("\n");
        }
    }

    private View defaultPrototypeFor(Naked object, Prototypes collectionPrototypes,
        Prototypes objectPrototypes) {
        Prototypes prototypes = (NakedCollection.class.isAssignableFrom(object.getClass()))
            ? collectionPrototypes : objectPrototypes;

        return defaultPrototypeFor(object, prototypes);
    }

    private View defaultPrototypeFor(Naked object, Prototypes prototypes) {
        View view;

        if (object == null) {
            view = emptyFieldProtoype;
        } else {
            Vector prototypeList = prototypesFor(object, prototypes);

            if (prototypeList.size() == 0) {
                view = fallbackView;
                LOG.error("No " + prototypes.getName() + " prototypes for " + object +
                    ", using fallback");
            } else {
                view = (View) prototypeList.firstElement();
                LOG.info("Using " + view.getName() + " (default) for " + object);
            }
        }

        return view;
    }

    private Vector prototypesFor(Naked object, Prototypes collectionPrototypes,
        Prototypes objectPrototypes) {
        Prototypes prototypes = (NakedCollection.class.isAssignableFrom(object.getClass()))
            ? collectionPrototypes : objectPrototypes;

        return prototypesFor(object, prototypes);
    }

    private Vector prototypesFor(Naked object, Prototypes prototypes) {
        if (object == null) {
            throw new NullPointerException();
        } else {
            Vector views = new Vector();
            prototypesFor(object.getClass(), views, prototypes);

            return views;
        }
    }

    private void prototypesFor(Class type, Vector views, Prototypes prototypes) {
        if (prototypes.containsKey(type)) {
            addPrototypes(views, prototypes.get(type));
        }

        // check interfaces next
        Class[] interfaces = type.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            Class interfaceType = interfaces[i];

            if (prototypes.containsKey(interfaceType)) {
                addPrototypes(views, prototypes.get(interfaceType));
            }
        }

        Class supertype = type.getSuperclass();

        if (supertype == null) {
            return;
        }

        prototypesFor(supertype, views, prototypes);
    }
}
