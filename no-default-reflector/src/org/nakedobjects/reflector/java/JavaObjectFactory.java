package org.nakedobjects.reflector.java;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.reflect.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;

public class JavaObjectFactory implements ObjectFactory {
    private static final Logger LOG = Logger.getLogger(JavaObjectFactory.class);

    private BusinessObjectContainer container;
    
    public JavaObjectFactory() {}
    
    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
    }


    public Object createValueObject(NakedObjectSpecification specification) {
        Class cls = classFor(specification);
        Object object = createObject(cls);        
        return object;
    }
    
    public Object createNewLogicalObject(NakedObjectSpecification specification) {
        Class cls = classFor(specification);
        Object object = createObject(cls);
        setContainer(object, cls);
        logicalCreation(object, cls);
        
        return object;
    }
    
    public Object recreateObject(NakedObjectSpecification specification) {
        Class cls = classFor(specification);
        Object object = createObject(cls);
        setContainer(object, cls);
        
        return object;
    }
    
    public Object createFakeObject(NakedObjectSpecification specification) {
        Class cls = classFor(specification);
        Object object = createObject(cls);
        setContainer(object, cls);        
        
        return object;
    }
     
    private Class classFor(NakedObjectSpecification specification) {
        String className = specification.getFullName();
        Class cls;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new NakedObjectRuntimeException(e);
        }
        return cls;
    }

    /**
     * Creates a new instance of the specified type, and then call the new
     * objects setContainer() methods if it has one.
     */
    private Object createObject(Class cls) {
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new NakedObjectRuntimeException("Cannot create an instance of an abstract classS: " + cls);
        }

        Object object;
        try {
            object = cls.newInstance();
        } catch (InstantiationException e) {
            throw new ReflectionException("Cannot create an instance of " + cls.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Cannot access the default constructor in " + cls.getName());
        }
        setContainer(object, cls);
        return object;
    }

    /**
     * Call the new object's created method if it has one.
     */
    private void logicalCreation(Object object, Class cls) {
        invokeMethod(cls, object, "created", new Class[0], new Object[0]);
    }

    public void initRecreatedObject(Object object) {
        Class cls = object.getClass();
        setContainer(object, cls);
    }

    private void setContainer(Object object, Class cls) {
        invokeMethod(cls, object, "setContainer", new Class[] { BusinessObjectContainer.class }, new Object[] { container });
    }

    private void invokeMethod(Class cls, Object target, String methodName, Class[] parameterTypes, Object[] parameters) {
        try {
            Method set = cls.getMethod(methodName, parameterTypes);
            set.invoke(target, parameters);
        } catch (SecurityException e) {
            throw new ReflectionException("Cannot access the " + methodName + " method in " + cls.getName());
        } catch (NoSuchMethodException ignore) {
            /*
             * If there is no such method then it will not be called.
             */
        } catch (IllegalArgumentException e1) {
            throw new ReflectionException(e1);
        } catch (IllegalAccessException e1) {
            throw new ReflectionException("Cannot access the " + methodName + " method in " + cls.getName());
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else {
                throw new ReflectionException(targetException);
            }
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing java business object container " + this);
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