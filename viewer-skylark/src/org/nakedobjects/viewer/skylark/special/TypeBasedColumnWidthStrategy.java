package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.NakedObjectField;

import java.util.Hashtable;


public class TypeBasedColumnWidthStrategy implements ColumnWidthStrategy {
    private static final NakedObjectSpecification NAKEDOBJECT = NakedObjects.getSpecificationLoader().loadSpecification(
            NakedObject.class);
    private Hashtable types = new Hashtable();

    public TypeBasedColumnWidthStrategy() {
/*
        NakedObjectSpecificationLoader loader = NakedObjects.getSpecificationLoader();
        addWidth(loader.loadSpecification(Logical.class), 25);
        addWidth(loader.loadSpecification(Date.class), 65);
        addWidth(loader.loadSpecification(Time.class), 38);
        addWidth(loader.loadSpecification(DateTime.class), 100);
        addWidth(loader.loadSpecification(TextString.class), 80);
 */
        }

    public void addWidth(NakedObjectSpecification specification, int width) {
        types.put(specification, new Integer(width));
    }

    public int getMaximumWidth(int i, NakedObjectField specification) {
        return 0;
    }

    public int getMinimumWidth(int i, NakedObjectField specification) {
        return 15;
    }

    public int getPreferredWidth(int i, NakedObjectField specification) {
        NakedObjectSpecification type = specification.getSpecification();
        if (type == null) {
            return 200;
        }
        Integer t = (Integer) types.get(type);
        if (t != null) {
            return t.intValue();
        } else if (type.isOfType(NAKEDOBJECT)) {
            return 120;
        } else {
            return 100;
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