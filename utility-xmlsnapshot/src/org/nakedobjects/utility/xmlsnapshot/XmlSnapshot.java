package org.nakedobjects.utility.xmlsnapshot;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
     * TODO: lastactivity is skipped because it is in transition (?) from
     * residing in AbstractNakedObject and BusinessObject.
     * 
     * (The problem is that in .NET the getLastActivity() is not shown as a
     * property but is in the meta-model). Having a deriveLastActivity() caused
     * two fields of the same name to be listed.
     */
    private static final String[] SKIP_FIELDS = new String[] { "lastactivity" };

    private final boolean addOids;
    private final Helper helper;

    private final NofMetaModel nofMeta;

    private final Place rootPlace;

    private final XmlSchema schema;

    /**
     * the suggested location for the schema (xsi:schemaLocation attribute)
     */
    private String schemaLocationFileName;
    private boolean topLevelElementWritten = false;

    private final Document xmlDocument;

    /**
     * root element of {@link #xmlDocument}
     */
    private Element xmlElement;
    private final Document xsdDocument;
    /**
     * root element of {@link #xsdDocument}
     */
    private final Element xsdElement;

    private final XsMetaModel xsMeta;

    /**
     * Start a snapshot at the root object, using own namespace manager.
     */
    public XmlSnapshot(final NakedObject rootObject) {
        this(rootObject, false);
    }

    /**
     * Start a snapshot at the root object, using own namespace manager.
     */
    public XmlSnapshot(final NakedObject rootObject, final boolean addOids) {
        this(rootObject, new XmlSchema(), addOids);
    }

    /**
     * Start a snapshot at the root object, using supplied namespace manager.
     */
    public XmlSnapshot(final NakedObject rootObject, final XmlSchema schema, final boolean addOids) {

        LOG.debug(".ctor(" + log("rootObj", rootObject) + andlog("schema", schema) + andlog("addOids", "" + true) + ")");

        this.addOids = addOids;
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

    private String andlog(final String label, final NakedObject object) {
        return ", " + log(label, object);
    }

    private String andlog(final String label, final Object object) {
        return ", " + log(label, object);
    }

    /**
     * Creates an Element representing this object, and appends it as the root
     * element of the Document.
     * 
     * The Document must not yet have a root element Additionally, the supplied
     * schemaManager must be populated with any application-level namespaces
     * referenced in the document that the parentElement resides within.
     * (Normally this is achieved simply by using appendXml passing in a new
     * schemaManager - see {@link #toXml()}or {@link XmlSnapshot}).
     */
    private Place appendXml(final NakedObject object) {

        LOG.debug("appendXml(" + log("obj", object) + "')");

        String fullyQualifiedClassName = object.getSpecification().getFullName();

        schema.setUri(fullyQualifiedClassName); // derive
        // URI
        // from
        // fully
        // qualified
        // name

        Place place = objectToElement(object);

        Element element = place.getXmlElement();
        Element xsElementElement = place.getXsdElement();

        LOG.debug("appendXml(NO): add as element to XML doc");
        getXmlDocument().appendChild(element);

        LOG.debug("appendXml(NO): add as xs:element to xs:schema of the XSD document");
        getXsdElement().appendChild(xsElementElement);

        LOG.debug("appendXml(NO): set target name in XSD, derived from FQCN of obj");
        schema.setTargetNamespace(getXsdDocument(), fullyQualifiedClassName);

        LOG.debug("appendXml(NO): set schema location file name to XSD, derived from FQCN of obj");
        String schemaLocationFileName = fullyQualifiedClassName + ".xsd";
        schema.assignSchema(getXmlDocument(), fullyQualifiedClassName, schemaLocationFileName);

        LOG.debug("appendXml(NO): copy into snapshot obj");
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
     * present. If the object is not yet persistent, then the hashCode is used
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

        LOG.debug("appendXml(" + log("parentPlace", parentPlace) + andlog("childObj", childObject) + ")");

        Element parentElement = parentPlace.getXmlElement();
        Element parentXsElement = parentPlace.getXsdElement();

        if (parentElement.getOwnerDocument() != getXmlDocument()) {
            throw new IllegalArgumentException("parent XML Element must have snapshot's XML document as its owner");
        }

        LOG.debug("appendXml(Pl, NO): invoking objectToElement() for " + log("childObj", childObject));
        Place childPlace = objectToElement(childObject);
        Element childElement = childPlace.getXmlElement();
        Element childXsElement = childPlace.getXsdElement();

        LOG.debug("appendXml(Pl, NO): invoking mergeTree of parent with child");
        childElement = mergeTree(parentElement, childElement);

        LOG.debug("appendXml(Pl, NO): adding XS Element to schema if required");
        schema.addXsElementIfNotPresent(parentXsElement, childXsElement);

        return childElement;
    }

    private boolean appendXmlThenIncludeRemaining(Place parentPlace, NakedObject referencedObject, final Vector fieldNames,
            final String annotation) {

        LOG.debug("appendXmlThenIncludeRemaining(: " + log("parentPlace", parentPlace)
                + andlog("referencedObj", referencedObject) + andlog("fieldNames", fieldNames) + andlog("annotation", annotation)
                + ")");

        LOG.debug("appendXmlThenIncludeRemaining(..): invoking appendXml(parentPlace, referencedObject)");

        Element referencedElement = appendXml(parentPlace, referencedObject);
        Place referencedPlace = new Place(referencedObject, referencedElement);

               boolean includedField = includeField(referencedPlace, fieldNames, annotation);

        LOG.debug("appendXmlThenIncludeRemaining(..): invoked includeField(referencedPlace, fieldNames)"
                + andlog("returned", "" + includedField));

        return includedField;
    }

    private Vector elementsUnder(final Element parentElement, final String localName) {
        Vector v = new Vector();
        NodeList existingNodes = parentElement.getChildNodes();
        for (int i = 0; i < existingNodes.getLength(); i++) {
            Node node = existingNodes.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            Element element = (Element) node;
            if (localName.equals("*") || element.getLocalName().equals(localName)) {
                v.addElement(element);
            }
        }
        return v;
    }

    public NakedObject getObject() {
        return rootPlace.getObject();
    }

    public XmlSchema getSchema() {
        return getSchema();
    }

    /**
     * The name of the <code>xsi:schemaLocation</code> in the XML document.
     * 
     * Taken from the <code>fullyQualifiedClassName</code> (which also is used
     * as the basis for the <code>targetNamespace</code>.
     * 
     * Populated in {@link #appendXml(NakedObject)}.
     */
    public String getSchemaLocationFileName() {
        return schemaLocationFileName;
    }

    public Document getXmlDocument() {
        return xmlDocument;
    }

    /**
     * The root element of {@link #getXmlDocument()}. Returns <code>null</code>
     * until the snapshot has actually been built.
     */
    public Element getXmlElement() {
        return xmlElement;
    }

    public Document getXsdDocument() {
        return xsdDocument;
    }

    /**
     * The root element of {@link #getXsdDocument()}. Returns <code>null</code>
     * until the snapshot has actually been built.
     */
    public Element getXsdElement() {
        return xsdElement;
    }

    public void include(final String path) {
        include(path, null);
    }

    public void include(final String path, final String annotation) {

        // tokenize into successive fields
        Vector fieldNames = new Vector();
        for (StringTokenizer tok = new StringTokenizer(path, "/"); tok.hasMoreTokens();) {
            String token = tok.nextToken().toLowerCase();

            LOG.debug("include(..): " + log("token", token));
            fieldNames.addElement(token);
        }

        LOG.debug("include(..): " + log("fieldNames", fieldNames));

        // navigate first field, from the root.
        LOG.debug("include(..): invoking includeField");
        includeField(rootPlace, fieldNames, annotation);
    }

    /**
     * @return true if able to navigate the complete vector of field names
     *                 successfully; false if a field could not be located or it turned
     *                 out to be a value.
     */
    private boolean includeField(final Place place, Vector fieldNames, final String annotation) {

        LOG.debug("includeField(: " + log("place", place) + andlog("fieldNames", fieldNames) + andlog("annotation", annotation)
                + ")");

        NakedObject object = place.getObject();
        Element xmlElement = place.getXmlElement();

        // we use a copy of the path so that we can safely traverse collections
        // without side-effects
        Vector fieldNamesOrig = fieldNames;
        fieldNames = new Vector();
        for (java.util.Enumeration enum = fieldNamesOrig.elements(); enum.hasMoreElements();) {
            fieldNames.addElement(enum.nextElement());
        }

        // see if we have any fields to process
        if (fieldNames.size() == 0) {
            return true;
        }

        // take the first field name from the list, and remove
        String fieldName = (String) fieldNames.elementAt(0);
        fieldNames.removeElementAt(0);

        LOG.debug("includeField(Pl, Vec, Str):" + log("processing field", fieldName) + andlog("left", "" + fieldNames.size()));

        // locate the field in the object's class
        NakedObjectSpecification nos = object.getSpecification();
        NakedObjectField field = null;
        try {
            // HACK: really want a NakedObjectSpecification.hasField method to
            // check first.
            field = nos.getField(fieldName);
        } catch (NakedObjectSpecificationException ex) {
            LOG.info("includeField(Pl, Vec, Str): could not locate field, skipping");
            return false;
        }

        // locate the corresponding XML element
        // (the corresponding XSD element will later be attached to xmlElement
        // as its userData)
        LOG.debug("includeField(Pl, Vec, Str): locating corresponding XML element");
        Vector xmlFieldElements = elementsUnder(xmlElement, field.getName());
        if (xmlFieldElements.size() != 1) {
            LOG.info("includeField(Pl, Vec, Str): could not locate " + log("field", field.getName())
                    + andlog("xmlFieldElements.size", "" + xmlFieldElements.size()));
            return false;
        }
        Element xmlFieldElement = (Element) xmlFieldElements.elementAt(0);

        if (fieldNames.size() == 0 && annotation != null) {
            // nothing left in the path, so we will apply the annotation now
            nofMeta.setAnnotationAttribute(xmlFieldElement, annotation);
        }

        Place fieldPlace = new Place(object, xmlFieldElement);

        if (field.isValue()) {
            LOG.debug("includeField(Pl, Vec, Str): field is value; done");
            return false;

        } else if (field instanceof OneToOneAssociation) {
            LOG.debug("includeField(Pl, Vec, Str): field is 1->1");

            OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
            NakedObject referencedObject = fieldPlace.getObject().getAssociation(oneToOneAssociation);

            if (referencedObject == null) {
                return true; // not a failure if the reference was null
            }

            boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, fieldNames, annotation);
            LOG.debug("includeField(Pl, Vec, Str): 1->1: invoked appendXmlThenIncludeRemaining for "
                    + log("referencedObj", referencedObject) + andlog("returned", "" + appendedXml));

            return appendedXml;

        } else if (field instanceof OneToManyAssociation) {

            LOG.debug("includeField(Pl, Vec, Str): field is 1->M");

            OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
            InternalCollection collection = (InternalCollection) fieldPlace.getObject().getField(oneToManyAssociation);

            LOG.debug("includeField(Pl, Vec, Str): 1->M: " + log("collection.size", "" + collection.size()));
            boolean allFieldsNavigated = true;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject referencedObject = (NakedObject) collection.elementAt(i);

                boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, fieldNames, annotation);

                LOG.debug("includeField(Pl, Vec, Str): 1->M: + invoked appendXmlThenIncludeRemaining for "
                        + log("referencedObj", referencedObject) + andlog("returned", "" + appendedXml));

                allFieldsNavigated = allFieldsNavigated && appendedXml;
            }
            LOG.debug("includeField(Pl, Vec, Str): " + log("returning", "" + allFieldsNavigated));

            return allFieldsNavigated;
        }

        return false; // fall through, shouldn't get here but just in
        // case.
    }

    private String log(final String label, final NakedObject object) {
        return log(label, (object == null? "(null)" : object.titleString() + "[" + oidOrHashCode(object) + "]"));
    }

    private String log(final String label, final Object object) {
        return (label  == null ? "?"     : label)+ "='" + (object == null ? "(null)": object.toString()) + "'";
    }

    /**
     * Merges the tree of Elements whose root is <code>childElement</code>
     * underneath the <code>parentElement</code>.
     * 
     * If the <code>parentElement</code> already has an element that matches
     * the <code>childElement</code>, then recursively attaches the
     * grandchildren instead.
     * 
     * The element returned will be either the supplied
     * <code>childElement</code>, or an existing child element if one already
     * existed under <code>parentElement</code>.
     */
    private Element mergeTree(final Element parentElement, final Element childElement) {

        LOG.debug("mergeTree(" + log("parent", parentElement) + andlog("child", childElement));

        String childElementOid = nofMeta.getAttribute(childElement, "oid");

        LOG.debug("mergeTree(El,El): " + log("childOid", childElementOid));
        if (childElementOid != null) {

            // before we add the child element, check to see if it is already
            // there
            LOG.debug("mergeTree(El,El): check if child already there");
            Vector existingChildElements = elementsUnder(parentElement, childElement.getLocalName());
            for (Enumeration childEnum = existingChildElements.elements(); childEnum.hasMoreElements();) {
                Element possibleMatchingElement = (Element) childEnum.nextElement();

                String possibleMatchOid = nofMeta.getAttribute(possibleMatchingElement, "oid");
                if (possibleMatchOid == null || !possibleMatchOid.equals(childElementOid)) {
                    continue;
                }

                LOG.debug("mergeTree(El,El): child already there; merging grandchildren");

                // match: transfer the children of the child (grandchildren) to
                // the
                // already existing matching child
                Element existingChildElement = possibleMatchingElement;
                Vector grandchildrenElements = elementsUnder(childElement, "*");
                for (Enumeration grandchildEnum = grandchildrenElements.elements(); grandchildEnum.hasMoreElements();) {
                    Element grandchildElement = (Element) grandchildEnum.nextElement();
                    childElement.removeChild(grandchildElement);

                    LOG.debug("mergeTree(El,El): merging " + log("grandchild", grandchildElement));

                    mergeTree(existingChildElement, grandchildElement);
                }
                return existingChildElement;
            }
        }

        parentElement.appendChild(childElement);
        return childElement;
    }

    Place objectToElement(final NakedObject object) {

        LOG.debug("objectToElement(" + log("object", object) + ")");

        NakedObjectSpecification nos = object.getSpecification();

        LOG.debug("objectToElement(NO): create element and nof:title");
        Element element = schema.createElement(getXmlDocument(), nos.getShortName(), nos.getFullName(), nos.getSingularName(),
                nos.getPluralName());
        nofMeta.appendNofTitle(element, object.titleString());

        LOG.debug("objectToElement(NO): create XS element for NOF class");
        Element xsElement = schema.createXsElementForNofClass(getXsdDocument(), element, topLevelElementWritten);
        topLevelElementWritten = true; // hack:
        // every
        // element
        // in the
        // XSD
        // schema
        // apart
        // from
        // first
        // needs
        // minimum
        // cardinality
        // setting.

        Place place = new Place(object, element);

        nofMeta.setAttributesForClass(element, oidOrHashCode(object).toString());

        NakedObjectField[] fields = nos.getFields();
        LOG.debug("objectToElement(NO): processing fields");
        eachField: for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            String fieldName = field.getName();

            LOG.debug("objectToElement(NO): " + log("field", fieldName));

            // Skip field if we have seen the name already
            // This is a workaround for getLastActivity(). This method exists
            // in AbstractNakedObject, but is not (at some level) being picked
            // up
            // by the dot-net reflector as a property. On the other hand it does
            // exist as a field in the meta model (NakedObjectSpecification).
            //
            // Now, to re-expose the lastactivity field for .Net, a
            // deriveLastActivity()
            // has been added to BusinessObject. This caused another field of
            // the
            // same name, ultimately breaking the XSD.
            eachPreviousField: for (int j = 0; j < i; j++) {
                if (fieldName.equals(fields[i])) {
                    LOG.debug("objectToElement(NO): " + log("field", fieldName) + " SKIPPED");
                    continue eachField;
                }
            }

            Element xmlFieldElement = getXmlDocument().createElementNS(schema.getUri(), // scoped
                    // by
                    // namespace
                    // of
                    // class
                    // of
                    // containing
                    // object
                    schema.getPrefix() + ":" + fieldName);

            Element xsdFieldElement = null;

            if (field.isValue()) {
                LOG.debug("objectToElement(NO): " + log("field", fieldName) + " is value");

                // skip fields of type XmlValue
                if (field.getSpecification() != null && field.getSpecification().getFullName() != null
                        && field.getSpecification().getFullName().endsWith("XmlValue")) {
                    continue eachField;
                }

                OneToOneAssociation valueFieldSpec = ((OneToOneAssociation) field);
                Naked value = object.getField(valueFieldSpec);
                Element xmlValueElement = xmlFieldElement; // more
                // meaningful
                // locally
                // scoped
                // name

                // a null value would be a programming error, but we protect
                // against it anyway
                if (value == null) {
                    continue;
                }

                // XML
                nofMeta.setAttributesForValue(xmlValueElement, value.getSpecification().getShortName());

                boolean isEmpty = (value.titleString().length() > 0);
                if (isEmpty) {
                    String valueStr = value.titleString();
                    xmlValueElement.appendChild(getXmlDocument().createTextNode(valueStr));
                } else {
                    nofMeta.setIsEmptyAttribute(xmlValueElement, true);
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofValue(xsElement, xmlValueElement);

            } else if (field instanceof OneToOneAssociation) {

                LOG.debug("objectToElement(NO): " + log("field", fieldName) + " is OneToOneAssociation");

                OneToOneAssociation oneToOneAssocSpec = ((OneToOneAssociation) field);
                NakedObject referencedNakedObject = object.getAssociation(oneToOneAssocSpec);
                String fullyQualifiedClassName = oneToOneAssocSpec.getSpecification().getFullName();
                Element xmlReferenceElement = xmlFieldElement; // more
                // meaningful
                // locally
                // scoped
                // name

                // XML
                nofMeta.setAttributesForReference(xmlReferenceElement, schema.getPrefix(), fullyQualifiedClassName);

                if (referencedNakedObject != null) {
                    nofMeta.appendNofTitle(xmlReferenceElement, referencedNakedObject.titleString());
                } else {
                    nofMeta.setIsEmptyAttribute(xmlReferenceElement, true);
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofReference(xsElement, xmlReferenceElement);

            } else if (field instanceof OneToManyAssociation) {

                LOG.debug("objectToElement(NO): " + log("field", fieldName) + " is OneToManyAssociation");

                OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
                InternalCollection collection = (InternalCollection) oneToManyAssociation.get(object);
                String fullyQualifiedClassName = collection.getElementSpecification().getFullName();
                Element xmlCollectionElement = xmlFieldElement; // more
                // meaningful
                // locally
                // scoped
                // name

                // XML
                nofMeta.setNofCollection(xmlCollectionElement, schema.getPrefix(), fullyQualifiedClassName, collection, addOids);

                // XSD
                xsdFieldElement = schema.createXsElementForNofCollection(xsElement, xmlCollectionElement);

            } else {
                LOG.info("objectToElement(NO): " + log("field", fieldName) + " is unknown type; ignored");
                continue;
            }

            if (xsdFieldElement != null) {
                Place.setXsdElement(xmlFieldElement, xsdFieldElement);
            }

            // XML
            LOG.debug("objectToElement(NO): invoking mergeTree for field");
            xmlFieldElement = mergeTree(element, xmlFieldElement);

            // XSD
            if (xsdFieldElement != null) {
                LOG.debug("objectToElement(NO): adding XS element for field to schema");
                schema.addFieldXsElement(xsElement, xsdFieldElement);
            }
        }

        return place;
    }

    private String oidOrHashCode(final NakedObject object) {
        Oid oid = object.getOid();
        if (oid == null) {
            return "" + object.hashCode();
        }
        return oid.toString();
        /*
         * InlineTransferableWriter itw = new InlineTransferableWriter();
         * oid.writeData(itw); itw.close(); return itw.toString();
         */
    }

    /**
     * @param schemaLocationFileName
     *                       The schemaLocationFileName to set.
     */
    private void setSchemaLocationFileName(String schemaLocationFileName) {
        this.schemaLocationFileName = schemaLocationFileName;
    }

    /**
     * @param xmlElement
     *                       The xmlElement to set.
     */
    private void setXmlElement(Element xmlElement) {
        this.xmlElement = xmlElement;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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