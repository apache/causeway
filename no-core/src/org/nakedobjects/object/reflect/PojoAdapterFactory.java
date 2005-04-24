package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.DebugInfo;

import java.util.Enumeration;


public interface PojoAdapterFactory extends DebugInfo {

    Naked createAdapter(Object pojo);

    NakedObject createNOAdapter(Object pojo);

    void shutdown();

    void reset();

    NakedObject getLoadedObject(Oid oid);

    boolean isLoaded(Oid oid);

    void loaded(NakedObject object) throws ResolveException;

    void unloaded(NakedObject object);

    Enumeration dirtyObjects();
    
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