package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.reflect.valueadapter.BooleanAdapter;
import org.nakedobjects.object.reflect.valueadapter.ByteAdapter;
import org.nakedobjects.object.reflect.valueadapter.DateAdapter;
import org.nakedobjects.object.reflect.valueadapter.DoubleAdapter;
import org.nakedobjects.object.reflect.valueadapter.FloatAdapter;
import org.nakedobjects.object.reflect.valueadapter.IntAdapter;
import org.nakedobjects.object.reflect.valueadapter.LongAdapter;
import org.nakedobjects.object.reflect.valueadapter.ShortAdapter;
import org.nakedobjects.object.reflect.valueadapter.StringAdapter;

import java.util.Date;

// TODO is the pojo hashmap the same thing as the loaded objects; can they be combined
public class PojoAdapterFactory {
    private PojoAdapterHash pojos;
    private ReflectorFactory reflectorFactory;
    
    public Naked createAdapter(Object pojo) {
        if(pojo == null) {
            return null;
        }
        Naked nakedObject;
        if(pojos.containsPojo(pojo)) {
            nakedObject = pojos.getPojo(pojo);
        } else {
            if(pojo instanceof PojoAdapter) {
                throw new NakedObjectRuntimeException("Warning: adapter is wrapping an adapter: " + pojo);
            }
            
            if(pojo instanceof String) {
                nakedObject = new StringAdapter((String) pojo);
            } else if(pojo instanceof Date) {
                nakedObject = new DateAdapter((Date) pojo);
            } else if(pojo instanceof Float) {
                nakedObject = new FloatAdapter();
            } else if(pojo instanceof Double) {
                nakedObject = new DoubleAdapter();
            } else if(pojo instanceof Boolean) {
                nakedObject = new BooleanAdapter();
            } else if(pojo instanceof Byte) {
                nakedObject = new ByteAdapter();
            } else if(pojo instanceof Short) {
                nakedObject = new ShortAdapter();
            } else if(pojo instanceof Integer) {
                nakedObject = new IntAdapter();
            } else if(pojo instanceof Long) {
                nakedObject = new LongAdapter();
           } else {
                nakedObject = reflectorFactory.createAdapter(pojo);
            }
            if(nakedObject == null) {
                nakedObject = new PojoAdapter(pojo);
            }
        }
        return nakedObject;       
    }
    
    public NakedObject createNOAdapter(Object pojo) {
         return (NakedObject) createAdapter(pojo);
    }
    
    /**
	 * Expose as a .Net property.
	 * @property
	 */
    public void set_PojoAdapterHash(PojoAdapterHash pojos) {
        this.pojos = pojos;
    }
    
    /**
	 * Expose as a .Net property.
	 * @property
	 */
    public void set_ReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }
    
    public void setPojoAdapterHash(PojoAdapterHash pojos) {
        this.pojos = pojos;
    }

     public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
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