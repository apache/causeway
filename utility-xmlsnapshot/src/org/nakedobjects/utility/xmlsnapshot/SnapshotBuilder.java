package org.nakedobjects.utility.xmlsnapshot;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.defaults.value.Snapshot;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.NakedObjectSpecificationException;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.ValueFieldSpecification;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.crimson.jaxp.DocumentBuilderFactoryImpl;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
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
 * SnapshotBuilder navigator = new SnapshotBuilder(customer); // where customer is a reference to an ANO
 * Element customerAsXml = navigator.toXml(); // returns customer's fields, titles of simple references, number of items in collections
 * navigator.navigate(&quot;placeOfBirth&quot;); // navigates to ANO represented by simple reference &quot;placeOfBirth&quot;
 * navigator.navigate(&quot;orders/product&quot;); // navigates to all Orders of Customer, and from them for their products 
 * 
 * </pre>
 * 
 * A possible enhancement (not yet implemented) might be to allow XPath
 * restrictions, perhaps with parameters. eg:
 * 
 * <pre>
 * navigator.navigate(&quot;orders[date.after(:beginningOfYear)]&quot;, new String[] { beginningOfYear.toString() });
 * </pre>
 */
public final class SnapshotBuilder {

    private static final Logger LOG = Logger.getLogger(SnapshotBuilder.class);

    /**
     * Represents a place in the graph to be navigated; really just wraps an object and a copy
     * of its XML together.  The copy of the XML is mutated as the graph of objects is navigated.
     */
    private static class Place {
        private final NakedObject object;
        private final Element element;
        
        Place(final NakedObject object, final Element element) {
            this.object = object;
            this.element = element;
        }
        
        public Element getElement() {
            return element;
        }
        
        public NakedObject getObject() {
            return object;
        }
        
     }
    
    private final Place rootPlace;
    private final NamespaceManager namespaceManager;

    /**
     * Start a snapshot at the root object, using own namespace manager.
     */
    public SnapshotBuilder(final NakedObject rootObject) {
        this(rootObject, new NamespaceManager());
    }

    /**
     * Start a snapshot at the root object, using supplied namespace manager.
     */
    public SnapshotBuilder(final NakedObject rootObject, final NamespaceManager namespaceManager) {
        this.namespaceManager = namespaceManager;
        this.rootPlace = new Place(rootObject, toXml(rootObject, namespaceManager));
    }

    public NakedObject getRootObject() {
        return rootPlace.getObject();
    }
    
    public NamespaceManager getNamespaceManager() {
        return namespaceManager;
    }
    
    
    private Element toXml(final NakedObject object, NamespaceManager namespaceManager) {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element rootElement = namespaceManager.createNofElement(doc, "snapshot");

            doc.appendChild(rootElement);
            
            appendXml(namespaceManager, object, rootElement);
            return rootElement;
        } catch (ParserConfigurationException e) {
            e.printStackTrace(System.err);
            LOG.error("Unable to build snapshot", e);
        }
        return null;
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
    
    
    /**
     * @return true if able to navigate the complete vector of field names successfully; false if a
     *                   field could not be located or it turned out to be a value.
     */
    private boolean includeField(final Place place, Vector fieldNames, final String annotation) {

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
        NakedObjectSpecification nakedClass = place.getObject().getSpecification();
        FieldSpecification field = null;
        try {
            // HACK: really want a NakedClass.hasField method to check first.
            field = nakedClass.getField(fieldName);
        } catch(NakedObjectSpecificationException ex) {
            return false;
        }
        
        // locate the corresponding XML element
        NodeList fieldElements = place.getElement().getElementsByTagNameNS("*", field.getName());
        if (fieldElements.getLength() != 1) {
            return false;
        }
        Element fieldElement =  (Element)fieldElements.item(0);
            
        if (fieldNames.size() == 0 && annotation != null) {
            // nothing left in the path, so we will apply the annotation now
            namespaceManager.setNofAttribute(fieldElement, "annotation", annotation);
        }
        

        if (field instanceof ValueFieldSpecification) {
            return false;
            
        } else if (field instanceof OneToOneAssociationSpecification) {
            OneToOneAssociationSpecification oneToOneAssociation = ((OneToOneAssociationSpecification) field);
            NakedObject referencedObject = (NakedObject) oneToOneAssociation.get(place.getObject()); // cast to ANO rather than NO because haven't (yet) put toXml() onto NO interface.

            if (referencedObject == null) {
                return true; // not a failure if the reference was null
            }

            return appendXmlThenIncludeRemaining(fieldElement, referencedObject, fieldNames, annotation);
        
        } else if (field instanceof OneToManyAssociationSpecification) {
            OneToManyAssociationSpecification oneToManyAssociation = (OneToManyAssociationSpecification) field;
            InternalCollection collection = (InternalCollection) oneToManyAssociation.get(place.getObject());
            
            boolean allFieldsNavigated = true;
            for (int i = 0; i < collection.size(); i++) {
                
                NakedObject referencedObject = (NakedObject) collection.elementAt(i); // cast because haven't (yet) put toXml() on the NO interface, only on ANO.

                allFieldsNavigated = allFieldsNavigated && appendXmlThenIncludeRemaining(fieldElement, referencedObject, fieldNames, annotation);
            }
            return allFieldsNavigated;
        }
        
        return false; // fall through, shouldn't get here but just in case.
    }

    private boolean appendXmlThenIncludeRemaining(Element fieldElement, NakedObject referencedObject, final Vector fieldNames, final String annotation) {

        Element referencedElement = appendXml(namespaceManager, referencedObject, fieldElement);
        Place referencedPlace = new Place(referencedObject, referencedElement);

        return includeField(referencedPlace, fieldNames, annotation);
    }

    public Element toXml() {
        return rootPlace.getElement();
    }

    private static Element addElementIfNotPresent(Element element, Element fieldElement, NamespaceManager namespaceManager) {
        // before we add the field element, check to see if it is already there
        NodeList existingElements = element.getElementsByTagNameNS("*" /* fieldElement.getNamespaceURI() */, fieldElement
                .getLocalName());
        if (existingElements.getLength() == 1) {
            Element possibleMatch = (Element) (Element) existingElements.item(0);
            String fieldElementOid = namespaceManager.getNofAttribute(fieldElement, "oid");
            String possibleMatchOid = namespaceManager.getNofAttribute(possibleMatch, "oid");
            if (fieldElementOid != null && possibleMatchOid != null && fieldElementOid.equals(possibleMatchOid)) {
                return possibleMatch;
            }
        }
        element.appendChild(fieldElement);
        return fieldElement;
    }

    /**
     * Creates an Element representing this object, and appends it to the
     * supplied parentElement, provided that an element for the object is not
     * already appended.
     * 
     * The method uses the OID to determine if an object's element is already
     * present.
     * 
     * The parentElement must have an owner document, and should define the nof
     * namespace. Additionally, the supplied namespaceManager must be populated
     * with any application-level namespaces referenced in the document that the
     * parentElement resides within. (Normally this is achieved simply by using
     * appendXml passing in a rootElement and an empty NamespaceManager - see
     * {@link #toXml()}or {@link SnapshotBuilder}).
     */
    private static Element appendXml(NamespaceManager namespaceManager, final NakedObject object , final Element parentElement) {
        if (parentElement.getOwnerDocument() == null) {
            throw new IllegalArgumentException("parentElement must have owner document");
        }

        NakedObjectSpecification nakedClass = object.getSpecification();

        Document doc = parentElement.getOwnerDocument();

        String qname = namespaceManager.getQname(nakedClass);
        String nsUri = namespaceManager.getUri(nakedClass);
        String nsAlias = namespaceManager.getAlias(nakedClass);

        Element element = doc.createElementNS(nsUri, qname);

        namespaceManager.addNamespace(element, nsAlias, nsUri);
        if(object.getOid() == null) {
            throw new NakedObjectRuntimeException("No object for " + nsAlias);
        }
        
        namespaceManager.setNofAttribute(element, "oid", object.getOid().toString());

        FieldSpecification[] fields = nakedClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];
            String fieldName = field.getName();
            Element fieldElement = doc.createElementNS(nsUri, namespaceManager.getAlias(nakedClass) + ":" + fieldName); // scoped
            // by
            // namespace
            // of
            // class
            // of
            // containing
            // object

            if (field instanceof ValueFieldSpecification) {
                ValueFieldSpecification valueHolder = ((ValueFieldSpecification) field);
                Naked value = valueHolder.get(object);
                if (value != null) {
                    // a null value would be a programming error, but we protect
                    // against it anyway
                    namespaceManager.setNofAttribute(fieldElement, "feature", "value");
                    namespaceManager.setNofAttribute(fieldElement, "datatype", NamespaceManager.NOF_METAMODEL_NS_ALIAS + ":"
                            + value.getSpecification().getShortName());

                    if (value.titleString().length() > 0) {
                        String valueStr = value.titleString();
                        fieldElement.appendChild(doc.createTextNode(valueStr));
                    } else {
                        namespaceManager.setNofAttribute(fieldElement, "isEmpty", "true");
                    }
                    element.appendChild(fieldElement);
                }

            } else if (field instanceof OneToOneAssociationSpecification) {
                OneToOneAssociationSpecification oneToOneAssociation = ((OneToOneAssociationSpecification) field);
                NakedObject referencedNakedObject = (NakedObject) oneToOneAssociation.get(object);
                namespaceManager.setNofAttribute(fieldElement, "feature", "reference");

                String fullyQualifiedClassName = oneToOneAssociation.getType().getFullName();
                namespaceManager.addNamespaceIfRequired(parentElement, fullyQualifiedClassName);
                setNofTypeAttr(fieldElement, namespaceManager, fullyQualifiedClassName);

                if (referencedNakedObject != null) {
                    String titleStr = referencedNakedObject.titleString();

                    Element titleElement = doc.createElementNS(NamespaceManager.NOF_METAMODEL_NS_URI, "nof:title");
                    fieldElement.appendChild(titleElement);
                    titleElement.appendChild(doc.createTextNode(titleStr));
                } else {
                    namespaceManager.setNofAttribute(fieldElement, "isEmpty", "true");
                }

                // before we add the field element, check to see if it is
                // already there
                fieldElement = addElementIfNotPresent(element, fieldElement, namespaceManager);

            } else if (field instanceof OneToManyAssociationSpecification) {
                namespaceManager.setNofAttribute(fieldElement, "feature", "collection");

                OneToManyAssociationSpecification oneToManyAssociation = (OneToManyAssociationSpecification) field;
                InternalCollection collection = (InternalCollection) oneToManyAssociation.get(object);
                String fullyQualifiedClassName = collection.getElementSpecification().getFullName();
                namespaceManager.addNamespaceIfRequired(parentElement, fullyQualifiedClassName);
                setNofTypeAttr(fieldElement, namespaceManager, fullyQualifiedClassName);

                Attr collectionSizeAttr = doc.createAttributeNS(NamespaceManager.NOF_METAMODEL_NS_URI, "nof:size");
                collectionSizeAttr.setValue("" + collection.size());
                fieldElement.setAttributeNode(collectionSizeAttr);

                fieldElement = addElementIfNotPresent(element, fieldElement, namespaceManager);

            }
        }
        element = addElementIfNotPresent(parentElement, element, namespaceManager);
        return element;
    }

    private static Element rootElementFor(final Element element) {
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

    /**
     * Adds an <code>nof:type</code> attribute for the supplied class to the
     * supplied field element.
     */
    private static void setNofTypeAttr(Element fieldElement, final NamespaceManager namespaceManager, String fullyQualifiedClassName) {
        String referencedQname = namespaceManager.getQname(fullyQualifiedClassName);
        namespaceManager.setNofAttribute(fieldElement, "type", referencedQname);
    }

    public Snapshot snapshot(NakedObject object) {
        Snapshot snapshot = new Snapshot();
        snapshot(object, snapshot);
        return snapshot;
    }
    
    private static void snapshot(NakedObject object, Snapshot snapshot) {
        NakedObjectSpecification nc = object.getSpecification();
        StringBuffer str = new StringBuffer();

        FieldSpecification[] fields = nc.getFields();
        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];

            if (field instanceof ValueFieldSpecification) {
                ValueFieldSpecification valueHolder = ((ValueFieldSpecification) field);
                Naked value = valueHolder.get(object);
                if (value != null) {
                    String valueStr = value.titleString();
                    str.append(valueStr);
                }
            } else if (field instanceof OneToOneAssociationSpecification) {
                OneToOneAssociationSpecification oneToOneAssociation = ((OneToOneAssociationSpecification) field);
                Naked referencedNakedObject = oneToOneAssociation.get(object);
                if (referencedNakedObject != null) {
                    String titleStr = referencedNakedObject.titleString();
                    str.append(titleStr);
                }
            } else if (field instanceof OneToManyAssociationSpecification) {
                OneToManyAssociationSpecification oneToManyAssociation = (OneToManyAssociationSpecification) field;
                InternalCollection collection = (InternalCollection) oneToManyAssociation.get(object);

                for (int j = 0; j < collection.size(); j++) {
                    NakedObject element = collection.elementAt(i);
                    if (element != null) {
                        str.append(element.titleString());
                    }
                }
            }

            str.append(",  ");
        }

    }

    /**
     * Returns a DOM Element representing the object.
     * 
     * The element returned will reside in its own document. To create a graph
     * of elements, use instead {@link SnapshotBuilder}.
     * 
     * @see #toXml()
     */
    private static Element toXml(final NakedObject object ) {

        NamespaceManager namespaceManager = new NamespaceManager();

        DocumentBuilderFactory dbf = DocumentBuilderFactoryImpl.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element rootElement = doc.createElementNS(NamespaceManager.NOF_METAMODEL_NS_URI, "nof:snapshot");
            doc.appendChild(rootElement);
            namespaceManager.addXmlnsNamespace(rootElement);

            appendXml(namespaceManager, object, rootElement);
            return rootElement;
        } catch (ParserConfigurationException e) {
            e.printStackTrace(System.err);
            LOG.error("Unable to build snapshot", e);
        }
        return null;
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