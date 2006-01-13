package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.RootCollection;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.basic.IconView;
import org.nakedobjects.viewer.skylark.basic.PanelBorder;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;


class LookupOverlaySpecification extends AbstractCompositeViewSpecification implements SubviewSpec {
    public LookupOverlaySpecification() {
        builder = new StackLayout(new CollectionElementBuilder(this, true), true);
    }

    public boolean canDisplay(Content content) {
        return content.isCollection();
    }

    public View createSubview(Content content, ViewAxis lookupAxis) {
        return new LookupSelection(new IconView(content, this, lookupAxis, Style.NORMAL));
    }

    public View createView(final Content content, final ViewAxis axis) {
        TypedNakedCollection instances;
        ObjectContent field = (ObjectContent) content;
        NakedObjectSpecification type = field.getSpecification();
        NakedObjectPersistor manager = NakedObjects.getObjectPersistor();
        instances = manager.allInstances(type, true);
        RootCollection instanceContent = new RootCollection(instances);
        instanceContent.setOrderByElement();
        return new PanelBorder(1, new ScrollBorder(super.createView(instanceContent, axis)));
    }

    public String getName() {
        return "Lookup Overlay";
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