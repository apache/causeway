package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Validity;
import org.nakedobjects.security.SecurityContext;


public interface ValueIf extends MemberIf {

	About getAbout(SecurityContext context, NakedObject object);
	
	void restoreValue(NakedObject inObject, Object setValue);

	void setValue(NakedObject inObject, NakedValue value);
	
//	void set(NakedObject inObject, String setValue) throws InvalidEntryException;
	
	boolean isDerived();

	Naked get(NakedObject fromObject);

	boolean hasAbout();
	
	Class getType();

    void parseValue(NakedValue value, String setValue)  throws ValueParseException;

    void isValid(NakedObject inObject, Validity validity);
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
