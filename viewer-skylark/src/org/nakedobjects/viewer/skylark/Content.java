package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.utility.DebugString;


public interface Content {

    /**
     * Determines if the specified content can be drop on this content.
     */
    Consent canDrop(Content sourceContent);

    void debugDetails(DebugString debug);

    /**
     * Implements the response to the dropping of the specified content onto this content.
     */
    Naked drop(Content sourceContent);

    Hint getHint();

    /** 
     * The name of the icon to use to respresent the object represented by this content.
     */
    String getIconName();

    /** 
     * The icon to use to respresent the object represented by this content.
     */
    Image getIconPicture(int iconHeight);

    /**
     * The object represented by this content.
     */
    Naked getNaked();

    /**
     * The specification of the object represented by this content.
     */
    NakedObjectSpecification getSpecification();

    /**
     * Returns true if this content represents a NakedCollection.
     */
    boolean isCollection();

    /**
     * Returns true if this content represents a NakedObject.
     */
    boolean isObject();

    /** 
     * Returns true if the object represented by this content can be persisted.
     */
    boolean isPersistable();

    /** 
     * Returns true if the object represented by this content is transient; has not been persisted yet.
     */
    boolean isTransient();

    /**
     * Returns true if this content represents a NakedValue.
     */
    boolean isValue();

    /**
     * Allows this content to add menu options to the set of menu options the user will see for a view.
     */
    void menuOptions(MenuOptionSet options);

    /**
     * Flags that the object represented by this content has had it state changed by the viewer.
     */
    boolean objectChanged();

    void parseTextEntry(String entryText) throws InvalidEntryException;

    String title();

    String windowTitle();
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