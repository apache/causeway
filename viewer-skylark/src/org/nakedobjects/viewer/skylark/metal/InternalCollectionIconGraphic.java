package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.viewer.skylark.Picture;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;


public class InternalCollectionIconGraphic extends IconGraphic {

    public InternalCollectionIconGraphic(View view, Text text) {
        super(view, text);
    }

    protected Picture iconPicture(NakedObject object) {
        final InternalCollection cls = (InternalCollection) object;
        final NakedObjectSpecification spec = cls.getElementSpecification();
        Picture icon = loadIcon(spec, "");
        if(icon == null) {
            icon = loadIcon(spec, "");
        }
        return icon;
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