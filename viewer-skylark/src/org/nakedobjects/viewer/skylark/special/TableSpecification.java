package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.collection.AbstractTypedNakedCollectionVector;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;


class TableHeaderBuilder extends AbstractBuilderDecorator {
    // could this be the axis?
    public TableHeaderBuilder(CompositeViewBuilder design) {
        super(design);
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        View view = wrappedBuilder.createCompositeView(content, specification, axis);
        return new TableBorder(view);
    }
}

public class TableSpecification extends AbstractCompositeViewSpecification implements SubviewSpec {
    private ViewSpecification rowSpecification = new TableRowSpecification();

    public TableSpecification() {
        builder = new TableHeaderBuilder(new StackLayout(new CollectionElementBuilder(this, true)));
    }

    public View createView(Content content, ViewAxis axis) {
        AbstractTypedNakedCollectionVector coll = (AbstractTypedNakedCollectionVector) ((ObjectContent) content).getObject();
        NakedObjectSpecification elementSpecification = NakedObjectSpecificationLoader.getInstance().loadSpecification(
                coll.getElementSpecification().getFullName());
        NakedObject exampleObject = (NakedObject) elementSpecification.acquireInstance();
        NakedObjectField[] viewFields = elementSpecification.getVisibleFields(exampleObject, ClientSession.getSession());
        TableAxis tableAxis = new TableAxis(tableFields(viewFields), exampleObject);
        tableAxis.setupColumnWidths(new TypeBasedColumnWidthStrategy());

        View table = super.createView(content, tableAxis);
        tableAxis.setRoot(table);
        return table;
    }

    private NakedObjectField[]  tableFields(NakedObjectField[] viewFields) {
        NakedObjectField[] tableFields = new NakedObjectField[viewFields.length];
        int c = 0;
        for (int i = 0; i < viewFields.length; i++) {
            if(!(viewFields[i] instanceof OneToManyAssociation)) {
                tableFields[c++] = viewFields[i];
            }
        }            

        NakedObjectField[] results = new NakedObjectField[c];
        System.arraycopy(tableFields, 0, results, 0, c);
        return results;
    }

    public boolean canDisplay(Naked object) {
        return object instanceof AbstractTypedNakedCollectionVector;
    }

    public View createSubview(Content content, ViewAxis axis) {
        return rowSpecification.createView(content, axis);
    }

    public String getName() {
        return "Standard Table";
    }

    public boolean isReplaceable() {
        return false;
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