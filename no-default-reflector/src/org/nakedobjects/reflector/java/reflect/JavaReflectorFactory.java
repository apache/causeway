package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.value.IntegerNumber;
import org.nakedobjects.application.valueholder.BusinessValueHolder;
import org.nakedobjects.application.valueholder.Color;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.reflector.java.JavaObjectFactory;
import org.nakedobjects.reflector.java.value.BusinessValueAdapter;
import org.nakedobjects.reflector.java.value.ColorValueObjectAdapter;
import org.nakedobjects.reflector.java.value.IntegerNumberAdapter;
import org.nakedobjects.reflector.java.value.LogicalValueObjectAdapter;
import org.nakedobjects.reflector.java.value.TextStringAdapter;

import java.util.Vector;

import org.apache.log4j.Logger;

public class JavaReflectorFactory extends ReflectorFactory {
    private final static Logger LOG = Logger.getLogger(JavaReflectorFactory.class);
    private JavaObjectFactory objectFactory;
    
    public Reflector createReflector(String className) throws ReflectionException {
         return new JavaReflector(className, objectFactory);
    }
    
    public Naked createAdapter(Object object) {
        //    TODO this code is duplicated in JavaReflector
        if (object instanceof TextString ){
            return new TextStringAdapter((TextString) object);
        } else if (object instanceof Logical ){
            return new LogicalValueObjectAdapter((Logical) object);
        } else if (object instanceof Color){
            return new ColorValueObjectAdapter((Color) object);
        } else if (object instanceof IntegerNumber){
            return new IntegerNumberAdapter((IntegerNumber) object);
        } else if (object instanceof BusinessValueHolder ){
            return new BusinessValueAdapter((BusinessValueHolder) object);
        } else if (object instanceof Vector){
            return VectorCollectionAdapter.createAdapter((Vector) object, Object.class);

         } else {
            return null;
        }
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }
    
    public void setObjectFactory(JavaObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
    

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing reflector factory " + this);
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