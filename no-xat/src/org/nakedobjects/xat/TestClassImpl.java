package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NotPersistableException;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.Session;


public class TestClassImpl extends AbstractTestObject implements TestClass {

    public static void init(NakedObjectStore objectStore) {}

    private NakedClass nakedClass;

    public TestClassImpl(Session session, NakedClass cls, TestObjectFactory factory) {
        super(session, factory);
         nakedClass = cls;
    }

    /**
     * Finds the instance whose title matched the one specified. A match is any
     * substring matching the specified text, and the result is the first object
     * found that gives such a match, i.e. only one object is returned even
     * though more than one match might occur.
     */
    public TestObject findInstance(String title) {
        NakedObjectContext context = NakedObjectContext.getDefaultContext();
        NakedObjectSpecification type = ((NakedClass) getForObject().getObject()).forObjectType();
        TypedNakedCollection instances = context.getObjectManager().findInstances(type, title, true);
        if (instances.size() == 0) {
            throw new IllegalActionError("No instance found with title " + title);
        } else {
            NakedObject foundObject = instances.elementAt(0);
            return factory.createTestObject(session, foundObject);
        }
    }

    /**
     * Returns the NakedClass that this view represents.
     */
    public final Naked getForObject() {
        return PojoAdapter.createAdapter(nakedClass);
    }

    public String getTitle() {
        return nakedClass.getSingularName();
    }

    /**
     * Get the instances of this class.
     */
    public TestCollection instances() {
        NakedObjectContext context = NakedObjectContext.getDefaultContext();
        NakedCollection instances = context.getObjectManager().allInstances(((NakedClass) getForObject()).forObjectType(), false);
        if (instances.size() == 0) {
            throw new IllegalActionError("Find must find at least one object");
        } else {
            return factory.createTestCollection(session, instances);
        }
    }

    /**
     * Creates a new instance of this class.
     */
    public TestObject newInstance() {
        NakedObject object = newInstance(nakedClass);

        return factory.createTestObject(session, object);
    }

    private NakedObject newInstance(NakedClass cls) {
        NakedObject object;

        NakedObjectContext context = NakedObjectContext.getDefaultContext();
        try {
            object = context.getObjectManager().createTransientInstance(cls.forObjectType());
            object.setContext(context);
            object.getContext().makePersistent(object);
            object.created();
            //object.getContext().getObjectManager().objectChanged(object);
            object.getContext().getObjectManager().saveChanges();
        } catch (NotPersistableException e) {
            NakedError error = context.getObjectManager().generatorError(
                    "Failed to create instance of " + cls.forObjectType().getFullName(), e);
            object = PojoAdapter.createNOAdapter(error);

            System.out.println("Failed to create instance of " + cls.forObjectType().getFullName());
            e.printStackTrace();
        }

        return object;
    }

    public void setForObject(Naked object) {
        throw new NakedObjectRuntimeException();
    }
  
    protected Action getAction(NakedObjectSpecification nakedClass, String name) {
        return nakedClass.getClassAction(Action.USER, name);
    }
    
    protected Action getAction(NakedObjectSpecification nakedClass, String name,
            NakedObjectSpecification[] parameterClasses) {
        return nakedClass.getClassAction(Action.USER, simpleName(name), parameterClasses);}
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