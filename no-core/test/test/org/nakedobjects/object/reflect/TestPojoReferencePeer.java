package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.utility.UnexpectedCallException;

import junit.framework.Assert;


public class TestPojoReferencePeer implements OneToOnePeer {
    public void clearAssociation(NakedObject inObject, NakedObject associate) {}

    public Naked getAssociation(NakedObject inObject) {
        Assert.assertTrue(inObject.getObject()  instanceof TestPojo);

        Object reference = ((TestPojo) inObject).getReference();
        DummyNakedObject nakedObject = new DummyNakedObject();
        nakedObject.setupObject(reference);
        return nakedObject;
    }

    public Object getExtension(Class cls) {
        return null;
    }
    
    public Class[] getExtensions() {
        return new Class[0];
    }


    public MemberIdentifier getIdentifier() {
        return null;
    }

    public NakedObjectSpecification getType() {
        return null;
    }

    public void initAssociation(NakedObject inObject, NakedObject associate) {
        Assert.assertTrue(inObject.getObject() instanceof TestPojo);

        setAssociation(inObject, associate);
    }

    public void initValue(NakedObject inObject, Object associate) {
        throw new UnexpectedCallException();
    }

    public boolean isDerived() {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }
    
    public boolean isEmpty(NakedObject inObject) {
        Assert.assertTrue(inObject.getObject()  instanceof TestPojo);
        return false;
    }

    public void addElement(NakedObject inObject, NakedObject associate) {
        Assert.assertTrue(inObject.getObject()  instanceof TestPojo);

        ((TestPojo) inObject.getObject()).setReference(associate.getObject());
    }

    public void setValue(NakedObject inObject, Object associate) {
        throw new UnexpectedCallException();
    }

    public boolean isObject() {
        return true;
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {}

    public Consent isAssociationValid(NakedObject inObject, NakedObject value) {
        return null;
    }

    public Consent isValueValid(NakedObject inObject, NakedValue value) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isAuthorised(Session session) {
        return false;
    }

    public Consent isAvailable(NakedObject target) {
        return null;
    }
    
    public Consent isUsable(NakedObject target) {
        return null;
    }

    public Consent isVisible(NakedObject target) {
        return null;
    }

    public boolean isHidden() {
        return false;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */