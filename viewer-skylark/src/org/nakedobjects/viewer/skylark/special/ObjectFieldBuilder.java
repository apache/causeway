package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectFieldException;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.OneToOneField;
import org.nakedobjects.viewer.skylark.ValueField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.core.CompositeView;
import org.nakedobjects.viewer.skylark.util.FieldErrorView;

import org.apache.log4j.Logger;


public class ObjectFieldBuilder extends AbstractViewBuilder {
    private static final Logger LOG = Logger.getLogger(ObjectFieldBuilder.class);
    private SubviewSpec subviewDesign;
    private final boolean useFieldType;

    public ObjectFieldBuilder(final SubviewSpec subviewDesign) {
        this(subviewDesign, false);
    }
    
    public ObjectFieldBuilder(final SubviewSpec subviewDesign, boolean useFieldType) {
        this.subviewDesign = subviewDesign;
        this.useFieldType = useFieldType;
    }

    public void build(View view) {
        Assert.assertEquals(view.getView(), view);

        Content content = view.getContent();
        NakedObject object = ((ObjectContent) content).getObject();

        LOG.debug("rebuild view " + view + " for " + object);

        NakedObjectSpecification cls = null;
        if(useFieldType) {
            cls = content.getSpecification();  //cls = ((ObjectField) content).getSpecification();
        } 
        if(cls == null) {
            cls = object.getSpecification();
        }
        
        NakedObjectField[] flds = cls.getVisibleFields(object);

        if (view.getSubviews().length == 0) {
            newBuild(view, object, flds);
        } else {
            updateBuild(view, object, flds);
        }
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        return new CompositeView(content, specification, axis);
    }

    private Content createContent(NakedObject parent, Naked object, NakedObjectField field) {
        if (field == null) {
            throw new NullPointerException();
        }

        Content content;
        if (field instanceof OneToManyAssociation) {
            content = new OneToManyField(parent, (InternalCollection) object, (OneToManyAssociation) field);
        } else if (field.isValue()) {
            content = new ValueField(parent, (NakedValue) object, (OneToOneAssociation) field);
        } else if (field instanceof OneToOneAssociation) {
            content = new OneToOneField(parent, (NakedObject) object, (OneToOneAssociation) field);
        } else {
            throw new NakedObjectRuntimeException();
        }

        return content;
    }

    public View decorateSubview(View subview) {
        return subviewDesign.decorateSubview(subview);
    }

    private void newBuild(View view, NakedObject object, NakedObjectField[] flds) {
        LOG.debug("build new view " + view + " for " + object);
        for (int f = 0; f < flds.length; f++) {
            NakedObjectField field = flds[f];
            addField(view, object, field);
        }
    }

    private void addField(View view, NakedObject object, NakedObjectField field) {
        try {
            Naked value = object.getField(field);
            Content content = createContent(object, value, field);
            View fieldView = subviewDesign.createSubview(content, view.getViewAxis());
            if (fieldView != null) {
                view.addView(decorateSubview(fieldView));
            }
        } catch (NakedObjectFieldException e) {
            LOG.error("invalid field", e);
            view.addView(new FieldErrorView(e.getMessage()));
        }
    }

    private void updateBuild(View view, NakedObject object, NakedObjectField[] flds) {
        LOG.debug("rebuild view " + view + " for " + object);

        View[] subviews = view.getSubviews();
        
        // remove views for fields that no longer exist
        outer:
            for (int i = 0; i < subviews.length; i++) {
                FieldContent fieldContent = ((FieldContent) subviews[i].getContent());
                
                for (int j = 0; j < flds.length; j++) {
                    NakedObjectField field = flds[j];
                    if(fieldContent.getFieldReflector() == field) {
                        continue outer;
                    }
                }

                view.removeView(subviews[i]);
            }
        
        
        // update existing fields if needed
        subviews = view.getSubviews();
        for (int i = 0; i < subviews.length; i++) {
            View subview = subviews[i];
            NakedObjectField fieldReflector = ((FieldContent) subview.getContent()).getFieldReflector();
            Naked value = object.getField(fieldReflector);
            if (fieldReflector.isValue()) {
                subview.refresh();
            } else if (value instanceof NakedCollection) {
                subview.update(value);
            } else {
                NakedObject existing = ((ObjectContent) subviews[i].getContent()).getObject();
                boolean changedValue = value != existing;
                if (changedValue) {
                    View fieldView;
                    try {
                        fieldView = subviewDesign.createSubview(createContent(object, value, fieldReflector), view.getViewAxis());
                    } catch (NakedObjectFieldException e) {
                        LOG.error("invalid field", e);
                        fieldView = new FieldErrorView(e.getMessage());
                    }
                    if (fieldView != null) {
                        view.replaceView(subview, decorateSubview(fieldView));
                    }
                }
            }
        }
        
        // add new fields
        outer2:
        for (int j = 0; j < flds.length; j++) {
            NakedObjectField field = flds[j];
            for (int i = 0; i < subviews.length; i++) {
                FieldContent fieldContent = ((FieldContent) subviews[i].getContent());
                if(fieldContent.getFieldReflector() == field) {
                    continue outer2;
                }
            }
            
            addField(view, object, field);
        }
            
        
        // debug
        {
	       View[] dsubviews = view.getSubviews();
	        for (int i = 0; i < flds.length; i++) {
	            LOG.debug(i + " " + flds[i].getName() + " " + flds[i].hashCode());
	        }
	        
	        for (int i = 0; i < dsubviews.length; i++) {
	            FieldContent fieldContent = ((FieldContent) dsubviews[i].getContent());
	            LOG.debug(i + " " + fieldContent.getFieldName() + " " + fieldContent.getFieldReflector().hashCode());
	        }
        }
        // end debug
        
        /*
         * 1/ To remove fields: look through views and remove any that don't  exists in visible fields
         * 2/ From remaining views chaeck for changes as already being done, and replace if needed
         * 3/ Finally look through fields to see if there is no existing subview; and add one
         */
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
