package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ParameterContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.core.CompositeView;
import org.nakedobjects.viewer.skylark.core.TextView;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;

import org.apache.log4j.Logger;


public class ActionFieldBuilder extends AbstractViewBuilder {
    private static final Logger LOG = Logger.getLogger(ActionFieldBuilder.class);
    private SubviewSpec subviewDesign;

    public ActionFieldBuilder(SubviewSpec subviewDesign) {
        this.subviewDesign = subviewDesign;
    }

    public void build(View view) {
        Assert.assertEquals(view.getView(), view);

        ActionContent actionContent = ((ActionContent) view.getContent());
        if (view.getSubviews().length == 0) {
            newBuild(view, actionContent);
        } else {
            updateBuild(view, actionContent);
        }

    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        return new CompositeView(content, specification, axis);
    }

    private View createFieldView(View view, ParameterContent parameter) {
        View fieldView = subviewDesign.createSubview(parameter, view.getViewAxis());
        if (fieldView == null) {
            throw new NakedObjectRuntimeException("All parameters must be shown");
        }
        return fieldView;
    }

    private void newBuild(View view, ActionContent actionContent) {
        LOG.debug("build new view " + view + " for " + actionContent);
        view.addView(new TextView( "ACTION: " + actionContent.getActionName()));

        int noParameters = actionContent.getNoParameters();
        for (int f = 0; f < noParameters; f++) {
            ParameterContent parameter = actionContent.getParameterContent(f);
            View fieldView = createFieldView(view, parameter);
            String label = parameter.getParameterName();
            view.addView(decorateSubview(new LabelBorder(label, fieldView)));
        }
    }

    private void updateBuild(View view, ActionContent actionContent) {
        LOG.debug("rebuild view " + view + " for " + actionContent);
        View[] subviews = view.getSubviews();

        for (int i = 0; i < subviews.length - 1; i++) {
            // Note the first view is the text field, hence we get the i+1 subview for the parameter i
            View subview = subviews[i + 1];
            Content content = subview.getContent();

            Naked subviewsObject = subview.getContent().getNaked();
            Naked invocationsObject = ((ActionContent) view.getContent()).getParameterObject(i); 
            
            if (content instanceof ObjectParameter) {
                if(subviewsObject != invocationsObject) {
                    ObjectParameter parameter = new ObjectParameter((ObjectParameter) content, (NakedObject) invocationsObject);
                    View fieldView = createFieldView(view, parameter);
                    String label = parameter.getParameterName();
                    view.replaceView(subview, decorateSubview(new LabelBorder(label, fieldView)));                    
                }
            }
        }
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
