package org.nakedobjects.application.valueholder;



public abstract class Magnitude extends BusinessValueHolder {
	private static final long serialVersionUID = 1L;

    public boolean isBetween(Magnitude minMagnitude, Magnitude maxMagnitude) {
        return isGreaterThanOrEqualTo(minMagnitude) && 
               isLessThanOrEqualTo(maxMagnitude);
    }

    public abstract boolean isEqualTo(Magnitude magnitude);

    public boolean isGreaterThan(Magnitude magnitude) {
        return magnitude.isLessThan(this);
    }

    public boolean isGreaterThanOrEqualTo(Magnitude magnitude) {
        return !isLessThan(magnitude);
    }

    public abstract boolean isLessThan(Magnitude magnitude);

    public boolean isLessThanOrEqualTo(Magnitude magnitude) {
        return !isGreaterThan(magnitude);
    }

    public Magnitude max(Magnitude magnitude) {
        return isGreaterThan(magnitude) ? this : magnitude;
    }

    public Magnitude min(Magnitude magnitude) {
        return isLessThan(magnitude) ? this : magnitude;
    }

    /**
     * delegates the comparsion to the <code>isEqualTo</code> method if specified object is a <code>Magnitude</code> else returns false.
     * @see #isSameAs(BusinessValue)
     */
    public final boolean isSameAs(BusinessValueHolder object) {
        if (object instanceof Magnitude) {
            return isEqualTo((Magnitude) object);
        } else {
            return false;
        }
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