package org.nakedobjects.persistence.file;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.SimpleOid;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.DateTime;
import org.nakedobjects.object.value.Logical;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.TextString;

import org.apache.log4j.PropertyConfigurator;

public class TestValues {

	public static void main(String[] args) throws ObjectStoreException {
		PropertyConfigurator.configure("log4j.testing.properties");
		
		DataManager manager = new XmlDataManager("tmp/tests");

		NakedObjectSpecification type = NakedObjectSpecification.getNakedClass(Role.class.getName());
		SimpleOid oid = new SimpleOid(99);
		ObjectData data =  new ObjectData(type, oid);

//		data.set("field1", NakedObjectManager.getInstance().getNakedClass(Role.class).getName(), 101);
//		data.set("field2", NakedObjectManager.getInstance().getNakedClass(Person.class).getName(), 102);
		data.saveValue("field3", new TextString("Fred"));

		Date d1 = new Date();
		d1.add(1,2,3);
		data.saveValue("date", d1);
		System.out.println(d1 + " " + d1.saveString());
		
		DateTime ts1 = new DateTime();
		ts1.add(1,2,3);
		data.saveValue("timestamp", ts1);
		System.out.println(ts1 + " " + ts1.saveString());

		Logical l1 = new Logical();
		l1.set();
		data.saveValue("logical", l1);
		System.out.println(l1 + " " + l1.saveString());
		
		Money m = new Money();
		m.setValue(1233.45);
		data.saveValue("money", m);
		System.out.println(m + " " + m.saveString());
		
		SimpleOid coid = new SimpleOid(100);
		
		data.initCollection(coid, "field4");
		for (int i = 0; i < 6; i++) {
			data.addElement("field4", new SimpleOid(103 + i));
		}		
		
		manager.save(data);
		
		
		
		ObjectData object = (ObjectData) manager.loadData(oid);
		TextString t = new TextString();
		object.restoreValue("field3", t);

		System.out.println(t + " " + t.title().toString());
		
		Date d2 = new Date();
		object.restoreValue("date", d2);

		System.out.println(d2 + " " + d2.saveString());

		DateTime ts2 = new DateTime();
		object.restoreValue("timestamp", ts2);

		System.out.println(ts2 + " " + ts2.saveString());

		Logical l2 = new Logical();
		object.restoreValue("logical", l2);

		System.out.println(l2 + " " + l2.saveString());
		
		Money m2 = new Money();
		object.restoreValue("money", m2);

		System.out.println(m2 + " " + m2.saveString());
	}
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

