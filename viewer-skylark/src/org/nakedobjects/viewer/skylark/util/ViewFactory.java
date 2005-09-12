package org.nakedobjects.viewer.skylark.util;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.metal.TextFieldSpecification;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * This class holds all the different view types that all the different objects
 * can be viewed as.
 */
public class ViewFactory implements DebugInfo {
    private static final ViewSpecification fallback = new FallbackView.Specification();
    public static final int INTERNAL = 2;
    private static final Logger LOG = Logger.getLogger(ViewFactory.class);
    public static final int WINDOW = 1;

    private ViewSpecification emptyFieldSpecification;
    private final Vector rootViews = new Vector();
    private ViewSpecification subviewIconSpecification;
    private final Vector subviews = new Vector();

    private final Vector valueFields = new Vector();
    private ViewSpecification workspaceClassIconSpecification;
    private ViewSpecification workspaceObjectIconSpecification;
    private ViewSpecification rootWorkspaceSpecification;
    private ViewSpecification workspaceSpecification;

    public void addClassIconSpecification(ViewSpecification spec) {
        workspaceClassIconSpecification = spec;
    }

    public void addCompositeRootViewSpecification(ViewSpecification spec) {
        rootViews.addElement(spec);
    }

    public void addCompositeSubviewViewSpecification(ViewSpecification spec) {
        subviews.addElement(spec);
    }

    public void addEmptyFieldSpecification(ViewSpecification spec) {
        emptyFieldSpecification = spec;
    }

    public void addObjectIconSpecification(ViewSpecification spec) {
        workspaceObjectIconSpecification = spec;
    }

    public void addSubviewIconSpecification(ViewSpecification spec) {
        subviewIconSpecification = spec;
    }

    public void addValueFieldSpecification(ViewSpecification spec) {
        valueFields.addElement(spec);
    }

    public void addRootWorkspaceSpecification(ViewSpecification spec) {
        rootWorkspaceSpecification = spec;
    }

    public void addWorkspaceSpecification(ViewSpecification spec) {
        workspaceSpecification = spec;
    }

    public Enumeration closedSubviews(Content forContent, View replacingView) {
        Vector v = new Vector();

        if (forContent instanceof ObjectContent) {
            v.addElement(subviewIconSpecification);
        }

        return v.elements();
    }

    public View createIcon(Content content) {
        ViewSpecification spec = getIconizedRootViewSpecification(content);
        View view = createView(spec, content);
        LOG.debug("creating " + view + " (icon) for " + content);

        return view;
    }

    /*
     * public View createWorkspaceIcon(NakedObject object) { ViewSpecification
     * spec = getIconizedRootViewSpecification(object); View view =
     * createRootView(object, spec); LOG.debug("creating " + view + " (iconized
     * root) for " + object);
     * 
     * return view; } /* private View createRootView(Naked object,
     * ViewSpecification spec) { Content content; if(object instanceof
     * NakedCollection) { content = new RootCollection((NakedCollection)
     * object); } else { content = new RootObject((NakedObject) object); }
     * return createView(spec, content); }
     * 
     * --not used? public View createSubviewIcon(OneToOneField content) {
     * ViewSpecification spec = getIconizedSubViewSpecification(content); View
     * view = createView(spec, content); LOG.debug("creating " + view + "
     * (iconized subview) for " + content);
     * 
     * return view; }
     */
    public View createWindow(Content content) {
        ViewSpecification spec = getOpenRootViewSpecification(content);
        View view = createView(spec, content);
        LOG.debug("creating " + view + " (window) for " + content);

        return view;
    }

    /**
     * 
     * public View createOpenRootView(Naked object) { ViewSpecification spec =
     * getOpenRootViewSpecification(object); View view = createRootView(object,
     * spec); LOG.debug("creating " + view + " (open root) for " + object);
     * 
     * return view; }
     */

    private View createView(ViewSpecification specification, Content content) {
        View view;
        if (specification == null) {
            LOG.warn("no suitable view for " + content + " using fallback view");
            specification = new FallbackView.Specification();
        }
        view = specification.createView(content, null);

        return view;
    }

    /*
     * public View createRootWorkspace(NakedObject workspace) {
     * LOG.debug("creating root workspace for " + workspace); View view =
     * createRootView(workspace, rootWorkspaceSpecification);
     * 
     * return view; }
     */
    public View createInnerWorkspace(Content content) {
        LOG.debug("creating inner workspace for " + content);
        View view = createView(workspaceSpecification, content);

        return view;
    }

    private ViewSpecification defaultViewSpecification(Vector availableViews, Content content) {
        Enumeration fields = availableViews.elements();
        while (fields.hasMoreElements()) {
            ViewSpecification spec = (ViewSpecification) fields.nextElement();
            // TODO convert canDisplay to take a content
            if (spec.canDisplay(content)) {
                return spec;
            }
        }

        LOG.warn("no suitable view for " + content + " using fallback view");
        return new FallbackView.Specification();
    }

    private ViewSpecification ensureView(ViewSpecification spec) {
        if (spec == null) {
            LOG.error("missing view; using fallback");
            return new FallbackView.Specification();
        } else {
            return spec;
        }
    }

    public String getDebugData() {
        StringBuffer sb = new StringBuffer();

        sb.append("RootsViews\n");
        Enumeration fields = rootViews.elements();
        while (fields.hasMoreElements()) {
            ViewSpecification spec = (ViewSpecification) fields.nextElement();
            sb.append("  ");
            sb.append(spec);
            sb.append("\n");
        }
        sb.append("\n\n");

        sb.append("Subviews\n");
        fields = subviews.elements();
        while (fields.hasMoreElements()) {
            ViewSpecification spec = (ViewSpecification) fields.nextElement();
            sb.append("  ");
            sb.append(spec);
            sb.append("\n");
        }
        sb.append("\n\n");

        sb.append("Value fields\n");
        fields = valueFields.elements();
        while (fields.hasMoreElements()) {
            ViewSpecification spec = (ViewSpecification) fields.nextElement();
            sb.append("  ");
            sb.append(spec);
            sb.append("\n");
        }
        sb.append("\n\n");

        return sb.toString();
    }

    public String getDebugTitle() {
        return "View factory entries";
    }

    public ViewSpecification getEmptyFieldSpecification() {
        if (emptyFieldSpecification == null) {
            LOG.error("missing empty field specification; using fallback");
            return fallback;
        }
        return emptyFieldSpecification;
    }

    public ViewSpecification getIconizedRootViewSpecification(Content content) {
        if (content.getNaked().getObject() instanceof NakedClass) {
            if (workspaceClassIconSpecification == null) {
                LOG.error("missing workspace class icon specification; using fallback");
                return fallback;
            }
            return ensureView(workspaceClassIconSpecification);
        } else {
            if (workspaceObjectIconSpecification == null) {
                LOG.error("missing workspace object icon specification; using fallback");
                return fallback;
            }
            return ensureView(workspaceObjectIconSpecification);
        }
    }

    public ViewSpecification getIconizedSubViewSpecification(Content content) {
        //    NakedObject object = ((ObjectContent) content).getObject();
        //   if((object != null && object.getObject() instanceof BusinessValue) ||
        // content.getType().getFullName() != "") {

        if (content.getNaked() == null) {
            return getEmptyFieldSpecification();
        } else {
            if (subviewIconSpecification == null) {
                LOG.error("missing sub view icon specification; using fallback");
                return fallback;
            }

            return ensureView(subviewIconSpecification);
        }
    }

    private ViewSpecification getOpenRootViewSpecification(Content content) {
        return defaultViewSpecification(rootViews, content);
    }

    /**
     * @deprecated - views should be specific about what subviews they create;
     *                              and allow the user to change them later
     */
    public ViewSpecification getOpenSubViewSpecification(ObjectContent content) {
        return defaultViewSpecification(subviews, content);
    }

    public ViewSpecification getValueFieldSpecification(ValueContent content) {
        NakedValue object = content.getObject();
        if (object == null || object.getObject() instanceof String || object.getObject() instanceof Date) {
            return new TextFieldSpecification();
        }
        return defaultViewSpecification(valueFields, content);
    }

    public Enumeration openRootViews(Content forContent, View replacingView) {
    //    if (forContent instanceof ObjectContent) {
            return viewSpecifications(rootViews, forContent);
  //      }

     //   return new Vector().elements();
    }

    public Enumeration openSubviews(Content forContent, View replacingView) {
        if (forContent instanceof ObjectContent) {
            return viewSpecifications(subviews, forContent);
        }
        return new Vector().elements();
    }

    public Enumeration valueViews(Content forContent, View replacingView) {
        return new Vector().elements();
    }

    private Enumeration viewSpecifications(Vector availableViews, Content content) {
        Vector v = new Vector();
        Enumeration fields = availableViews.elements();
        while (fields.hasMoreElements()) {
            ViewSpecification spec = (ViewSpecification) fields.nextElement();
            if (spec.canDisplay(content)) {
                v.addElement(spec);
            }
        }
        return v.elements();
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