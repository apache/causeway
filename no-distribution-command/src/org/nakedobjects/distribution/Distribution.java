package org.nakedobjects.distribution;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.Session;


public interface Distribution {
    ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses);

    ObjectData[] clearAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate);

    ServerActionResultData executeServerAction(
            Session session,
            String actionType,
            String actionIdentifier,
            ReferenceData target,
            Data[] parameters);

    ClientActionResultData executeClientAction(
            Session session,
            ObjectData[] persisted,
            ObjectData[] changed,
            ReferenceData[] deleted);

    ObjectData[] findInstances(Session session, InstancesCriteria criteria);

    boolean hasInstances(Session session, String fullName);

    int numberOfInstances(Session sessionId, String fullName);

    Data resolveField(Session session, ReferenceData data, String name);

    ObjectData resolveImmediately(Session session, ReferenceData target);

    ObjectData[] setAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate);

    ObjectData[] setValue(Session session, String fieldIdentifier, ReferenceData target, Object value);
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