package org.nakedobjects.utility.xmlsnapshot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Stateless utility methods relating to the NOF meta model.
 */
final class NofMetaModel {

	/**
	 * The generated XML schema references the NOF metamodel schema.  This is the
	 * default location for this schema.
	 */
	public static final String DEFAULT_NOF_SCHEMA_LOCATION = "nof.xsd";
	/**
	 * URI representing the namespace of NakedObject framework's metamodel.
	 * 
	 * The NamespaceManager  will not allow any namespaces with this URI to be added.
	 */
	public static final String NOF_METAMODEL_NS_URI = "http://www.nakedobjects.org/ns/0.1/metamodel";
	/**
	 * Namespace prefix for {@link NOF_METAMODEL_NS_URI}.
	 * 
	 * The NamespaceManager will not allow any namespace to use this prefix.
	 */
	public static final String NOF_METAMODEL_NS_PREFIX = "nof";
	/**
	 * The base of the namespace URI to use for application namespaces if none explicitly supplied in the
	 * constructor.
	 */
	public final static String DEFAULT_URI_BASE = "http://www.nakedobjects.org/ns/app/";
	
    /**
	 * Enumeration of nof:feature attribute representing a class
	 */
	public static final String NOF_METAMODEL_FEATURE_CLASS = "class";
	/**
	 * Enumeration of nof:feature attribute representing a value field
	 */
	public static final String NOF_METAMODEL_FEATURE_VALUE = "value";
	/**
	 * Enumeration of nof:feature attribute representing a reference (1:1 association)
	 */
	public static final String NOF_METAMODEL_FEATURE_REFERENCE= "reference";
	/**
	 * Enumeration of nof:feature attribute representing a collection (1:n association)
	 */
	public static final String NOF_METAMODEL_FEATURE_COLLECTION = "collection";

    

	private final Helper helper;

	public NofMetaModel() {
		this.helper = new Helper();
	}

	
	/**
	 * Creates an element in the NOF namespace.
	 * The NOF namespace is added (though it almost certainly would have been defined for some parent element
	 * because nof:xxx elements only every appear as children).
	 */
	Element createElement(final Document doc, final String localName) 
	{
		Element element = doc.createElementNS(NofMetaModel.NOF_METAMODEL_NS_URI, NofMetaModel.NOF_METAMODEL_NS_PREFIX+":" + localName);
		addNamespace(element);
		return element;
	}

	/**
	 * Appends an <code>nof:title</code> element with the supplied title string
	 * to the provided element.
	 */
	public void appendNofTitle(Element element, String titleStr) {
		final Document doc = helper.docFor(element);
		Element titleElement = createElement(doc, "title");
		element.appendChild(titleElement);
		titleElement.appendChild(doc.createTextNode(titleStr));
	}
	
	
	void addNamespace(final Element element) 
	{
		helper.rootElementFor(element).setAttributeNS(XsMetaModel.W3_ORG_XMLNS_URI,
			XsMetaModel.W3_ORG_XMLNS_PREFIX+":"+NofMetaModel.NOF_METAMODEL_NS_PREFIX, NofMetaModel.NOF_METAMODEL_NS_URI);
	}




	/**
	 * Adds <code>nof:feature=&quot;class&quot;</code> attribute and
	 * <code>nof:oid=&quote;...&quot;</code> for the supplied element.
	 */
	void setAttributesForClass(Element element, String oid) 
	{
		setAttribute(element, "feature", NOF_METAMODEL_FEATURE_CLASS);
		setAttribute(element, "oid", oid);
	}


	/**
	 * Adds <code>nof:feature=&quot;value&quot;</code> attribute and
	 * <code>nof:datatype=&quote;...&quot;</code> for the supplied element.
	 */
	void setAttributesForValue(Element element, String datatypeName) 
	{
		setAttribute(element, "feature", NOF_METAMODEL_FEATURE_VALUE);
		setAttribute(element, "datatype", NofMetaModel.NOF_METAMODEL_NS_PREFIX + ":" + datatypeName);
	}

	/**
	 * Adds <code>nof:feature=&quot;reference&quot;</code> attribute and
	 * <code>nof:type=&quote;...&quot;</code> for the supplied element.
	 */
	void setAttributesForReference(Element element, String prefix, String fullyQualifiedClassName) 
	{
		setAttribute(element, "feature", NOF_METAMODEL_FEATURE_REFERENCE);
		setAttribute(element, "type", prefix + ":" + fullyQualifiedClassName );
	}

	/**
	 * Adds <code>nof:feature=&quot;collection&quot;</code> attribute, the
	 * <code>nof:type=&quote;...&quot;</code> and the <code>nof:size=&quote;...&quot;</code>
	 * for the supplied element.
	 */
	void setNofCollection(Element element, String prefix, String fullyQualifiedClassName, int size) 
	{
		setAttribute(element, "feature", NOF_METAMODEL_FEATURE_COLLECTION);
		setAttribute(element, "type", prefix + ":" + fullyQualifiedClassName );
		setAttribute(element, "size", ""+size);
	}


	/**
	 * Adds an <code>nof:isEmpty</code> attribute for the supplied class to the
	 * supplied element.
	 */
	void setIsEmptyAttribute(Element element, boolean isEmpty) 
	{
		setAttribute(element, "isEmpty", ""+isEmpty);
	}

	/**
	 * Adds an <code>nof:annotation</code> attribute for the supplied class to the
	 * supplied element.
	 */
	void setAnnotationAttribute(Element element, String annotation) 
	{
		setAttribute(element, "annotation", NofMetaModel.NOF_METAMODEL_NS_PREFIX + ":" + annotation);
	}

	
	/**
	 * Gets an attribute with the supplied name in the NOF namespace from the supplied element
	 */
	String getAttribute(Element element, String attributeName) 
	{
		return element.getAttributeNS(NofMetaModel.NOF_METAMODEL_NS_URI, attributeName);
	}

	
	/**
	 * Sets an attribute of the supplied element with the attribute being in the NOF namespace.
	 */
	private void setAttribute(Element element, String attributeName, String attributeValue) 
	{
		element.setAttributeNS(NofMetaModel.NOF_METAMODEL_NS_URI, NofMetaModel.NOF_METAMODEL_NS_PREFIX+":" + attributeName, attributeValue);
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