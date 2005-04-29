package org.nakedobjects.object.persistence;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;

public class TitleCriteria implements InstancesCriteria {
    
    private final NakedObjectSpecification specification;
    private final String requiredTitle;
    private final boolean includeSubclasses;

    public TitleCriteria(NakedObjectSpecification specification, String title, boolean includeSubclasses) {
        this.specification = specification;
        this.requiredTitle = title.toLowerCase();
        this.includeSubclasses = includeSubclasses;
        }

    public boolean matches(NakedObject object) {
        String objectTitle = object.titleString().toLowerCase();
        return objectTitle == requiredTitle || objectTitle.indexOf(requiredTitle) >= 0;
    }

    public NakedObjectSpecification getSpecification() {
        return specification;
    }

    public boolean includeSubclasses() {
        return includeSubclasses;
    }

    public String getRequiredTitle() {
        return requiredTitle;
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