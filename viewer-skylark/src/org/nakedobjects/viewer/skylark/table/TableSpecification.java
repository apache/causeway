package org.nakedobjects.viewer.skylark.table;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;
import org.nakedobjects.viewer.skylark.special.CollectionElementBuilder;
import org.nakedobjects.viewer.skylark.special.StackLayout;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;


class TableHeaderBuilder extends AbstractBuilderDecorator {
    // could this be the axis?
    public TableHeaderBuilder(CompositeViewBuilder design) {
        super(design);
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        View view = wrappedBuilder.createCompositeView(content, specification, axis);
        // TODO remove
        return new TableBorder(view);
    }
}

public class TableSpecification extends AbstractCompositeViewSpecification implements SubviewSpec {
    private ViewSpecification rowSpecification = new TableRowSpecification();

    public TableSpecification() {
        builder = new TableHeaderBuilder(new StackLayout(new CollectionElementBuilder(this, true)));
    }

    public boolean canDisplay(Content content) {
        if (content.isCollection()) {
            TypedNakedCollection coll = (TypedNakedCollection) ((CollectionContent) content).getCollection();
            NakedObjectSpecification elementSpecification = NakedObjects.getSpecificationLoader().loadSpecification(
                    coll.getElementSpecification().getFullName());

            return !elementSpecification.isAbstract();
        } else {
            return false;
        }
    }

    public View createSubview(Content content, ViewAxis axis) {
        return rowSpecification.createView(content, axis);
    }

    public View createView(Content content, ViewAxis axis) {
        TypedNakedCollection coll = (TypedNakedCollection) ((CollectionContent) content).getCollection();
        NakedObjectSpecification elementSpecification = NakedObjects.getSpecificationLoader().loadSpecification(
                coll.getElementSpecification().getFullName());

        NakedObject exampleObject = NakedObjects.getObjectLoader().createTransientInstance(elementSpecification);
        NakedObjectField[] viewFields = exampleObject.getVisibleFields();
        TableAxis tableAxis = new TableAxis(tableFields(viewFields), exampleObject);
        tableAxis.setupColumnWidths(new TypeBasedColumnWidthStrategy());

        View table = super.createView(content, tableAxis);
        tableAxis.setRoot(table);
        return table;

    }

    public String getName() {
        return "Standard Table";
    }

    public boolean isReplaceable() {
        return false;
    }

    private NakedObjectField[] tableFields(NakedObjectField[] viewFields) {
        NakedObjectField[] tableFields = new NakedObjectField[viewFields.length];
        int c = 0;
        for (int i = 0; i < viewFields.length; i++) {
            if (!(viewFields[i] instanceof OneToManyAssociation)) {
                tableFields[c++] = viewFields[i];
            }
        }

        NakedObjectField[] results = new NakedObjectField[c];
        System.arraycopy(tableFields, 0, results, 0, c);
        return results;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */