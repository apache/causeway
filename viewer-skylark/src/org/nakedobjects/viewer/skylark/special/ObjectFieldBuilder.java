package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectDefinitionException;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.ObjectField;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.OneToOneField;
import org.nakedobjects.viewer.skylark.ValueField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.core.CompositeObjectView;
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
        if(useFieldType && content instanceof ObjectField) {
            cls = object.getSpecification();cls = ((ObjectField) content).getSpecification();
        } 
        if(cls == null) {
            cls = object.getSpecification();
        }
        
        NakedObjectField[] flds = cls.getVisibleFields(object, ClientSession.getSession());

        if (view.getSubviews().length == 0) {
            newBuild(view, object, flds);
        } else {
            updateBuild(view, object, flds);
        }
        object.clearViewDirty();
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        return new CompositeObjectView(content, specification, axis);
    }

    private ObjectField createContent(NakedObject parent, Naked object, NakedObjectField field) {
        if (field == null) {
            throw new NullPointerException();
        }

        ObjectField content;
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
            try {
                Naked value = object.getField(field);
                ObjectField content = createContent(object, value, field);
                View fieldView = subviewDesign.createSubview(content, view.getViewAxis());
                if (fieldView != null) {
                    view.addView(decorateSubview(fieldView));
                }
            } catch (NakedObjectDefinitionException e) {
                LOG.error("Invalid field", e);
                view.addView(new FieldErrorView(e.getMessage()));
            }
        }
    }

    private void updateBuild(View view, NakedObject object, NakedObjectField[] flds) {
        LOG.debug("rebuild view " + view + " for " + object);

        View[] subviews = view.getSubviews();

        int fld = 0;

        for (int i = 0; i < subviews.length; i++) {
            View subview = subviews[i];
            while (((FieldContent) subview.getContent()).getField() != flds[fld]) {
                fld++;
            }
            Assert.assertTrue(fld < flds.length);

            NakedObjectField field = flds[fld];
            try {
                Naked value = object.getField(field);
                if (field.isValue()) {
                    NakedValue existing = ((ValueField) subview.getContent()).getObject();
                    if (value != existing) {
               //         ((OneToOneField) subview.getContent()).updateDerivedValue((NakedObject) value);
                    }
                    subview.refresh();
                } else if (value instanceof NakedCollection) {
                    subview.update(value);
                } else {
                    NakedObject existing = ((ObjectContent) subviews[i].getContent()).getObject();
         //           boolean changeToNull = value == null && existing != null;
         //           boolean changedFromNull = value != null && existing == null;
                    boolean changedValue = value != existing;
          //          if (changeToNull || changedFromNull || changedValue) {
                    if (changedValue) {
                        View fieldView = subviewDesign.createSubview(createContent(object, value, field), view.getViewAxis());
                        if (fieldView != null) {
                            view.replaceView(subview, decorateSubview(fieldView));
                        }
                    }
                }
            } catch (NakedObjectDefinitionException e) {
                LOG.error("Invalid field", e);
                view.addView(new FieldErrorView(e.getMessage()));
            }

            fld++;
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
