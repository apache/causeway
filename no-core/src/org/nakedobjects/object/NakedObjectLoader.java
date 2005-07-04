package org.nakedobjects.object;

import org.nakedobjects.NakedObjectsComponent;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.DebugInfo;

import java.util.Enumeration;

public interface NakedObjectLoader extends NakedObjectsComponent, DebugInfo {
    NakedObject createAdapterForTransient(Object object);

    Enumeration getIdentifiedObjects();

    NakedCollection createCollectionAdapter(final Object collection);
    
    NakedObject createTransientInstance(NakedObjectSpecification specification);
    

    /**
     * Creates new instance of the specified class, and creates an adapter to hold it.
     */
    Naked recreateExistingInstance(NakedObjectSpecification specification);

    NakedObject createInstance(NakedObjectSpecification specification);
    
    
    /**
     * Recreates an adapter for a persistent busines object that is being loaded into the system.  If an
     * adapter already exists for the specified OID then that adapter is returned.  Otherwise a new instance
     * of the specified business object is created and an adapter is created for it.  The adapter will then
     * be in the state UNRESOLVED.
     */
    NakedObject recreateAdapter(Oid oid, NakedObjectSpecification spec) ;
        
    NakedValue createAdapterForValue(Object value);

    NakedObject getAdapterFor(Oid oid);
  
    NakedObject getAdapterFor(Object object);
    
    NakedObject getAdapterOrCreateTransientFor(final Object object);
    
    boolean isIdentityKnown(Oid oid);

    void loading(NakedObject object, ResolveState state);

    void loaded(NakedObject object, ResolveState state);
    
    void reset();
    
    void unloaded(NakedObject object);

    void makePersistent(NakedObject object, Oid oid);

    boolean needsLoading(NakedObject object);

    NakedValue createValueInstance(NakedObjectSpecification specification);
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