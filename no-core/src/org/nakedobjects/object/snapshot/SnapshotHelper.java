package org.nakedobjects.object.snapshot;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.value.Snapshot;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.crimson.jaxp.DocumentBuilderFactoryImpl;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class SnapshotHelper extends AbstractNakedObject {
    private static final Logger LOG = Logger.getLogger(SnapshotHelper.class);

    private Element addElementIfNotPresent(Element element, Element fieldElement, NamespaceManager namespaceManager) {
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
    Element appendXml(NamespaceManager namespaceManager, final Element parentElement) {
        if (parentElement.getOwnerDocument() == null) {
            throw new IllegalArgumentException("parentElement must have owner document");
        }

        final NakedObject object = this;
        NakedClass nakedClass = object.getNakedClass();

        Document doc = parentElement.getOwnerDocument();

        String qname = namespaceManager.getQname(nakedClass);
        String nsUri = namespaceManager.getUri(nakedClass);
        String nsAlias = namespaceManager.getAlias(nakedClass);

        Element element = doc.createElementNS(nsUri, qname);

        namespaceManager.addNamespace(element, nsAlias, nsUri);
        namespaceManager.setNofAttribute(element, "oid", getOid().toString());

        Field[] fields = nakedClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String fieldName = field.getName();
            Element fieldElement = doc.createElementNS(nsUri, namespaceManager.getAlias(nakedClass) + ":" + fieldName); // scoped
            // by
            // namespace
            // of
            // class
            // of
            // containing
            // object

            if (field instanceof Value) {
                Value valueHolder = ((Value) field);
                Naked value = valueHolder.get(object);
                if (value != null) {
                    // a null value would be a programming error, but we protect
                    // against it anyway
                    namespaceManager.setNofAttribute(fieldElement, "feature", "value");
                    namespaceManager.setNofAttribute(fieldElement, "datatype", NamespaceManager.NOF_METAMODEL_NS_ALIAS + ":"
                            + value.getShortClassName());

                    if (!value.isEmpty()) {
                        String valueStr = value.title().toString();
                        fieldElement.appendChild(doc.createTextNode(valueStr));
                    } else {
                        namespaceManager.setNofAttribute(fieldElement, "isEmpty", "true");
                    }
                    element.appendChild(fieldElement);
                }

            } else if (field instanceof OneToOneAssociation) {
                OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
                NakedObject referencedNakedObject = (NakedObject) oneToOneAssociation.get(object);
                namespaceManager.setNofAttribute(fieldElement, "feature", "reference");

                String fullyQualifiedClassName = oneToOneAssociation.getType().getName();
                namespaceManager.addNamespaceIfRequired(parentElement, fullyQualifiedClassName);
                setNofTypeAttr(fieldElement, namespaceManager, fullyQualifiedClassName);

                if (referencedNakedObject != null) {
                    String titleStr = referencedNakedObject.title().toString();

                    Element titleElement = doc.createElementNS(NamespaceManager.NOF_METAMODEL_NS_URI, "nof:title");
                    fieldElement.appendChild(titleElement);
                    titleElement.appendChild(doc.createTextNode(titleStr));
                } else {
                    namespaceManager.setNofAttribute(fieldElement, "isEmpty", "true");
                }

                // before we add the field element, check to see if it is
                // already there
                fieldElement = addElementIfNotPresent(element, fieldElement, namespaceManager);

            } else if (field instanceof OneToManyAssociation) {
                namespaceManager.setNofAttribute(fieldElement, "feature", "collection");

                OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
                InternalCollection collection = (InternalCollection) oneToManyAssociation.get(object);
                String fullyQualifiedClassName = collection.getType().getName();
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

    private Element rootElementFor(final Element element) {
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
    private void setNofTypeAttr(Element fieldElement, final NamespaceManager namespaceManager, String fullyQualifiedClassName) {
        String referencedQname = namespaceManager.getQname(fullyQualifiedClassName);
        namespaceManager.setNofAttribute(fieldElement, "type", referencedQname);
    }

    public Snapshot snapshot() {
        Snapshot snapshot = new Snapshot();
        snapshot(this, snapshot);
        return snapshot;
    }

    private void snapshot(NakedObject object, Snapshot snapshot) {
        NakedClass nc = object.getNakedClass();
        StringBuffer str = new StringBuffer();

        Field[] fields = nc.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            if (field instanceof Value) {
                Value valueHolder = ((Value) field);
                Naked value = valueHolder.get(object);
                if (value != null) {
                    String valueStr = value.title().toString();
                    str.append(valueStr);
                }
            } else if (field instanceof OneToOneAssociation) {
                OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
                Naked referencedNakedObject = oneToOneAssociation.get(object);
                if (referencedNakedObject != null) {
                    String titleStr = referencedNakedObject.title().toString();
                    str.append(titleStr);
                }
            } else if (field instanceof OneToManyAssociation) {
                OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
                InternalCollection collection = (InternalCollection) oneToManyAssociation.get(object);

                for (int j = 0; j < collection.size(); j++) {
                    NakedObject element = collection.elementAt(i);
                    if (element != null) {
                        str.append(element.title().toString());
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
    public Element toXml() {

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

            appendXml(namespaceManager, rootElement);
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