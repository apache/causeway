package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.security.SecurityContext;


public class OneToManyAssociation extends Association {
    private final OneToManyAssociationIF delegatedTo;

    public OneToManyAssociation(String name, Class type, OneToManyAssociationIF association) {
        super(name, type);
        this.delegatedTo = association;
    }

    public boolean canAccess(SecurityContext context, NakedObject object) {
        return getAbout(context, object, null, true).canAccess().isAllowed();
    }

    public boolean canUse(SecurityContext context, NakedObject object) {
        return getAbout(context, object, null, true).canUse().isAllowed();
    }

    public void clear(NakedObject inObject) {
        delegatedTo.removeAllAssociations(inObject);
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        if (associate == null) { throw new NullPointerException("element should not be null"); }

        delegatedTo.removeAssociation(inObject, associate);
    }

    public Naked get(NakedObject fromObject) {
        return delegatedTo.getAssociations(fromObject);
    }

    public About getAbout(SecurityContext context, NakedObject object) {
        return getAbout(context, object, null, true);
    }

    public About getAbout(SecurityContext context, NakedObject container, NakedObject element, boolean add) {
        return delegatedTo.getAbout(context, container, element, add);
    }

    public String getLabel(SecurityContext context, NakedObject object) {
        About about = getAbout(context, object);

        return getLabel(about);
    }

    public boolean hasAbout() {
        return delegatedTo.hasAbout();
    }

    public boolean isDerived() {
        return delegatedTo.isDerived();
    }

    public boolean isPart() {
        return true;
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        if (associate == null) { throw new NullPointerException("Can't use null to add an item to a collection"); }

        delegatedTo.addAssociation(inObject, associate);
    }

    public String toString() {
        return "OneToMany [" + super.toString() + "]";
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
