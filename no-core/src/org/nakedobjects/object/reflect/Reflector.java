package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;


public interface Reflector {
    public static final boolean CLASS = true;
    public static final boolean OBJECT = false;

    Naked acquireInstance();

    ActionPeer[] actions(boolean forClass);

    String[] actionSortOrder();

    String[] classActionSortOrder();

    Hint classHint();

    void clearDirty(NakedObject object2);

    FieldPeer[] fields();

    String[] fieldSortOrder();

    /**
     * The fully qualified class name; including domains, package names etc.
     */
    String fullName();

    /**
     * Returns a list of this class's interfaces where the interfaces are of
     * subtypes of Naked.
     */
    public String[] getInterfaces();

    /**
     * The name of this reflector's class's superclass. Returns null if this
     * reflector's class does not extend a class that implements the Naked
     * interface.
     */
    String getSuperclass();

    boolean isAbstract();

    boolean isDirty(NakedObject object);

    boolean isLookup();

    boolean isObject();

    boolean isPartOf();

    Persistable persistable();

    boolean isValue();

    void markDirty(NakedObject object2);

    /**
     * The natural language (eg english, spanish) name in the plural form - for
     * a number of objects of this type.
     */
    String pluralName();

    /**
     * The short, programmatic, class name; excluding any domains, package names
     * etc.
     */
    String shortName();

    /**
     * The natural language (eg english, spanish) name in the singular form -
     * for a single object of this type.
     */
    String singularName();

    ObjectTitle title();

    String unresolvedTitle(NakedObject pojo);
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