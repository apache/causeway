package org.nakedobjects.viewer.skylark.table;

import org.nakedobjects.object.reflect.NakedObjectField;

public class DefaultColumnWidthStrategy implements ColumnWidthStrategy {

    private int minimum;
    private int preferred;
    private int maximum;

    public DefaultColumnWidthStrategy() {
        this(18, 70, 250);
    }
    
    public DefaultColumnWidthStrategy(int minimum, int preferred, int maximum) {
        if(minimum <= 0) {
            throw new IllegalArgumentException("minimum width must be greater than zero");
        }
        if(preferred <= minimum || preferred >= maximum ) {
            throw new IllegalArgumentException("preferred width must be greater than minimum and less than maximum");
        }
        this.minimum = minimum;
        this.preferred = preferred;
        this.maximum = maximum;
    }

    public int getMinimumWidth(int i, NakedObjectField specification) {
        return minimum;
    }

    public int getPreferredWidth(int i, NakedObjectField specification) {
        return preferred;
    }

    public int getMaximumWidth(int i, NakedObjectField specification) {
        return maximum;
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/