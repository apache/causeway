package org.nakedobjects.utility.xmlsnapshot;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.NakedObjectSpecificationException;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.ValueFieldSpecification;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Traverses object graph from specified root, so that an XML representation of
 * the graph can be returned.
 * 
 * Initially designed to allow snapshots to be easily created.
 * 
 * Typical use:
 * 
 * <pre>
 * XmlSnapshot snapshot = new XmlSnapshot(customer); // where customer is a reference to an ANO
 * Element customerAsXml = snapshot.toXml(); // returns customer's fields, titles of simple references, number of items in collections
 * snapshot.include(&quot;placeOfBirth&quot;); // navigates to ANO represented by simple reference &quot;placeOfBirth&quot;
 * snapshot.include(&quot;orders/product&quot;); // navigates to all Orders of Customer, and from them for their products 
 * 
 * </pre>
 * 
 * A possible enhancement (not yet implemented) might be to allow XPath
 * restrictions, perhaps with parameters. eg:
 * 
 * <pre>
 * builder.include(&quot;orders[date.after(:beginningOfYear)]&quot;, new String[] { beginningOfYear.toString() });
 * </pre>
 */
public final class XmlSnapshot {

	private static final Logger LOG = Logger.getLogger(XmlSnapshot.class);

	/**
	 * not serialized into Snapshot
	 */
	private static final String[] SKIP_FIELDS = new String[] {"rawxml", "rawxsd", "rawxsl" , "lastactivity" };
    
	private final Place rootPlace;

	private final NofMetaModel nofMeta;
	private final Helper helper;


	
	
	private final Document xmlDocument;
	private final Document xsdDocument;
	
	/**
	* root element of {@link #xmlDocument}
	*/ 
	private Element xmlElement;
	/**
		* root element of {@link #xsdDocument}
		*/ 
	private final Element xsdElement;

	/**
	 * the suggested location for the schema (xsi:schemaLocation attribute)
	 */
	private String schemaLocationFileName;

	private final XmlSchema schema;

	private final XsMetaModel xsMeta;

	
	
	/**
	 * Start a snapshot at the root object, using own namespace manager.
	 */
	public XmlSnapshot(final NakedObject rootObject) {
		this(rootObject, new XmlSchema());
	}

	/**
	 * Start a snapshot at the root object, using supplied namespace manager.
	 */
	public XmlSnapshot(final NakedObject rootObject, final XmlSchema schema) {

		this.nofMeta = new NofMetaModel();
		this.xsMeta = new XsMetaModel();
		this.helper = new Helper();

		this.schema = schema;

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			this.xmlDocument = db.newDocument();
			this.xsdDocument = db.newDocument();

			xsdElement = xsMeta.createXsSchemaElement(xsdDocument);

			this.rootPlace = appendXml(rootObject);

		} catch (ParserConfigurationException e) {
			LOG.error("Unable to build snapshot", e);
			throw new NakedObjectRuntimeException(e);
		}

	}

	public NakedObject getObject() {
		return rootPlace.getObject();
	}
    
	public XmlSchema getSchema() {
		return getSchema();
	}
    

	/**
	 * Creates an Element representing this object, and appends it as the 
	 * root element of the Document.
	 * 
	 * The Document must not yet have a root element  Additionally, the supplied
	 * schemaManager must be populated with any application-level namespaces
	 * referenced in the document that the parentElement resides within.
	 * (Normally this is achieved simply by using
	 * appendXml passing in a new schemaManager - see
	 * {@link #toXml()} or {@link XmlSnapshot}).
	 */
	private Place appendXml(final NakedObject object) {

		String fullyQualifiedClassName = object.getSpecification().getFullName();

		schema.setUri(fullyQualifiedClassName); // derive URI from fully qualified name
		
		Place place = objectToElement(object);

		Element element = place.getXmlElement();
		Element xsElementElement = place.getXsdElement();
		
		getXmlDocument().appendChild(element); // add as root element to the XML document
		getXsdElement().appendChild(xsElementElement); // add as xs:element to xs:schema of the XSD document
		
		// target name space derived from object
		schema.setTargetNamespace(getXsdDocument(), fullyQualifiedClassName);
		
		// schemaLocation also derived from object
		String schemaLocationFileName = fullyQualifiedClassName + ".xsd";
		schema.assignSchema(getXmlDocument(), fullyQualifiedClassName, schemaLocationFileName);
		
		// copy into snapshot
		setXmlElement(element);
		setSchemaLocationFileName(schemaLocationFileName);
		
		return place;
	}

	/**
	 * Creates an Element representing this object, and appends it to the
	 * supplied parentElement, provided that an element for the object is not
	 * already appended.
	 * 
	 * The method uses the OID to determine if an object's element is already
	 * present.  If the object is not yet persistent, then the hashCode is used
	 * instead.
	 * 
	 * The parentElement must have an owner document, and should define the nof
	 * namespace. Additionally, the supplied schemaManager must be populated
	 * with any application-level namespaces referenced in the document that the
	 * parentElement resides within. (Normally this is achieved simply by using
	 * appendXml passing in a rootElement and a new schemaManager - see
	 * {@link #toXml()}or {@link XmlSnapshot}).
	 */
	private Element appendXml(final Place parentPlace, final NakedObject childObject) {
		
		Element parentElement = parentPlace.getXmlElement();
		Element parentXsElement = parentPlace.getXsdElement();

		if (parentElement.getOwnerDocument() != getXmlDocument()) {
			throw new IllegalArgumentException("parent XML Element must have snapshot's XML document as its owner");
		}

		Place childPlace = objectToElement(childObject);
		Element childElement = childPlace.getXmlElement();
		Element childXsElement = childPlace.getXsdElement();

		addElementIfNotPresent(parentElement, childElement);
		schema.addXsElementIfNotPresent(parentXsElement, childXsElement);

		return childElement;
	}



	Place objectToElement(final NakedObject object) {

		NakedObjectSpecification nos = object.getSpecification();

		Element element = schema.createElement(getXmlDocument(), nos.getShortName(), nos.getFullName());
		Element xsElement = schema.createXsElementForNofClass(getXsdDocument(), element);

		Place place = new Place(object, element);

		nofMeta.setAttributesForClass(element, oidOrHashCode(object).toString());

		FieldSpecification[] fields = nos.getFields();
eachField:
		for (int i = 0; i < fields.length; i++) {
			FieldSpecification field = fields[i];
			String fieldName = field.getName();
			// skip fields that contain Raw XML
			// TODO: change this to be based on value type instead.
eachSkipField:
			for(int j=0; j<SKIP_FIELDS.length; j++) {
				if (fieldName.equals(SKIP_FIELDS[j])) {
					continue eachField;
				}
			}
			Element xmlFieldElement =
				getXmlDocument().createElementNS(schema.getUri(), // scoped by namespace of class of containing object
									schema.getPrefix() + ":" + fieldName);

			
			Element xsdFieldElement = null;

			if (field instanceof ValueFieldSpecification) {

				ValueFieldSpecification valueFieldSpec = ((ValueFieldSpecification) field);
				Naked value = valueFieldSpec.get(object);
				Element xmlValueElement = xmlFieldElement; // more meaningful locally scoped name

				// a null value would be a programming error, but we protect against it anyway
				if (value == null) {
					continue;
				}

				// XML
				nofMeta.setAttributesForValue(xmlValueElement, value.getSpecification().getShortName() );

				boolean isEmpty = (value.titleString().length() > 0);
				if (isEmpty) {
					String valueStr = value.titleString();
					xmlValueElement.appendChild(getXmlDocument().createTextNode(valueStr));
				} 
				else {
					nofMeta.setIsEmptyAttribute(xmlValueElement, true);
				}

				// XSD
				xsdFieldElement = schema.createXsElementForNofValue(xsElement, xmlValueElement);

			} else if (field instanceof OneToOneAssociationSpecification) {

				OneToOneAssociationSpecification oneToOneAssocSpec = ((OneToOneAssociationSpecification) field);
				NakedObject referencedNakedObject = (NakedObject) oneToOneAssocSpec.get(object);
				String fullyQualifiedClassName = oneToOneAssocSpec.getType().getFullName();
				Element xmlReferenceElement = xmlFieldElement; // more meaningful locally scoped name

				// XML
				nofMeta.setAttributesForReference(xmlReferenceElement, schema.getPrefix(), fullyQualifiedClassName);

				if (referencedNakedObject != null) {
					nofMeta.appendNofTitle(xmlReferenceElement, referencedNakedObject.titleString());
				} 
				else {
					nofMeta.setIsEmptyAttribute(xmlReferenceElement, true);
				}

				// XSD
				xsdFieldElement = schema.createXsElementForNofReference(xsElement, xmlReferenceElement);


			} else if (field instanceof OneToManyAssociationSpecification) {

				OneToManyAssociationSpecification oneToManyAssociation = (OneToManyAssociationSpecification) field;
				InternalCollection collection = (InternalCollection) oneToManyAssociation.get(object);
				String fullyQualifiedClassName = collection.getElementSpecification().getFullName();
				Element xmlCollectionElement = xmlFieldElement; // more meaningful locally scoped name

				// XML
				nofMeta.setNofCollection(xmlCollectionElement, schema.getPrefix(), fullyQualifiedClassName, collection.size());

				// XSD
				xsdFieldElement = schema.createXsElementForNofCollection(xsElement, xmlCollectionElement);

			} else {
				continue;
			}


			if (xsdFieldElement != null) {
				Place.setXsdElement(xmlFieldElement, xsdFieldElement);
			}
			
			// XML
			xmlFieldElement = addElementIfNotPresent(element, xmlFieldElement);

			// XSD
			if (xsdFieldElement != null) {
				schema.addFieldXsElement(xsElement, xsdFieldElement);
			}
		}

		return place;
	}


	public void include(final String path) {
		include(path, null);
	}
	public void include(final String path, final String annotation) {
        
		// tokenize into successive fields
		Vector fieldNames = new Vector();
		for(StringTokenizer tok = new StringTokenizer(path, "/"); tok.hasMoreTokens(); ) {
			fieldNames.addElement(tok.nextToken().toLowerCase());
		}
        	
		// navigate first field, from the root.
		includeField(rootPlace, fieldNames, annotation);
	}
    

	public Document getXmlDocument() {
		return xmlDocument;
	}

	public Document getXsdDocument() {
		return xsdDocument;
	}

	/**
		* The root element of {@link #getXmlDocument()}.
		* Returns <code>null</code> until the snapshot has actually been built.
		*/
	public Element getXmlElement() {
		return xmlElement;
	}
	/**
	 * @param xmlElement The xmlElement to set.
	 */
	private void setXmlElement(Element xmlElement) {
		this.xmlElement = xmlElement;
	}

	/**
		* The root element of {@link #getXsdDocument()}.
		* Returns <code>null</code> until the snapshot has actually been built.
		*/
	public Element getXsdElement() {
		return xsdElement;
	}

	/**
		* The name of the <code>xsi:schemaLocation</code> in the XML document.
		* 
		* Taken from the <code>fullyQualifiedClassName</code> (which also is used as
		* the basis for the <code>targetNamespace</code>.
		* 
		* Populated in {@link #appendXml(NakedObject)}.
		*/
	public String getSchemaLocationFileName() {
		return schemaLocationFileName;
	}
	
	/**
	 * @param schemaLocationFileName The schemaLocationFileName to set.
	 */
	private void setSchemaLocationFileName(String schemaLocationFileName) {
		this.schemaLocationFileName = schemaLocationFileName;
	}


	
	/**
	 * @return true if able to navigate the complete vector of field names successfully; false if a
	 *                   field could not be located or it turned out to be a value.
	 */
	private boolean includeField(final Place place, Vector fieldNames, final String annotation) {

		NakedObject object = place.getObject();
		Element xmlElement = place.getXmlElement();

		// we use a copy of the path so that we can safely traverse collections without side-effects
		Vector fieldNamesOrig = fieldNames;
		fieldNames = new Vector();
		for(java.util.Enumeration enum = fieldNamesOrig.elements(); enum.hasMoreElements(); ) {
			fieldNames.addElement(enum.nextElement());
		}

		// see if we have any fields to process
		if (fieldNames.size() == 0) {
			return true;
		}

		// take the first field name from the list, and remove
		String fieldName = (String)fieldNames.elementAt(0);
		fieldNames.removeElementAt(0);
        
		// locate the field in the object's class
		NakedObjectSpecification nos = object.getSpecification();
		FieldSpecification field = null;
		try {
			// HACK: really want a NakedObjectSpecification.hasField method to check first.
			field = nos.getField(fieldName);
		} catch(NakedObjectSpecificationException ex) {
			return false;
		}
        

		// locate the corresponding XML element
		NodeList xmlFieldElements = xmlElement.getElementsByTagNameNS("*", field.getName());
		if (xmlFieldElements.getLength() != 1) {
			return false;
		}
		Element xmlFieldElement =  (Element)xmlFieldElements.item(0);
            
		if (fieldNames.size() == 0 && annotation != null) {
			// nothing left in the path, so we will apply the annotation now
			nofMeta.setAnnotationAttribute(xmlFieldElement, annotation);
		}
        

		// locate the corresponding XSD element
//		Element xsdFieldElement = getXsdElement(xmlFieldElement);

       
		Place fieldPlace = new Place(object, xmlFieldElement);

		if (field instanceof ValueFieldSpecification) {
			return false;
            
		} else if (field instanceof OneToOneAssociationSpecification) {
			OneToOneAssociationSpecification oneToOneAssociation = ((OneToOneAssociationSpecification) field);
			NakedObject referencedObject = (NakedObject) oneToOneAssociation.get(fieldPlace.getObject()); // cast to ANO rather than NO because haven't (yet) put toXml() onto NO interface.

			if (referencedObject == null) {
				return true; // not a failure if the reference was null
			}

			return appendXmlThenIncludeRemaining(fieldPlace, referencedObject, fieldNames, annotation);
        
		} else if (field instanceof OneToManyAssociationSpecification) {
			OneToManyAssociationSpecification oneToManyAssociation = (OneToManyAssociationSpecification) field;
			InternalCollection collection = (InternalCollection) oneToManyAssociation.get(fieldPlace.getObject());
            
			boolean allFieldsNavigated = true;
			for (int i = 0; i < collection.size(); i++) {
                
				NakedObject referencedObject = (NakedObject) collection.elementAt(i); // cast because haven't (yet) put toXml() on the NO interface, only on ANO.

				allFieldsNavigated = allFieldsNavigated && appendXmlThenIncludeRemaining(fieldPlace, referencedObject, fieldNames, annotation);
			}
			return allFieldsNavigated;
		}
        
		return false; // fall through, shouldn't get here but just in case.
	}

	private boolean appendXmlThenIncludeRemaining(Place parentPlace, NakedObject referencedObject, final Vector fieldNames, final String annotation) {

		Element referencedElement = appendXml(parentPlace, referencedObject);
		Place referencedPlace = new Place(referencedObject, referencedElement);

		return includeField(referencedPlace, fieldNames, annotation);
	}

	private Element addElementIfNotPresent(final Element parentElement, final Element childElement) {


		// before we add the child element, check to see if it is already there
		NodeList existingElements = parentElement.getElementsByTagNameNS("*" /* childElement.getNamespaceURI() */,
			                        childElement.getLocalName());
		if (existingElements.getLength() == 1) {
			Element possibleMatch = (Element) existingElements.item(0);
			String childElementOid = nofMeta.getAttribute(childElement, "oid");
			String possibleMatchOid = nofMeta.getAttribute(possibleMatch, "oid");
			if (childElementOid != null && possibleMatchOid != null && childElementOid.equals(possibleMatchOid)) {
				return possibleMatch;
			}
		}
		parentElement.appendChild(childElement);
		return childElement;
	}

	private String oidOrHashCode(final NakedObject object) {
		return (object.getOid() != null)? object.getOid().toString() : ""+object.hashCode();	
	}


}


/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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