package org.nakedobjects.utility.xmlsnapshot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Stateless utility methods for manipulating XML documents.
 */
public final class Helper {

	/**
	 * Helper method
	 */
	String trailingSlash(final String str) 
	{
		return str.endsWith("/")?str:str+"/";
	}

	/**
	 * Utility method that returns just the class's name for the supplied fully qualified class name.
	 * 
	 * cf 'basename' in Unix.
	 */
	String classNameFor(String fullyQualifiedClassName) 
	{
		int fullNameLastPeriodIdx = fullyQualifiedClassName.lastIndexOf('.');
		if (fullNameLastPeriodIdx > 0 && fullNameLastPeriodIdx < fullyQualifiedClassName.length()) 
		{
			return fullyQualifiedClassName.substring(fullNameLastPeriodIdx+1);
		} 
		else 
		{
			return fullyQualifiedClassName;
		}
	}

	/**
	 * Utility method that returns the package name for the supplied fully qualified class name, or
	 * <code>default</code> if  the class is in no namespace / in the default namespace.
	 * 
	 * cf 'dirname' in Unix.
	 */
	String packageNameFor(String fullyQualifiedClassName) 
	{
		int fullNameLastPeriodIdx = fullyQualifiedClassName.lastIndexOf('.');
		if (fullNameLastPeriodIdx > 0) 
		{
			return fullyQualifiedClassName.substring(0, fullNameLastPeriodIdx);
		} 
		else 
		{
			return "default"; // TODO: should provide a better way to specify namespace.
		}
	}
    
	/**
     * Returns the root element for the element by looking up the owner document for the element,
     * and from that its document element.
     * 
     * If no document element exists, just returns the supplied document.
     */
    Element rootElementFor(final Element element) 
	{
        Document doc = element.getOwnerDocument();
        if (doc == null) {
            return element;
        }
        Element rootElement = doc.getDocumentElement();
        if (rootElement == null) {
            return element;
        }
        return rootElement;
    }


	Document docFor(final Element element) {
		return element.getOwnerDocument();
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