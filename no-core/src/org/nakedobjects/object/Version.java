package org.nakedobjects.object;

import java.util.Date;


/**
 * Version marks a NakedObject as being a particular variant of that object. This is normally done using some
 * form of incrementing number or timestamp, which would be held within the implementing class. The numbers,
 * timestamps, etc should change for each changed object, and the different() method shoud indicate that the
 * two Version objects are different.
 * 
 * <p>
 * The user's name and a timestamp should alos be kept so that when an message is passed to the user it can be
 * of the form "user has change object at time"
 */
public interface Version {

    /**
     * Compares this version against the specified version and returns true if they are different versions.
     * This is use for optimistic checking, where the existence of a different version will normally cause a
     * concurrency exception.
     */
    boolean different(Version version);

    /**
     * Returns the user who made the last change.
     */
    String getUser();

    /**
     * Returns the time of the last change.
     */
    Date getTime();
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