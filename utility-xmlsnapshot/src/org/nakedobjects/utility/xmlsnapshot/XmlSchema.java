package org.nakedobjects.utility.xmlsnapshot;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;


/**
 * Represents the schema for the derived snapshot.
 */
public final class XmlSchema
{

	private String prefix;
    private String uriBase;
	private String uri;
	
	private final NofMetaModel nofMeta;
	private final XsMetaModel xsMeta;
	private final Helper helper;

	/**
	 * The base part of the namespace prefix to use if none explicitly supplied in the constructor.
	 */
	public final static String DEFAULT_PREFIX = "app";
    
    public XmlSchema() {
        this(NofMetaModel.DEFAULT_URI_BASE, XmlSchema.DEFAULT_PREFIX);
    }
	
																																																										     /**
     * @param uriBase   the prefix for the application namespace's URIs
     * @param prefix the prefix for the application namespace's prefix
     */
    public XmlSchema(String uriBase, final String prefix) {
		this.nofMeta = new NofMetaModel();
		this.xsMeta = new XsMetaModel();
		this.helper = new Helper();
		
        uriBase = new Helper().trailingSlash(uriBase);
        if (XsMetaModel.W3_ORG_XMLNS_URI.equals(uriBase)) {
            throw new IllegalArgumentException("Namespace URI reserved for w3.org XMLNS namespace");
        }
        if (XsMetaModel.W3_ORG_XMLNS_PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("Namespace prefix reserved for w3.org XMLNS namespace.");
        }
		if (XsMetaModel.W3_ORG_XS_URI.equals(uriBase)) {
			throw new IllegalArgumentException("Namespace URI reserved for w3.org XML schema namespace.");
		}
		if (XsMetaModel.W3_ORG_XS_PREFIX.equals(prefix)) {
			throw new IllegalArgumentException("Namespace prefix reserved for w3.org XML schema namespace.");
		}
		if (XsMetaModel.W3_ORG_XSI_URI.equals(uriBase)) {
			throw new IllegalArgumentException("Namespace URI reserved for w3.org XML schema-instance namespace.");
		}
		if (XsMetaModel.W3_ORG_XSI_PREFIX.equals(prefix)) {
			throw new IllegalArgumentException("Namespace prefix reserved for w3.org XML schema-instance namespace.");
		}
		if (NofMetaModel.NOF_METAMODEL_NS_URI.equals(uriBase)) {
            throw new IllegalArgumentException("Namespace URI reserved for NOF metamodel namespace.");
        }
        if (NofMetaModel.NOF_METAMODEL_NS_PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("Namespace prefix reserved for NOF metamodel namespace.");
        }
        this.uriBase = uriBase;
		this.prefix = prefix;
    }
    
    
    /**
     * The base of the Uri in use.  All namespaces are concatenated with this. 
     * 
     * The namespace string will be the concatenation of the plus the
     * package name of the class of the object being referenced.
     * 
     * If not specified in the constructor, then {@link #DEFAULT_URI_PREFIX} is used.
     */
    public String getUriBase() {
        return uriBase;
    }
    

    /**
     * Returns the namespace URI for the class.
     */
    void setUri(final String fullyQualifiedClassName) {
		if (uri != null) {
			throw new IllegalStateException("URI has already been specified.");
		}
		this.uri = getUriBase() + helper.packageNameFor(fullyQualifiedClassName) + "/" + helper.classNameFor(fullyQualifiedClassName);
	}


	/**
	 * The URI of the application namespace.
	 * 
	 * The value returned will be <code>null</code> until a {@link Snapshot} is created.
	 */
	public String getUri() {
		if (uri == null) {
			throw new IllegalStateException("URI has not been specified.");
		}
		return uri;
	}

	/**
	 * The prefix to the namespace for the application.
	 */
	public String getPrefix() {
		return this.prefix;
	}


	/**
	 * Creates an element with the specified localName, in the appropriate namespace for the NOS.
	 * 
	 * If necessary the namespace definition is added to the root element of the doc used to
	 * create the element.  The element is not parented but to avoid an error can only be added
	 * as a child of another element in the same doc.
	 */
	Element createElement(final Document doc, final String localName, final String fullyQualifiedClassName, final String singularName, final String pluralName) 
	{
		Element element = doc.createElementNS(getUri(), getPrefix() + ":" + localName);
		element.setAttributeNS(NofMetaModel.NOF_METAMODEL_NS_URI, "nof:fqn", fullyQualifiedClassName);
		element.setAttributeNS(NofMetaModel.NOF_METAMODEL_NS_URI, "nof:singular", singularName);
		element.setAttributeNS(NofMetaModel.NOF_METAMODEL_NS_URI, "nof:plural", pluralName);
		nofMeta.addNamespace(element); // good a place as any

		addNamespace(element, getPrefix(), getUri());
		return element;
	}


    /**
	 * Sets the target namespace for the XSD document to a URI derived from the
	 * fully qualified class name of the supplied object
	 */
	void setTargetNamespace(final Document xsdDoc, String fullyQualifiedClassName) {
		
		Element xsSchemaElement = xsdDoc.getDocumentElement();
		if (xsSchemaElement == null) {
			throw new IllegalArgumentException("XSD Document must have <xs:schema> element attached");
		}

		//	targetNamespace="http://www.nakedobjects.org/ns/app/<fully qualified class name>
		xsSchemaElement.setAttribute("targetNamespace", getUri());

		addNamespace(xsSchemaElement, getPrefix(), getUri());
	}

	/**
	 * Creates an &lt;xs:element&gt; element defining the presence of the named element
	 * representing a class
	 */ 
	Element createXsElementForNofClass(final Document xsdDoc, final Element element, final boolean addCardinality) {
		
		// gather details from XML element
		String localName = element.getLocalName();

		//	<xs:element name="AO11ConfirmAnimalRegistration">
		//		<xs:complexType>
		//			<xs:sequence>
		//             <xs:element ref="nof:title"/>
		//             <!-- placeholder -->
		//			</xs:sequence>
		//			<xs:attribute ref="nof:feature"
		//			              default="class"/>
		//			<xs:attribute ref="nof:oid"/>
		//			<xs:attribute ref="nof:annotation"/>
		//			<xs:attribute ref="nof:fqn"/>
		//	    </xs:complexType>
		//	</xs:element>
  
		// xs:element/@name="class name"
		// add to XML schema as a global attribute
		Element xsElementForNofClassElement = xsMeta.createXsElementElement(xsdDoc, localName, addCardinality);

		// xs:element/xs:complexType
		// xs:element/xs:complexType/xs:sequence
		Element xsComplexTypeElement = xsMeta.complexTypeFor(xsElementForNofClassElement);
		Element xsSequenceElement = xsMeta.sequenceFor(xsComplexTypeElement);

		// xs:element/xs:complexType/xs:sequence/xs:element ref="nof:title"
		Element xsTitleElement = xsMeta.createXsElement(helper.docFor(xsSequenceElement), "element");
		xsTitleElement.setAttribute("ref", NofMetaModel.NOF_METAMODEL_NS_PREFIX + ":" + "title");
		xsSequenceElement.appendChild(xsTitleElement);
		xsMeta.setXsCardinality(xsTitleElement, 0, 1);

		// xs:element/xs:complexType/xs:attribute ...
		xsMeta.addXsNofFeatureAttributeElements(xsComplexTypeElement, "class");
		xsMeta.addXsNofAttribute(xsComplexTypeElement, "oid");
		xsMeta.addXsNofAttribute(xsComplexTypeElement, "fqn");
		xsMeta.addXsNofAttribute(xsComplexTypeElement, "singular");
		xsMeta.addXsNofAttribute(xsComplexTypeElement, "plural");
		xsMeta.addXsNofAttribute(xsComplexTypeElement, "annotation");

		Place.setXsdElement(element, xsElementForNofClassElement);

		return xsElementForNofClassElement;
	}


	/**
	 * Creates an <code>xs:element</code> element to represent a value field in a class.
	 * 
	 * The returned element should be appended to <code>xs:sequence</code> element of the
	 * xs:element representing the type of the owning object.
	 */ 
	Element createXsElementForNofValue(final Element parentXsElementElement, final Element xmlValueElement) {

		// gather details from XML element
		String datatype = xmlValueElement.getAttributeNS(NofMetaModel.NOF_METAMODEL_NS_URI, "datatype");
		String fieldName = xmlValueElement.getLocalName();

		// <xs:element name="%owning object%">
		//		<xs:complexType>
		//			<xs:sequence>
		//				<xs:element name="%%field object%%">
		//					<xs:complexType>
		//						<xs:simpleContent>
		//							<xs:extension base="xs:string">
		//								<xs:attribute ref="nof:feature" fixed="value"/>
		//								<xs:attribute ref="nof:datatype" fixed="nof:%datatype%"/>
		//								<xs:attribute ref="nof:isEmpty"/>
		//			                    <xs:attribute ref="nof:annotation"/>
		//							</xs:extension>
		//						</xs:simpleContent>
		//					</xs:complexType>
		//				</xs:element>
		//			</xs:sequence>
		//		</xs:complexType>
		//	</xs:element>

		// xs:element/xs:complexType/xs:sequence
		Element parentXsComplexTypeElement = xsMeta.complexTypeFor(parentXsElementElement);
		Element parentXsSequenceElement = xsMeta.sequenceFor(parentXsComplexTypeElement);

		// xs:element/xs:complexType/xs:sequence/xs:element name="%%field object%"
		Element xsFieldElementElement = xsMeta.createXsElementElement(helper.docFor(parentXsSequenceElement), fieldName);
		parentXsSequenceElement.appendChild(xsFieldElementElement);

		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:simpleContent/xs:extension
		Element xsFieldComplexTypeElement = xsMeta.complexTypeFor(xsFieldElementElement);
		Element xsFieldSimpleContentElement = xsMeta.simpleContentFor(xsFieldComplexTypeElement);
		Element xsFieldExtensionElement = xsMeta.extensionFor(xsFieldSimpleContentElement, "string");

		xsMeta.addXsNofFeatureAttributeElements(xsFieldExtensionElement, "value");
		xsMeta.addXsNofAttribute(xsFieldExtensionElement, "datatype", datatype);
		xsMeta.addXsNofAttribute(xsFieldExtensionElement, "isEmpty");
		xsMeta.addXsNofAttribute(xsFieldExtensionElement, "annotation");

		return xsFieldElementElement;
	}

	/**
	 * Creates an &lt;xs:element&gt; element defining the presence of the named element
	 * representing a reference to a class; appended to xs:sequence element
	 */ 
	Element createXsElementForNofReference(final Element parentXsElementElement, final Element xmlReferenceElement) {

		// gather details from XML element
		String fieldName = xmlReferenceElement.getLocalName();

		// <xs:element name="%owning object%">
		//		<xs:complexType>
		//			<xs:sequence>
		//				<xs:element name="%%field object%%">
		//					<xs:complexType>
		//						<xs:sequence>
		//							<xs:element ref="nof:title" minOccurs="0"/>
		//							<xs:sequence minOccurs="0" maxOccurs="1"/>
		//						</xs:sequence>
		//						<xs:attribute ref="nof:feature" fixed="reference"/>
		//						<xs:attribute ref="nof:type" fixed="%%appX%%:%%type%%"/>
		//						<xs:attribute ref="nof:isEmpty"/>
		//						<xs:attribute ref="nof:annotation"/>
		//					</xs:complexType>
		//				</xs:element>
		//			</xs:sequence>
		//		</xs:complexType>
		//	</xs:element>

		// xs:element/xs:complexType/xs:sequence
		Element parentXsComplexTypeElement = xsMeta.complexTypeFor(parentXsElementElement);
		Element parentXsSequenceElement = xsMeta.sequenceFor(parentXsComplexTypeElement);

		// xs:element/xs:complexType/xs:sequence/xs:element name="%%field object%"
		Element xsFieldElementElement = xsMeta.createXsElementElement(helper.docFor(parentXsSequenceElement),fieldName);
		parentXsSequenceElement.appendChild(xsFieldElementElement);

		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence
		Element xsFieldComplexTypeElement = xsMeta.complexTypeFor(xsFieldElementElement);
		Element xsFieldSequenceElement = xsMeta.sequenceFor(xsFieldComplexTypeElement);

		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element ref="nof:title"
		Element xsFieldTitleElement = xsMeta.createXsElement(helper.docFor(xsFieldSequenceElement), "element");
		xsFieldTitleElement.setAttribute("ref", NofMetaModel.NOF_METAMODEL_NS_PREFIX + ":" + "title");
		xsFieldSequenceElement.appendChild(xsFieldTitleElement);
		xsMeta.setXsCardinality(xsFieldTitleElement, 0, 1);

		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:sequence   // placeholder
		Element xsReferencedElementSequenceElement = xsMeta.sequenceFor(xsFieldSequenceElement);
		xsMeta.setXsCardinality(xsReferencedElementSequenceElement, 0, 1);

		xsMeta.addXsNofFeatureAttributeElements(xsFieldComplexTypeElement, "reference");
		xsMeta.addXsNofAttribute(xsFieldComplexTypeElement, "type");
		xsMeta.addXsNofAttribute(xsFieldComplexTypeElement, "isEmpty");
		xsMeta.addXsNofAttribute(xsFieldComplexTypeElement, "annotation");

		return xsFieldElementElement;
	}

	
	/**
	 * Creates an &lt;xs:element&gt; element defining the presence of the named element
	 * representing a collection in a class; appended to xs:sequence element
	 */ 
	Element createXsElementForNofCollection(final Element parentXsElementElement, final Element xmlCollectionElement) {

		// gather details from XML element
		String fieldName = xmlCollectionElement.getLocalName();

		// <xs:element name="%owning object%">
		//		<xs:complexType>
		//			<xs:sequence>
		//				<xs:element name="%%field object%%">
		//					<xs:complexType>
		//						<xs:sequence>
		//							<xs:element ref="nof:oids" minOccurs="0" maxOccurs="1"/>
		//						    <!-- nested element definitions go here -->
		//						</xs:sequence>
		//						<xs:attribute ref="nof:feature" fixed="collection"/>
		//						<xs:attribute ref="nof:type" fixed="%%appX%%:%%type%%"/>
		//						<xs:attribute ref="nof:size"/>
		//						<xs:attribute ref="nof:annotation"/>
		//					</xs:complexType>
		//				</xs:element>
		//			</xs:sequence>
		//		</xs:complexType>
		//	</xs:element>

		// xs:element/xs:complexType/xs:sequence
		Element parentXsComplexTypeElement = xsMeta.complexTypeFor(parentXsElementElement);
		Element parentXsSequenceElement = xsMeta.sequenceFor(parentXsComplexTypeElement);

		// xs:element/xs:complexType/xs:sequence/xs:element name="%field object%%"
		Element xsFieldElementElement = xsMeta.createXsElementElement(helper.docFor(parentXsSequenceElement), fieldName);
		parentXsSequenceElement.appendChild(xsFieldElementElement);

		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType
		Element xsFieldComplexTypeElement = xsMeta.complexTypeFor(xsFieldElementElement);
		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence
		Element xsFieldSequenceElement = xsMeta.sequenceFor(xsFieldComplexTypeElement);

		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element ref="nof:oids"
		Element xsFieldOidsElement = xsMeta.createXsElement(helper.docFor(xsFieldSequenceElement), "element");
		xsFieldOidsElement.setAttribute("ref", NofMetaModel.NOF_METAMODEL_NS_PREFIX + ":" + "oids");
		xsFieldSequenceElement.appendChild(xsFieldOidsElement);
		xsMeta.setXsCardinality(xsFieldOidsElement, 0, 1);

//		// xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:choice
//		Element xsFieldChoiceElement = xsMeta.choiceFor(xsFieldComplexTypeElement); // placeholder
//		xsMeta.setXsCardinality(xsFieldChoiceElement, 0, Integer.MAX_VALUE);

//		Element xsFieldTitleElement = addXsNofRefElementElement(xsFieldSequenceElement, "title");

//		Element xsReferencedElementSequenceElement = sequenceFor(xsFieldSequenceElement);
//		setXsCardinality(xsReferencedElementSequenceElement, 0, 1);

		xsMeta.addXsNofFeatureAttributeElements(xsFieldComplexTypeElement, "collection");
		xsMeta.addXsNofAttribute(xsFieldComplexTypeElement, "type");
		xsMeta.addXsNofAttribute(xsFieldComplexTypeElement, "size");
		xsMeta.addXsNofAttribute(xsFieldComplexTypeElement, "annotation");

		return xsFieldElementElement;
	}



	/**
	 * 
	 * <pre>
	 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 * xsi:schemaLocation="http://www.nakedobjects.org/ns/app/sdm.common.fixture.schemes.ao.communications ddd.xsd"
	 * </pre>
	 * 
	 * Assumes that the URI has been specified. 
	 * 
	 * @param xmlDoc
	 * @param fullyQualifiedClassName
	 * @param schemaLocationFileName
	 */
	void assignSchema(final Document xmlDoc, final String fullyQualifiedClassName, final String schemaLocationFileName) {

		String xsiSchemaLocationAttrValue = getUri() + " " + schemaLocationFileName;

		Element rootElement = xmlDoc.getDocumentElement();

		// xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		addNamespace(rootElement, XsMetaModel.W3_ORG_XSI_PREFIX, XsMetaModel.W3_ORG_XSI_URI);
		
		// xsi:schemaLocation="http://www.nakedobjects.org/ns/app/<fully qualified class name> sdm.common.fixture.schemes.ao.communications sdm.common.fixture.schemes.ao.communications.AO11ConfirmAnimalRegistration.xsd"
		rootElement.setAttributeNS(XsMetaModel.W3_ORG_XSI_URI, "xsi:schemaLocation", xsiSchemaLocationAttrValue);
	}

	/**
	 * Adds a previously created &lt;xs:element&gt; element (representing a field of an object)
	 * to the supplied element (presumed to be a <code>complexType/sequence</code>).
	 */ 
	void addFieldXsElement(final Element xsElement, final Element xsFieldElement) {
		if (xsFieldElement == null) {
			return;
		}
		Element sequenceElement = xsMeta.sequenceForComplexTypeFor(xsElement);
		sequenceElement.appendChild(xsFieldElement);
	}

	
	/**
	 * Adds a namespace using the supplied prefix and the supplied URI to the
	 * root element of the document that is the parent of the supplied element.
	 * 
	 * If the namespace declaration already exists but has a different URI (shouldn't
	 * normally happen) overwrites with supplied URI.
	 */
	private void addNamespace(Element element, String prefix, String nsUri) 
	{
		Element rootElement = helper.rootElementFor(element);
		// see if we have the NS prefix there already
		String existingNsUri = rootElement.getAttributeNS(XsMetaModel.W3_ORG_XMLNS_URI, prefix);
		// if there is none (or it is different from what we want), then set the attribute
		if (existingNsUri == null || !existingNsUri.equals(nsUri)) {
			helper.rootElementFor(element).setAttributeNS(XsMetaModel.W3_ORG_XMLNS_URI, XsMetaModel.W3_ORG_XMLNS_PREFIX+":" + prefix, nsUri);
		}
	}

	Element addXsElementIfNotPresent(final Element parentXsElement, final Element childXsElement) {

		Element parentChoiceOrSequenceElement = xsMeta.choiceOrSequenceFor(xsMeta.complexTypeFor(parentXsElement));

		if (parentChoiceOrSequenceElement == null) {
			throw new IllegalArgumentException("Unable to locate complexType/sequence or complexType/choice under supplied parent XSD element");
		}

		NamedNodeMap childXsElementAttributeMap = childXsElement.getAttributes();
		Attr childXsElementAttr = (Attr)childXsElementAttributeMap.getNamedItem("name");
		String localName = childXsElementAttr.getValue();

		NodeList existingElements = parentChoiceOrSequenceElement.getElementsByTagNameNS("*", childXsElement.getLocalName());
		for(int i=0; i<existingElements.getLength(); i++) {
			Element xsElement = (Element)existingElements.item(i);
			NamedNodeMap xsElementAttributeMap = xsElement.getAttributes();
			Attr attr = (Attr)xsElementAttributeMap.getNamedItem("name");
			if (attr != null && attr.getValue().equals(localName)) {
				return xsElement;
			}
		}

		parentChoiceOrSequenceElement.appendChild(childXsElement);  
		return childXsElement;
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