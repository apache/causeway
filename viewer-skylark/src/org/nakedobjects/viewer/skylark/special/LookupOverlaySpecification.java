package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.RootCollection;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.basic.IconView;
import org.nakedobjects.viewer.skylark.basic.LineBorder;
import org.nakedobjects.viewer.skylark.basic.PlainBackground;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;


class LookupOverlaySpecification extends AbstractCompositeViewSpecification implements SubviewSpec {


    
    public LookupOverlaySpecification() {
        builder = new StackLayout(new CollectionElementBuilder(this, true), true);
    }

    public boolean canDisplay(Naked object) {
        return object instanceof NakedCollection;
    }

    public View createSubview(Content content, ViewAxis lookupAxis) {
        return new LookupSelection(new IconView(content, this, lookupAxis, Style.NORMAL));
    }

    public View createView(final Content content, final ViewAxis axis) {
        ObjectContent field = (ObjectContent) content;
        NakedObjectSpecification type = field.getType();
        NakedObjectManager manager = NakedObjectContext.getDefaultContext().getObjectManager();
        RootCollection instanceContent = new RootCollection(manager.allInstances(type, true));
        return new PlainBackground(new LineBorder(2, new ScrollBorder(super.createView(instanceContent, axis))));
    }

    public String getName() {
        return "List";
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