package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalCollectionContent;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;
import org.nakedobjects.viewer.skylark.special.DialogFieldBuilder;
import org.nakedobjects.viewer.skylark.special.StackLayout;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;
import org.nakedobjects.viewer.skylark.util.ViewFactory;

public class ActionDialogSpecification extends AbstractCompositeViewSpecification {

    public ActionDialogSpecification() {
        builder = new WindowDecorator(new StackLayout(new DialogFieldBuilder(new FormSubviews())));
    }

    private static class FormSubviews implements SubviewSpec {

        public View createSubview(Content content, ViewAxis axis) {
            ViewFactory factory = ViewFactory.getViewFactory();

            ViewSpecification spec;
            if (content instanceof InternalCollectionContent) {
                spec = factory.getOpenSubViewSpecification((ObjectContent) content);
            } else if (content instanceof ValueContent) {
                spec = factory.getValueFieldSpecification((ValueContent) content);
            } else if (content instanceof ObjectContent) {
                spec = factory.getIconizedSubViewSpecification((ObjectContent) content);
            } else {
                throw new NakedObjectRuntimeException();
            }

            return spec.createView(content, axis);
        }

        public View decorateSubview(View view) {
            return new FieldLabel(view);
        }
    }

    public String getName() {
        return "Standard Form";
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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