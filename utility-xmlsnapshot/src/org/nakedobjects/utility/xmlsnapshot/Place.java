package org.nakedobjects.utility.xmlsnapshot;

import org.nakedobjects.object.NakedObject;

import org.apache.crimson.tree.ElementNode2;
import org.w3c.dom.Element;


/**
 * Represents a place in the graph to be navigated; really just wraps an object and an XML
 * Element in its XML document.  Also provides the capability to extract the corresponding XSD
 * element (associated with each XML element).
 * 
 * The XML element (its children) is mutated as the graph of objects is navigated.
 */
final class Place {
	private final NakedObject object;
	private final Element element;
    
	Place(final NakedObject object, final Element element) {
		this.object = object;
		this.element = element;
	}
    
	public Element getXmlElement() {
		return element;
	}
    
	public NakedObject getObject() {
		return object;
	}
    
	public Element getXsdElement() {
		if (!(element instanceof ElementNode2)) {
			return null;
		}
		ElementNode2 e = (ElementNode2)element;
		Object o = e.getUserObject();
		if (o == null || !(o instanceof Element)) {
			return null;
		}
		return (Element)o;
	}

	// TODO: smelly; where should this responsibility lie?
	static void setXsdElement(final Element element, final Element xsElement) {
		if (!(element instanceof ElementNode2)) {
			return;
		}
		ElementNode2 e = (ElementNode2)element;
		e.setUserObject(xsElement);
	}


}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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