/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.core.runtime.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.isis.applib.snapshot.Snapshottable;
import org.apache.isis.applib.snapshot.SnapshottableWithInclusions;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.directly.DirectlyStringableOid;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.PersistenceSession;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Traverses object graph from specified root, so that an XML representation of the graph can be returned.
 *
 * <p>
 * Initially designed to allow snapshots to be easily created.
 * 
 * <p>
 * Typical use:
 * <pre>
 * XmlSnapshot snapshot = new XmlSnapshot(customer); // where customer is a reference to an ANO
 * Element customerAsXml = snapshot.toXml(); // returns customer's fields, titles of simple references, number of items in collections
 * snapshot.include(&quot;placeOfBirth&quot;); // navigates to another object represented by simple reference &quot;placeOfBirth&quot;
 * snapshot.include(&quot;orders/product&quot;); // navigates to all <tt>Order</tt>s of <tt>Customer</tt>, and from them for their <tt>Product</tt>s 
 * </pre>
 * 
 * <p>
 * Alternative fluent use:
 * <pre>
 * XmlSnapshot snapshot = 
 *     XmlSnapshot.create(customer)
 *                .includePath(&quot;placeOfBirth&quot;)
 *                .include(&quot;orders/product&quot;)
 *                .build();
 * Element customerAsXml = snapshot.toXml();
 * </pre>
 */
public final class XmlSnapshot {

    private static final Logger LOG = Logger.getLogger(XmlSnapshot.class);

    public static class Builder {
    	private final Snapshottable snapshottable;
        //private boolean addOids;
        private XmlSchema schema;
        static class PathAndAnnotation {
        	public PathAndAnnotation(String path, String annotation) {
				this.path = path;
				this.annotation = annotation;
			}
			private String path;
        	private String annotation;
        }
        private List<PathAndAnnotation> paths = new ArrayList<PathAndAnnotation>();
    	
    	public Builder(Snapshottable domainObject) {
			this.snapshottable = domainObject;
		}
    	public Builder usingSchema(XmlSchema schema) {
    		this.schema = schema;
    		return this;
    	}
    	public Builder includePath(String path) {
    		return includePathAndAnnotation(path, null);
    	}
    	public Builder includePathAndAnnotation(String path, String annotation) {
    		paths.add(new PathAndAnnotation(path, annotation));
    		return this;
    	}
    	
    	public XmlSnapshot build() {
    		ObjectAdapter adapter = getAdapterManager().adapterFor(snapshottable);
    		XmlSnapshot snapshot = (schema != null) ? 
    				new XmlSnapshot(adapter, schema) : 
    				new XmlSnapshot(adapter);
    		for(PathAndAnnotation paa: paths) {
    			if (paa.annotation != null) {
    				snapshot.include(paa.path, paa.annotation);
    			} else {
    				snapshot.include(paa.path);
    			}
    		}
    		return snapshot;
    	}
    	
    }
    
    public static Builder create(Snapshottable snapshottable) {
    	return new Builder(snapshottable);
    }
    
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
    public XmlSnapshot(final ObjectAdapter rootObject) {
        this(rootObject, new XmlSchema());
    }

    /**
     * Start a snapshot at the root object, using supplied namespace manager.
     */
    public XmlSnapshot(final ObjectAdapter rootObject, final XmlSchema schema) {

        LOG.debug(".ctor(" + log("rootObj", rootObject) + andlog("schema", schema) + andlog("addOids", "" + true) + ")");

        this.nofMeta = new NofMetaModel();
        this.xsMeta = new XsMetaModel();

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

        } catch (final ParserConfigurationException e) {
            LOG.error("unable to build snapshot", e);
            throw new IsisException(e);
        }

		for(String path: getPathsFor(rootObject.getObject())) {
			include(path);
		}

    }

	private List<String> getPathsFor(Object object) {
		if (!(object instanceof SnapshottableWithInclusions)) {
			return Collections.emptyList();
		}
		List<String> paths = 
			((SnapshottableWithInclusions) object).snapshotInclusions();
		if (paths == null) {
			return Collections.emptyList();
		}
		return paths;
	}
	

    private String andlog(final String label, final ObjectAdapter object) {
        return ", " + log(label, object);
    }

    private String andlog(final String label, final Object object) {
        return ", " + log(label, object);
    }

    /**
     * Creates an Element representing this object, and appends it as the root element of the Document.
     * 
     * The Document must not yet have a root element Additionally, the supplied schemaManager must be
     * populated with any application-level namespaces referenced in the document that the parentElement
     * resides within. (Normally this is achieved simply by using appendXml passing in a new schemaManager -
     * see {@link #toXml()}or {@link XmlSnapshot}).
     */
    private Place appendXml(final ObjectAdapter object) {

        LOG.debug("appendXml(" + log("obj", object) + "')");

        final String fullyQualifiedClassName = object.getSpecification().getFullName();

        schema.setUri(fullyQualifiedClassName); // derive
        // URI
        // from
        // fully
        // qualified
        // name

        final Place place = objectToElement(object);

        final Element element = place.getXmlElement();
        final Element xsElementElement = place.getXsdElement();

        LOG.debug("appendXml(NO): add as element to XML doc");
        getXmlDocument().appendChild(element);

        LOG.debug("appendXml(NO): add as xs:element to xs:schema of the XSD document");
        getXsdElement().appendChild(xsElementElement);

        LOG.debug("appendXml(NO): set target name in XSD, derived from FQCN of obj");
        schema.setTargetNamespace(getXsdDocument(), fullyQualifiedClassName);

        LOG.debug("appendXml(NO): set schema location file name to XSD, derived from FQCN of obj");
        final String schemaLocationFileName = fullyQualifiedClassName + ".xsd";
        schema.assignSchema(getXmlDocument(), fullyQualifiedClassName, schemaLocationFileName);

        LOG.debug("appendXml(NO): copy into snapshot obj");
        setXmlElement(element);
        setSchemaLocationFileName(schemaLocationFileName);

        return place;
    }

    /**
     * Creates an Element representing this object, and appends it to the supplied parentElement, provided
     * that an element for the object is not already appended.
     * 
     * The method uses the OID to determine if an object's element is already present. If the object is not
     * yet persistent, then the hashCode is used instead.
     * 
     * The parentElement must have an owner document, and should define the nof namespace. Additionally, the
     * supplied schemaManager must be populated with any application-level namespaces referenced in the
     * document that the parentElement resides within. (Normally this is achieved simply by using appendXml
     * passing in a rootElement and a new schemaManager - see {@link #toXml()}or {@link XmlSnapshot}).
     */
    private Element appendXml(final Place parentPlace, final ObjectAdapter childObject) {

        LOG.debug("appendXml(" + log("parentPlace", parentPlace) + andlog("childObj", childObject) + ")");

        final Element parentElement = parentPlace.getXmlElement();
        final Element parentXsElement = parentPlace.getXsdElement();

        if (parentElement.getOwnerDocument() != getXmlDocument()) {
            throw new IllegalArgumentException("parent XML Element must have snapshot's XML document as its owner");
        }

        LOG.debug("appendXml(Pl, NO): invoking objectToElement() for " + log("childObj", childObject));
        final Place childPlace = objectToElement(childObject);
        Element childElement = childPlace.getXmlElement();
        final Element childXsElement = childPlace.getXsdElement();

        LOG.debug("appendXml(Pl, NO): invoking mergeTree of parent with child");
        childElement = mergeTree(parentElement, childElement);

        LOG.debug("appendXml(Pl, NO): adding XS Element to schema if required");
        schema.addXsElementIfNotPresent(parentXsElement, childXsElement);

        return childElement;
    }

    private boolean appendXmlThenIncludeRemaining(
            final Place parentPlace,
            final ObjectAdapter referencedObject,
            final Vector fieldNames,
            final String annotation) {

        LOG.debug("appendXmlThenIncludeRemaining(: " + log("parentPlace", parentPlace)
                + andlog("referencedObj", referencedObject) + andlog("fieldNames", fieldNames) + andlog("annotation", annotation)
                + ")");

        LOG.debug("appendXmlThenIncludeRemaining(..): invoking appendXml(parentPlace, referencedObject)");

        final Element referencedElement = appendXml(parentPlace, referencedObject);
        final Place referencedPlace = new Place(referencedObject, referencedElement);

        final boolean includedField = includeField(referencedPlace, fieldNames, annotation);

        LOG.debug("appendXmlThenIncludeRemaining(..): invoked includeField(referencedPlace, fieldNames)"
                + andlog("returned", "" + includedField));

        return includedField;
    }

    private Vector elementsUnder(final Element parentElement, final String localName) {
        final Vector v = new Vector();
        final NodeList existingNodes = parentElement.getChildNodes();
        for (int i = 0; i < existingNodes.getLength(); i++) {
            final Node node = existingNodes.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            final Element element = (Element) node;
            if (localName.equals("*") || element.getLocalName().equals(localName)) {
                v.addElement(element);
            }
        }
        return v;
    }

    public ObjectAdapter getObject() {
        return rootPlace.getObject();
    }

    public XmlSchema getSchema() {
        return schema;
    }

    /**
     * The name of the <code>xsi:schemaLocation</code> in the XML document.
     * 
     * Taken from the <code>fullyQualifiedClassName</code> (which also is used as the basis for the
     * <code>targetNamespace</code>.
     * 
     * Populated in {@link #appendXml(ObjectAdapter)}.
     */
    public String getSchemaLocationFileName() {
        return schemaLocationFileName;
    }

    public Document getXmlDocument() {
        return xmlDocument;
    }

    /**
     * The root element of {@link #getXmlDocument()}. Returns <code>null</code> until the snapshot has
     * actually been built.
     */
    public Element getXmlElement() {
        return xmlElement;
    }

    public Document getXsdDocument() {
        return xsdDocument;
    }

    /**
     * The root element of {@link #getXsdDocument()}. Returns <code>null</code> until the snapshot has
     * actually been built.
     */
    public Element getXsdElement() {
        return xsdElement;
    }

    public void include(final String path) {
        include(path, null);
    }

    public void include(final String path, final String annotation) {

        // tokenize into successive fields
        final Vector fieldNames = new Vector();
        for (final StringTokenizer tok = new StringTokenizer(path, "/"); tok.hasMoreTokens();) {
            final String token = tok.nextToken();

            LOG.debug("include(..): " + log("token", token));
            fieldNames.addElement(token);
        }

        LOG.debug("include(..): " + log("fieldNames", fieldNames));

        // navigate first field, from the root.
        LOG.debug("include(..): invoking includeField");
        includeField(rootPlace, fieldNames, annotation);
    }

    /**
     * @return true if able to navigate the complete vector of field names successfully; false if a field
     *         could not be located or it turned out to be a value.
     */
    private boolean includeField(final Place place, final Vector fieldNames, final String annotation) {

        LOG.debug("includeField(: " + log("place", place) + andlog("fieldNames", fieldNames) + andlog("annotation", annotation)
                + ")");

        final ObjectAdapter object = place.getObject();
        final Element xmlElement = place.getXmlElement();

        // we use a copy of the path so that we can safely traverse collections
        // without side-effects
        final Vector originalNames = fieldNames;
        final Vector names = new Vector();
        for (final java.util.Enumeration e = originalNames.elements(); e.hasMoreElements();) {
            names.addElement(e.nextElement());
        }

        // see if we have any fields to process
        if (names.size() == 0) {
            return true;
        }

        // take the first field name from the list, and remove
        final String fieldName = (String) names.elementAt(0);
        names.removeElementAt(0);

        LOG.debug("includeField(Pl, Vec, Str):" + log("processing field", fieldName) + andlog("left", "" + names.size()));

        // locate the field in the object's class
        final ObjectSpecification nos = object.getSpecification();
        ObjectAssociation field = null;
        try {
            // HACK: really want a ObjectSpecification.hasField method to
            // check first.
            field = nos.getAssociation(fieldName);
        } catch (final ObjectSpecificationException ex) {
            LOG.info("includeField(Pl, Vec, Str): could not locate field, skipping");
            return false;
        }

        // locate the corresponding XML element
        // (the corresponding XSD element will later be attached to xmlElement
        // as its userData)
        LOG.debug("includeField(Pl, Vec, Str): locating corresponding XML element");
        final Vector xmlFieldElements = elementsUnder(xmlElement, field.getId());
        if (xmlFieldElements.size() != 1) {
            LOG.info("includeField(Pl, Vec, Str): could not locate " + log("field", field.getId())
                    + andlog("xmlFieldElements.size", "" + xmlFieldElements.size()));
            return false;
        }
        final Element xmlFieldElement = (Element) xmlFieldElements.elementAt(0);

        if (names.size() == 0 && annotation != null) {
            // nothing left in the path, so we will apply the annotation now
            nofMeta.setAnnotationAttribute(xmlFieldElement, annotation);
        }

        final Place fieldPlace = new Place(object, xmlFieldElement);

        if (field instanceof OneToOneAssociation) {
            if (field.getSpecification().getAssociations().length == 0) {
                LOG.debug("includeField(Pl, Vec, Str): field is value; done");
                return false;
            }

            LOG.debug("includeField(Pl, Vec, Str): field is 1->1");

            final OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
            final ObjectAdapter referencedObject = oneToOneAssociation.get(fieldPlace.getObject());

            if (referencedObject == null) {
                return true; // not a failure if the reference was null
            }

            final boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, names,
                    annotation);
            LOG.debug("includeField(Pl, Vec, Str): 1->1: invoked appendXmlThenIncludeRemaining for "
                    + log("referencedObj", referencedObject) + andlog("returned", "" + appendedXml));

            return appendedXml;

        } else if (field instanceof OneToManyAssociation) {
            LOG.debug("includeField(Pl, Vec, Str): field is 1->M");

            final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
            final ObjectAdapter collection = oneToManyAssociation.get(fieldPlace.getObject());
            final CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);

            LOG.debug("includeField(Pl, Vec, Str): 1->M: " + log("collection.size", "" + facet.size(collection)));
            boolean allFieldsNavigated = true;
            final Enumeration elements = facet.elements(collection);
            while (elements.hasMoreElements()) {
                final ObjectAdapter referencedObject = (ObjectAdapter) elements.nextElement();
                final boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, names, annotation);
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

    private String log(final String label, final ObjectAdapter object) {
        return log(label, (object == null ? "(null)" : object.titleString() + "[" + oidOrHashCode(object) + "]"));
    }

    private String log(final String label, final Object object) {
        return (label == null ? "?" : label) + "='" + (object == null ? "(null)" : object.toString()) + "'";
    }

    /**
     * Merges the tree of Elements whose root is <code>childElement</code> underneath the
     * <code>parentElement</code>.
     * 
     * If the <code>parentElement</code> already has an element that matches the <code>childElement</code>,
     * then recursively attaches the grandchildren instead.
     * 
     * The element returned will be either the supplied <code>childElement</code>, or an existing child
     * element if one already existed under <code>parentElement</code>.
     */
    private Element mergeTree(final Element parentElement, final Element childElement) {

        LOG.debug("mergeTree(" + log("parent", parentElement) + andlog("child", childElement));

        final String childElementOid = nofMeta.getAttribute(childElement, "oid");

        LOG.debug("mergeTree(El,El): " + log("childOid", childElementOid));
        if (childElementOid != null) {

            // before we add the child element, check to see if it is already
            // there
            LOG.debug("mergeTree(El,El): check if child already there");
            final Vector existingChildElements = elementsUnder(parentElement, childElement.getLocalName());
            for (final Enumeration childEnum = existingChildElements.elements(); childEnum.hasMoreElements();) {
                final Element possibleMatchingElement = (Element) childEnum.nextElement();

                final String possibleMatchOid = nofMeta.getAttribute(possibleMatchingElement, "oid");
                if (possibleMatchOid == null || !possibleMatchOid.equals(childElementOid)) {
                    continue;
                }

                LOG.debug("mergeTree(El,El): child already there; merging grandchildren");

                // match: transfer the children of the child (grandchildren) to
                // the
                // already existing matching child
                final Element existingChildElement = possibleMatchingElement;
                final Vector grandchildrenElements = elementsUnder(childElement, "*");
                for (final Enumeration grandchildEnum = grandchildrenElements.elements(); grandchildEnum.hasMoreElements();) {
                    final Element grandchildElement = (Element) grandchildEnum.nextElement();
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

    Place objectToElement(final ObjectAdapter object) {

        LOG.debug("objectToElement(" + log("object", object) + ")");

        final ObjectSpecification nos = object.getSpecification();

        LOG.debug("objectToElement(NO): create element and nof:title");
        final Element element = schema.createElement(getXmlDocument(), nos.getShortName(), nos.getFullName(), nos
                .getSingularName(), nos.getPluralName());
        nofMeta.appendNofTitle(element, object.titleString());

        LOG.debug("objectToElement(NO): create XS element for NOF class");
        final Element xsElement = schema.createXsElementForNofClass(getXsdDocument(), element, topLevelElementWritten, FacetUtil
                .getFacetsByType(nos));

        // hack: every element in the XSD schema apart from first needs minimum cardinality setting.
        topLevelElementWritten = true;

        final Place place = new Place(object, element);

        nofMeta.setAttributesForClass(element, oidOrHashCode(object).toString());

        final ObjectAssociation[] fields = nos.getAssociations();
        LOG.debug("objectToElement(NO): processing fields");
        eachField: for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation field = fields[i];
            final String fieldName = field.getId();

            LOG.debug("objectToElement(NO): " + log("field", fieldName));

            // Skip field if we have seen the name already
            // This is a workaround for getLastActivity(). This method exists
            // in AbstractObjectAdapter, but is not (at some level) being picked
            // up
            // by the dot-net reflector as a property. On the other hand it does
            // exist as a field in the meta model (ObjectSpecification).
            //
            // Now, to re-expose the lastactivity field for .Net, a
            // deriveLastActivity()
            // has been added to BusinessObject. This caused another field of
            // the
            // same name, ultimately breaking the XSD.
            for (int j = 0; j < i; j++) {
                if (fieldName.equals(fields[i].getName())) {
                    LOG.debug("objectToElement(NO): " + log("field", fieldName) + " SKIPPED");
                    continue eachField;
                }
            }

            Element xmlFieldElement = getXmlDocument().createElementNS(schema.getUri(), // scoped by namespace
                    // of class of
                    // containing object
                    schema.getPrefix() + ":" + fieldName);

            Element xsdFieldElement = null;

            if (field.getSpecification().containsFacet(ValueFacet.class)) {
                LOG.debug("objectToElement(NO): " + log("field", fieldName) + " is value");

                final ObjectSpecification fieldNos = field.getSpecification();
                // skip fields of type XmlValue
                if (fieldNos == null) {
                    continue eachField;
                }
                if (fieldNos.getFullName() != null && fieldNos.getFullName().endsWith("XmlValue")) {
                    continue eachField;
                }

                final OneToOneAssociation valueAssociation = ((OneToOneAssociation) field);
                final Element xmlValueElement = xmlFieldElement; // more meaningful locally scoped name

                ObjectAdapter value;
                try {
                    value = valueAssociation.get(object);

                    final ObjectSpecification valueNos = value.getSpecification();

                    // XML
                    nofMeta.setAttributesForValue(xmlValueElement, valueNos.getShortName());

                    // return parsed string, else encoded string, else title.
                    String valueStr;
                    ParseableFacet parseableFacet = fieldNos.getFacet(ParseableFacet.class);
                    EncodableFacet encodeableFacet = fieldNos.getFacet(EncodableFacet.class);
                    if(parseableFacet != null) {
                    	valueStr = parseableFacet.parseableTitle(value);
                    } else if(encodeableFacet != null) {
                    	valueStr = encodeableFacet.toEncodedString(value);
                    } else {
                    	valueStr = value.titleString();
                    }
                    
					final boolean notEmpty = (valueStr.length() > 0);
                    if (notEmpty) {
                        xmlValueElement.appendChild(getXmlDocument().createTextNode(valueStr));
                    } else {
                        nofMeta.setIsEmptyAttribute(xmlValueElement, true);
                    }

                } catch (final Exception ex) {
                    LOG.warn("objectToElement(NO): " + log("field", fieldName)
                            + ": getField() threw exception - skipping XML generation");
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofValue(xsElement, xmlValueElement, FacetUtil
                        .getFacetsByType(valueAssociation));

            } else if (field instanceof OneToOneAssociation) {

                LOG.debug("objectToElement(NO): " + log("field", fieldName) + " is OneToOneAssociation");

                final OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
                final String fullyQualifiedClassName = nos.getFullName();
                final Element xmlReferenceElement = xmlFieldElement; // more meaningful locally scoped name

                ObjectAdapter referencedObjectAdapter;

                try {
                    referencedObjectAdapter = oneToOneAssociation.get(object);

                    // XML
                    nofMeta.setAttributesForReference(xmlReferenceElement, schema.getPrefix(), fullyQualifiedClassName);

                    if (referencedObjectAdapter != null) {
                        nofMeta.appendNofTitle(xmlReferenceElement, referencedObjectAdapter.titleString());
                    } else {
                        nofMeta.setIsEmptyAttribute(xmlReferenceElement, true);
                    }

                } catch (final Exception ex) {
                    LOG.warn("objectToElement(NO): " + log("field", fieldName)
                            + ": getAssociation() threw exception - skipping XML generation");
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofReference(xsElement, xmlReferenceElement, oneToOneAssociation
                        .getSpecification().getFullName(), FacetUtil.getFacetsByType(oneToOneAssociation));

            } else if (field instanceof OneToManyAssociation) {

                LOG.debug("objectToElement(NO): " + log("field", fieldName) + " is OneToManyAssociation");

                final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
                final Element xmlCollectionElement = xmlFieldElement; // more meaningful locally scoped name

                ObjectAdapter collection;
                try {
                    collection = oneToManyAssociation.get(object);
                    final ObjectSpecification referencedTypeNos = oneToManyAssociation.getSpecification();
                    final String fullyQualifiedClassName = referencedTypeNos.getFullName();

                    // XML
                    nofMeta.setNofCollection(xmlCollectionElement, schema.getPrefix(), fullyQualifiedClassName, collection);
                } catch (final Exception ex) {
                    LOG.warn("objectToElement(NO): " + log("field", fieldName)
                            + ": get(obj) threw exception - skipping XML generation");
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofCollection(xsElement, xmlCollectionElement, oneToManyAssociation
                        .getSpecification().getFullName(), FacetUtil.getFacetsByType(oneToManyAssociation));

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

    private String oidOrHashCode(final ObjectAdapter object) {
        final Oid oid = object.getOid();
        /*
         * if (oid == null) { return "" + object.hashCode(); }
         */
        if (oid instanceof DirectlyStringableOid) {
			DirectlyStringableOid directlyStringableOid = (DirectlyStringableOid) oid;
        	return directlyStringableOid.enString();
        } else {
        	return oid.toString();
        }
        /*
         * InlineTransferableWriter itw = new InlineTransferableWriter(); oid.writeData(itw); itw.close();
         * return itw.toString();
         */
    }

    /**
     * @param schemaLocationFileName
     *            The schemaLocationFileName to set.
     */
    private void setSchemaLocationFileName(final String schemaLocationFileName) {
        this.schemaLocationFileName = schemaLocationFileName;
    }

    /**
     * @param xmlElement
     *            The xmlElement to set.
     */
    private void setXmlElement(final Element xmlElement) {
        this.xmlElement = xmlElement;
    }

    

	/////////////////////////////////////////////////////////
	// Dependencies (from context)
	/////////////////////////////////////////////////////////
	
	private static AdapterManager getAdapterManager() {
		return getPersistenceSession().getAdapterManager();
	}
	
	private static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}

}
