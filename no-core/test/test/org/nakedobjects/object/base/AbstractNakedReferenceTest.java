package test.org.nakedobjects.object.base;

import org.nakedobjects.object.ConcurrencyException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.defaults.AbstractNakedReference;
import org.nakedobjects.object.persistence.LongNumberVersion;

import java.util.Date;

import junit.framework.TestCase;
import test.org.nakedobjects.object.DummyNakedObjectSpecification;

public class AbstractNakedReferenceTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AbstractNakedReferenceTest.class);
    }
    
    public void testCheckLock() {
        Version a1 = new LongNumberVersion(1023L, "Tom", new Date(0L)); 
        Version a2 = new LongNumberVersion(1023L, "Tom", new Date(0L)); 
        Version b = new LongNumberVersion(932L, "Dick", new Date(200000L)); 
        
        DummyNakedReference dummy = new DummyNakedReference();
        dummy.setupSpecification(new DummyNakedObjectSpecification());
        dummy.setOptimisticLock(a1);
        
        dummy.checkLock(a2);
        
        try {
            dummy.checkLock(b);
            fail();
        } catch (ConcurrencyException e) {
        }
        
    }

}

class DummyNakedReference extends AbstractNakedReference {

    private DummyNakedObjectSpecification specification;

    public void destroyed() {}

    public void setupSpecification(DummyNakedObjectSpecification specification) {
        this.specification = specification;}

    public Object getObject() {
        return null;
    }

    public String titleString() {
        return "Business Object";
    }
    
    public NakedObjectSpecification getSpecification() {
        return specification;
    }
    
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