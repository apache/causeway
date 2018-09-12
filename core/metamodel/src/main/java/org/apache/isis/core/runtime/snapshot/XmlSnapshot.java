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

import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService.Snapshot;
import org.apache.isis.applib.snapshot.SnapshottableWithInclusions;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Traverses object graph from specified root, so that an XML representation of
 * the graph can be returned.
 *
 * <p>
 * Initially designed to allow snapshots to be easily created.
 *
 * <p>
 * Typical use:
 *
 * <pre>
 * XmlSnapshot snapshot = new XmlSnapshot(customer); // where customer is a
 * // reference to an
 * // ObjectAdapter
 * Element customerAsXml = snapshot.toXml(); // returns customer's fields, titles
 * // of simple references, number of
 * // items in collections
 * snapshot.include(&quot;placeOfBirth&quot;); // navigates to another object represented by
 * // simple reference &quot;placeOfBirth&quot;
 * snapshot.include(&quot;orders/product&quot;); // navigates to all &lt;tt&gt;Order&lt;/tt&gt;s of
 * // &lt;tt&gt;Customer&lt;/tt&gt;, and from them for
 * // their &lt;tt&gt;Product&lt;/tt&gt;s
 * </pre>
 */
public class XmlSnapshot implements Snapshot {

    private static final Logger LOG = LoggerFactory.getLogger(XmlSnapshot.class);

    private final IsisSchema isisMetaModel;

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
     *
     */
    public XmlSnapshot(final ObjectAdapter rootAdapter) {
        this(rootAdapter, new XmlSchema());
    }

    /**
     * Start a snapshot at the root object, using supplied namespace manager.
     */
    public XmlSnapshot(final ObjectAdapter rootAdapter, final XmlSchema schema) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(".ctor({}{}{})", log("rootObj", rootAdapter), andlog("schema", schema), andlog("addOids", "" + true));
        }

        this.isisMetaModel = new IsisSchema();
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

            this.rootPlace = appendXml(rootAdapter);

        } catch (final ParserConfigurationException e) {
            LOG.error("unable to build snapshot", e);
            throw new IsisException(e);
        }

        for (final String path : getPathsFor(rootAdapter.getObject())) {
            include(path);
        }

    }

    private List<String> getPathsFor(final Object object) {
        if (!(object instanceof SnapshottableWithInclusions)) {
            return Collections.emptyList();
        }
        final List<String> paths = ((SnapshottableWithInclusions) object).snapshotInclusions();
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
     * Creates an Element representing this object, and appends it as the root
     * element of the Document.
     *
     * The Document must not yet have a root element Additionally, the supplied
     * schemaManager must be populated with any application-level namespaces
     * referenced in the document that the parentElement resides within.
     * (Normally this is achieved simply by using appendXml passing in a new
     * schemaManager - see {@link XmlSnapshot}).
     */
    private Place appendXml(final ObjectAdapter object) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml({})", log("obj", object));
        }

        final String fullyQualifiedClassName = object.getSpecification().getFullIdentifier();

        schema.setUri(fullyQualifiedClassName); // derive
        // URI
        // from
        // fully
        // qualified
        // name

        final Place place = objectToElement(object);

        final Element element = place.getXmlElement();
        final Element xsElementElement = place.getXsdElement();

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(NO): add as element to XML doc");
        }
        getXmlDocument().appendChild(element);

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(NO): add as xs:element to xs:schema of the XSD document");
        }
        getXsdElement().appendChild(xsElementElement);

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(NO): set target name in XSD, derived from FQCN of obj");
        }
        schema.setTargetNamespace(getXsdDocument(), fullyQualifiedClassName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(NO): set schema location file name to XSD, derived from FQCN of obj");
        }
        final String schemaLocationFileName = fullyQualifiedClassName + ".xsd";
        schema.assignSchema(getXmlDocument(), fullyQualifiedClassName, schemaLocationFileName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(NO): copy into snapshot obj");
        }
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
     * The parentElement must have an owner document, and should define the &quot;isis&quot;
     * namespace. Additionally, the supplied schemaManager must be populated
     * with any application-level namespaces referenced in the document that the
     * parentElement resides within. (Normally this is achieved simply by using
     * appendXml passing in a rootElement and a new schemaManager - see
     * {@link XmlSnapshot}).
     */
    private Element appendXml(final Place parentPlace, final ObjectAdapter childObject) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml({}{})", log("parentPlace", parentPlace), andlog("childObj", childObject));
        }

        final Element parentElement = parentPlace.getXmlElement();
        final Element parentXsElement = parentPlace.getXsdElement();

        if (parentElement.getOwnerDocument() != getXmlDocument()) {
            throw new IllegalArgumentException("parent XML Element must have snapshot's XML document as its owner");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(Pl, NO): invoking objectToElement() for {}", log("childObj", childObject));
        }
        final Place childPlace = objectToElement(childObject);
        Element childElement = childPlace.getXmlElement();
        final Element childXsElement = childPlace.getXsdElement();

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(Pl, NO): invoking mergeTree of parent with child");
        }
        childElement = mergeTree(parentElement, childElement);

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXml(Pl, NO): adding XS Element to schema if required");
        }
        schema.addXsElementIfNotPresent(parentXsElement, childXsElement);

        return childElement;
    }

    private boolean appendXmlThenIncludeRemaining(final Place parentPlace, final ObjectAdapter referencedObject, final Vector fieldNames, final String annotation) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXmlThenIncludeRemaining(: {}{}{}{})", log("parentPlace", parentPlace), andlog("referencedObj", referencedObject), andlog("fieldNames", fieldNames), andlog("annotation", annotation));
            LOG.debug("appendXmlThenIncludeRemaining(..): invoking appendXml(parentPlace, referencedObject)");
        }

        final Element referencedElement = appendXml(parentPlace, referencedObject);
        final Place referencedPlace = new Place(referencedObject, referencedElement);

        final boolean includedField = includeField(referencedPlace, fieldNames, annotation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("appendXmlThenIncludeRemaining(..): invoked includeField(referencedPlace, fieldNames){}", andlog("returned", "" + includedField));
        }

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
     * Taken from the <code>fullyQualifiedClassName</code> (which also is used
     * as the basis for the <code>targetNamespace</code>.
     *
     * Populated in {@link #appendXml(ObjectAdapter)}.
     */
    public String getSchemaLocationFileName() {
        return schemaLocationFileName;
    }

    @Override
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

    @Override
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
        final Vector fieldNames = new Vector();
        for (final StringTokenizer tok = new StringTokenizer(path, "/"); tok.hasMoreTokens();) {
            final String token = tok.nextToken();

            if (LOG.isDebugEnabled()) {
                LOG.debug("include(..): {}", log("token", token));
            }
            fieldNames.addElement(token);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("include(..): {}", log("fieldNames", fieldNames));
        }

        // navigate first field, from the root.
        if (LOG.isDebugEnabled()) {
            LOG.debug("include(..): invoking includeField");
        }
        includeField(rootPlace, fieldNames, annotation);
    }

    /**
     * @return true if able to navigate the complete vector of field names
     *         successfully; false if a field could not be located or it turned
     *         out to be a value.
     */
    private boolean includeField(final Place place, final Vector fieldNames, final String annotation) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("includeField(: {}{}{})", log("place", place), andlog("fieldNames", fieldNames),andlog("annotation", annotation));
        }

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

        if (LOG.isDebugEnabled()) {
            LOG.debug("includeField(Pl, Vec, Str):{}{}", log("processing field", fieldName), andlog("left", "" + names.size()));
        }

        // locate the field in the object's class
        final ObjectSpecification nos = object.getSpecification();
        ObjectAssociation field = null;
        try {
            // HACK: really want a ObjectSpecification.hasField method to
            // check first.
            field = nos.getAssociation(fieldName);
        } catch (final ObjectSpecificationException ex) {
            if (LOG.isInfoEnabled()) {
                LOG.info("includeField(Pl, Vec, Str): could not locate field, skipping");
            }
            return false;
        }

        // locate the corresponding XML element
        // (the corresponding XSD element will later be attached to xmlElement
        // as its userData)
        if (LOG.isDebugEnabled()) {
            LOG.debug("includeField(Pl, Vec, Str): locating corresponding XML element");
        }
        final Vector xmlFieldElements = elementsUnder(xmlElement, field.getId());
        if (xmlFieldElements.size() != 1) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                        "includeField(Pl, Vec, Str): could not locate {}",
                        log("field", field.getId()) +
                        andlog("xmlFieldElements.size", "" + xmlFieldElements.size()));
            }
            return false;
        }
        final Element xmlFieldElement = (Element) xmlFieldElements.elementAt(0);

        if (names.size() == 0 && annotation != null) {
            // nothing left in the path, so we will apply the annotation now
            isisMetaModel.setAnnotationAttribute(xmlFieldElement, annotation);
        }

        final Place fieldPlace = new Place(object, xmlFieldElement);

        if (field instanceof OneToOneAssociation) {
            
            if (field.getSpecification().streamAssociations(Contributed.INCLUDED).limit(1).count() == 0L) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("includeField(Pl, Vec, Str): field is value; done");
                }
                return false;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("includeField(Pl, Vec, Str): field is 1->1");
            }

            final OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
            final ObjectAdapter referencedObject = oneToOneAssociation.get(fieldPlace.getObject(),
                    InteractionInitiatedBy.FRAMEWORK);

            if (referencedObject == null) {
                return true; // not a failure if the reference was null
            }

            final boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, names, annotation);
            if (LOG.isDebugEnabled()) {
                LOG.debug("includeField(Pl, Vec, Str): 1->1: invoked appendXmlThenIncludeRemaining for {}{}", log("referencedObj", referencedObject), andlog("returned", "" + appendedXml));
            }

            return appendedXml;

        } else if (field instanceof OneToManyAssociation) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("includeField(Pl, Vec, Str): field is 1->M");
            }

            final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
            final ObjectAdapter collection = oneToManyAssociation.get(fieldPlace.getObject(), InteractionInitiatedBy.FRAMEWORK);
            final CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);

            if (LOG.isDebugEnabled()) {
                LOG.debug("includeField(Pl, Vec, Str): 1->M: {}", log("collection.size", "" + facet.size(collection)));
            }
            boolean allFieldsNavigated = true;
            for (final ObjectAdapter referencedObject : facet.iterable(collection)) {
                final boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, names, annotation);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("includeField(Pl, Vec, Str): 1->M: + invoked appendXmlThenIncludeRemaining for {}{}",log("referencedObj", referencedObject), andlog("returned", "" + appendedXml));
                }
                allFieldsNavigated = allFieldsNavigated && appendedXml;
            }
            LOG.debug("includeField(Pl, Vec, Str): {}", log("returning", "" + allFieldsNavigated));
            return allFieldsNavigated;
        }

        return false; // fall through, shouldn't get here but just in
        // case.
    }

    private String log(final String label, final ObjectAdapter adapter) {
        return log(label, (adapter == null ? "(null)" : adapter.titleString() + "[" + oidAsString(adapter) + "]"));
    }

    private String log(final String label, final Object pojo) {
        return (label == null ? "?" : label) + "='" + (pojo == null ? "(null)" : pojo.toString()) + "'";
    }

    /**
     * Merges the tree of Elements whose root is <code>childElement</code>
     * underneath the <code>parentElement</code>.
     *
     * If the <code>parentElement</code> already has an element that matches the
     * <code>childElement</code>, then recursively attaches the grandchildren
     * instead.
     *
     * The element returned will be either the supplied
     * <code>childElement</code>, or an existing child element if one already
     * existed under <code>parentElement</code>.
     */
    private Element mergeTree(final Element parentElement, final Element childElement) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeTree({}{})", log("parent", parentElement), andlog("child", childElement));
        }

        final String childElementOid = isisMetaModel.getAttribute(childElement, "oid");

        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeTree(El,El): {}", log("childOid", childElementOid));
        }
        if (childElementOid != null) {

            // before we add the child element, check to see if it is already
            // there
            if (LOG.isDebugEnabled()) {
                LOG.debug("mergeTree(El,El): check if child already there");
            }
            final Vector existingChildElements = elementsUnder(parentElement, childElement.getLocalName());
            for (final Enumeration childEnum = existingChildElements.elements(); childEnum.hasMoreElements();) {
                final Element possibleMatchingElement = (Element) childEnum.nextElement();

                final String possibleMatchOid = isisMetaModel.getAttribute(possibleMatchingElement, "oid");
                if (possibleMatchOid == null || !possibleMatchOid.equals(childElementOid)) {
                    continue;
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("mergeTree(El,El): child already there; merging grandchildren");
                }

                // match: transfer the children of the child (grandchildren) to
                // the
                // already existing matching child
                final Element existingChildElement = possibleMatchingElement;
                final Vector grandchildrenElements = elementsUnder(childElement, "*");
                for (final Enumeration grandchildEnum = grandchildrenElements.elements(); grandchildEnum.hasMoreElements();) {
                    final Element grandchildElement = (Element) grandchildEnum.nextElement();
                    childElement.removeChild(grandchildElement);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("mergeTree(El,El): merging {}", log("grandchild", grandchildElement));
                    }

                    mergeTree(existingChildElement, grandchildElement);
                }
                return existingChildElement;
            }
        }

        parentElement.appendChild(childElement);
        return childElement;
    }

    Place objectToElement(final ObjectAdapter adapter) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("objectToElement({})", log("object", adapter));
        }

        final ObjectSpecification nos = adapter.getSpecification();

        if (LOG.isDebugEnabled()) {
            LOG.debug("objectToElement(NO): create element and isis:title");
        }
        final Element element = schema.createElement(getXmlDocument(), nos.getShortIdentifier(), nos.getFullIdentifier(), nos.getSingularName(), nos.getPluralName());
        isisMetaModel.appendIsisTitle(element, adapter.titleString());

        if (LOG.isDebugEnabled()) {
            LOG.debug("objectToElement(NO): create XS element for Isis class");
        }
        final Element xsElement = schema.createXsElementForNofClass(getXsdDocument(), element, topLevelElementWritten, FacetUtil.getFacetsByType(nos));

        // hack: every element in the XSD schema apart from first needs minimum
        // cardinality setting.
        topLevelElementWritten = true;

        final Place place = new Place(adapter, element);

        isisMetaModel.setAttributesForClass(element, oidAsString(adapter).toString());

        final List<ObjectAssociation> fields = nos.streamAssociations(Contributed.INCLUDED)
                .collect(Collectors.toList());
        if (LOG.isDebugEnabled()) {
            LOG.debug("objectToElement(NO): processing fields");
        }
        eachField: for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final String fieldName = field.getId();

            if (LOG.isDebugEnabled()) {
                LOG.debug("objectToElement(NO): {}", log("field", fieldName));
            }

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
                if (fieldName.equals(fields.get(i).getName())) {
                    LOG.debug("objectToElement(NO): {} SKIPPED", log("field", fieldName));
                    continue eachField;
                }
            }

            Element xmlFieldElement = getXmlDocument().createElementNS(schema.getUri(), // scoped
                    // by
                    // namespace
                    // of class of
                    // containing object
                    schema.getPrefix() + ":" + fieldName);

            Element xsdFieldElement = null;

            if (field.getSpecification().containsFacet(ValueFacet.class)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("objectToElement(NO): {} is value", log("field", fieldName));
                }

                final ObjectSpecification fieldNos = field.getSpecification();
                // skip fields of type XmlValue
                if (fieldNos == null) {
                    continue eachField;
                }
                if (fieldNos.getFullIdentifier() != null && fieldNos.getFullIdentifier().endsWith("XmlValue")) {
                    continue eachField;
                }

                final OneToOneAssociation valueAssociation = ((OneToOneAssociation) field);
                final Element xmlValueElement = xmlFieldElement; // more
                // meaningful
                // locally
                // scoped name

                ObjectAdapter value;
                try {
                    value = valueAssociation.get(adapter, InteractionInitiatedBy.FRAMEWORK);

                    final ObjectSpecification valueNos = value.getSpecification();

                    // XML
                    isisMetaModel.setAttributesForValue(xmlValueElement, valueNos.getShortIdentifier());

                    // return parsed string, else encoded string, else title.
                    String valueStr;
                    final ParseableFacet parseableFacet = fieldNos.getFacet(ParseableFacet.class);
                    final EncodableFacet encodeableFacet = fieldNos.getFacet(EncodableFacet.class);
                    if (parseableFacet != null) {
                        valueStr = parseableFacet.parseableTitle(value);
                    } else if (encodeableFacet != null) {
                        valueStr = encodeableFacet.toEncodedString(value);
                    } else {
                        valueStr = value.titleString();
                    }

                    final boolean notEmpty = (valueStr.length() > 0);
                    if (notEmpty) {
                        xmlValueElement.appendChild(getXmlDocument().createTextNode(valueStr));
                    } else {
                        isisMetaModel.setIsEmptyAttribute(xmlValueElement, true);
                    }

                } catch (final Exception ex) {
                    LOG.warn("objectToElement(NO): {}: getField() threw exception - skipping XML generation", log("field", fieldName));
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofValue(xsElement, xmlValueElement, FacetUtil.getFacetsByType(valueAssociation));

            } else if (field instanceof OneToOneAssociation) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("objectToElement(NO): {} is OneToOneAssociation", log("field", fieldName));
                }

                final OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
                final String fullyQualifiedClassName = nos.getFullIdentifier();
                final Element xmlReferenceElement = xmlFieldElement; // more
                // meaningful
                // locally
                // scoped
                // name

                ObjectAdapter referencedObjectAdapter;

                try {
                    referencedObjectAdapter = oneToOneAssociation.get(adapter, InteractionInitiatedBy.FRAMEWORK);

                    // XML
                    isisMetaModel.setAttributesForReference(xmlReferenceElement, schema.getPrefix(), fullyQualifiedClassName);

                    if (referencedObjectAdapter != null) {
                        isisMetaModel.appendIsisTitle(xmlReferenceElement, referencedObjectAdapter.titleString());
                    } else {
                        isisMetaModel.setIsEmptyAttribute(xmlReferenceElement, true);
                    }

                } catch (final Exception ex) {
                    LOG.warn("objectToElement(NO): {}: getAssociation() threw exception - skipping XML generation", log("field", fieldName));
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofReference(xsElement, xmlReferenceElement, oneToOneAssociation.getSpecification().getFullIdentifier(), FacetUtil.getFacetsByType(oneToOneAssociation));

            } else if (field instanceof OneToManyAssociation) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("objectToElement(NO): {} is OneToManyAssociation", log("field", fieldName));
                }

                final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
                final Element xmlCollectionElement = xmlFieldElement; // more
                // meaningful
                // locally
                // scoped
                // name

                ObjectAdapter collection;
                try {
                    collection = oneToManyAssociation.get(adapter, InteractionInitiatedBy.FRAMEWORK);
                    final ObjectSpecification referencedTypeNos = oneToManyAssociation.getSpecification();
                    final String fullyQualifiedClassName = referencedTypeNos.getFullIdentifier();

                    // XML
                    isisMetaModel.setIsisCollection(xmlCollectionElement, schema.getPrefix(), fullyQualifiedClassName, collection);
                } catch (final Exception ex) {
                    LOG.warn("objectToElement(NO): {}: get(obj) threw exception - skipping XML generation", log("field", fieldName));
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofCollection(xsElement, xmlCollectionElement, oneToManyAssociation.getSpecification().getFullIdentifier(), FacetUtil.getFacetsByType(oneToManyAssociation));

            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("objectToElement(NO): {} is unknown type; ignored", log("field", fieldName));
                }
                continue;
            }

            if (xsdFieldElement != null) {
                Place.setXsdElement(xmlFieldElement, xsdFieldElement);
            }

            // XML
            if (LOG.isDebugEnabled()) {
                LOG.debug("objectToElement(NO): invoking mergeTree for field");
            }
            xmlFieldElement = mergeTree(element, xmlFieldElement);

            // XSD
            if (xsdFieldElement != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("objectToElement(NO): adding XS element for field to schema");
                }
                schema.addFieldXsElement(xsElement, xsdFieldElement);
            }
        }

        return place;
    }


    private final Map<ObjectAdapter, String> viewModelFakeOids = Maps.newHashMap();

    private String oidAsString(final ObjectAdapter adapter) {
        if(adapter.getObject() instanceof ViewModel) {
            // return a fake oid for view models;
            // a snapshot may be being used to create the memento/OID
            String fakeOid = viewModelFakeOids.get(adapter);
            if(fakeOid == null) {
                fakeOid = "viewmodel-fakeoid-" + UUID.randomUUID().toString();
                viewModelFakeOids.put(adapter, fakeOid);
            }
            return fakeOid;
        } else {
            return adapter.getOid().enString();
        }
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

    @Override
    public String getXmlDocumentAsString() {
        final Document doc = getXmlDocument();
        return asString(doc);
    }

    @Override
    public String getXsdDocumentAsString() {
        final Document doc = getXsdDocument();
        return asString(doc);
    }

    private static String asString(final Document doc) {
        try {
            final DOMSource domSource = new DOMSource(doc);
            final StringWriter writer = new StringWriter();
            final StreamResult result = new StreamResult(writer);
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(domSource, result);

            return writer.toString();
        } catch (TransformerConfigurationException e) {
            throw new IsisException(e);
        } catch (TransformerException e) {
            throw new IsisException(e);
        }
    }


}
