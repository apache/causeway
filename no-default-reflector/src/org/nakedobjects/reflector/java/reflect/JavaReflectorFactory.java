package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.value.Date;
import org.nakedobjects.application.value.IntegerNumber;
import org.nakedobjects.application.valueholder.BusinessValueHolder;
import org.nakedobjects.application.valueholder.Color;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.MultilineTextString;
import org.nakedobjects.application.valueholder.Password;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.reflector.java.value.DateValueAdapter;
import org.nakedobjects.reflector.java.value.IntegerNumberAdapter;
import org.nakedobjects.reflector.java.valueholder.BusinessValueAdapter;
import org.nakedobjects.reflector.java.valueholder.ColorValueObjectAdapter;
import org.nakedobjects.reflector.java.valueholder.LogicalValueObjectAdapter;
import org.nakedobjects.reflector.java.valueholder.MultilineTextStringAdapter;
import org.nakedobjects.reflector.java.valueholder.PasswordAdapter;
import org.nakedobjects.reflector.java.valueholder.TextStringAdapter;

import java.util.Vector;

import org.apache.log4j.Logger;

public class JavaReflectorFactory extends ReflectorFactory {
    private final static Logger LOG = Logger.getLogger(JavaReflectorFactory.class);
    
    public Reflector createReflector(String className) throws ReflectionException {
         return new JavaReflector(className);
    }
    
    public NakedValue createValueAdapter(Object object) {
        if (object instanceof MultilineTextString ){
            return new MultilineTextStringAdapter((MultilineTextString) object);
        } else  if (object instanceof TextString ){
            return new TextStringAdapter((TextString) object);
        } else if (object instanceof Password ){
            return new PasswordAdapter((Password) object);
        } else if (object instanceof Logical ){
            return new LogicalValueObjectAdapter((Logical) object);
        } else if (object instanceof Color){
            return new ColorValueObjectAdapter((Color) object);
        } else if (object instanceof IntegerNumber){
            return new IntegerNumberAdapter((IntegerNumber) object);
        } else if (object instanceof Date){
            return new DateValueAdapter((Date) object);
        } else if (object instanceof BusinessValueHolder ){
            return new BusinessValueAdapter((BusinessValueHolder) object);
        } else {
            return null;
        }
    }
    
    
    public NakedCollection createCollectionAdapter(Object object) {
        if (object instanceof Vector){
            return VectorCollectionAdapter.createAdapter((Vector) object, Object.class);

        } else {
           return null;
       }
      }
    
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing reflector factory " + this);
    }

    public void init() {}

    public void shutdown() {}


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