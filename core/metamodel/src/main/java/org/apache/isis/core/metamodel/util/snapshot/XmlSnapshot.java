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

package org.apache.isis.core.metamodel.util.snapshot;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService.Snapshot;
import org.apache.isis.applib.snapshot.SnapshottableWithInclusions;
import org.apache.isis.commons.internal.codec._DocumentFactories;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Traverses object graph from specified root, so that an XML representation of
 * the graph can be returned.
 *
 * <p>
 * Initially designed to allow snapshots to be easily created.
 *
 * <p>
 * Typical use, returning customer's fields, titles of simple references,
 * number of items in collections:
 *
 * <pre>
 *      XmlSnapshot snapshot = new XmlSnapshot(customer);
 *      Element customerAsXml = snapshot.toXml();
 * </pre>
 *
 * <p>
 * More complex use, navigates to another object represented by a simple
 * reference:
 *
 * <pre>
 *      XmlSnapshot snapshot = new XmlSnapshot(customer);
 *      snapshot.include(&quot;placeOfBirth&quot;);
 *      Element customerAsXml = snapshot.toXml();
 * </pre>
 *
 * <p>
 * More complex still, navigating to all <code>Order</code>s of
 *  <code>Customer</code>, and from them for
 *  their <code>Product</code>s
 *
 * <pre>
 *      XmlSnapshot snapshot = new XmlSnapshot(customer);
 *      snapshot.include(&quot;orders/product&quot;);
 *      Element customerAsXml = snapshot.toXml();
 * </pre>
 */
@Log4j2
public class XmlSnapshot implements Snapshot {

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
    public XmlSnapshot(final ManagedObject rootAdapter) {
        this(rootAdapter, new XmlSchema());
    }

    /**
     * Start a snapshot at the root object, using supplied namespace manager.
     */
    public XmlSnapshot(final ManagedObject rootAdapter, final XmlSchema schema) {

        if (log.isDebugEnabled()) {
            log.debug(".ctor({}{}{})", log("rootObj", rootAdapter), andlog("schema", schema),
                    andlog("addOids", "" + true));
        }

        this.isisMetaModel = new IsisSchema();
        this.xsMeta = new XsMetaModel();

        this.schema = schema;

        final DocumentBuilderFactory dbf = _DocumentFactories.documentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            this.xmlDocument = db.newDocument();
            this.xsdDocument = db.newDocument();

            xsdElement = xsMeta.createXsSchemaElement(xsdDocument);

            this.rootPlace = appendXml(rootAdapter);

        } catch (final ParserConfigurationException e) {
            log.error("unable to build snapshot", e);
            throw new UnrecoverableException(e);
        }

        for (final String path : getPathsFor(rootAdapter.getPojo())) {
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

    private String andlog(final String label, final ManagedObject object) {
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
     * referenced in the document that the parentElement resides within. (Normally
     * this is achieved simply by using appendXml passing in a new schemaManager -
     * see {@link XmlSnapshot}).
     */
    private Place appendXml(final ManagedObject object) {

        if (log.isDebugEnabled()) {
            log.debug("appendXml({})", log("obj", object));
        }

        final String fullyQualifiedClassName = object.getSpecification().getFullIdentifier();

        schema.setUri(fullyQualifiedClassName); // derive URI from fully qualified name

        final Place place = objectToElement(object);

        final Element element = place.getXmlElement();
        final Element xsElementElement = place.getXsdElement();

        if (log.isDebugEnabled()) {
            log.debug("appendXml(NO): add as element to XML doc");
        }
        getXmlDocument().appendChild(element);

        if (log.isDebugEnabled()) {
            log.debug("appendXml(NO): add as xs:element to xs:schema of the XSD document");
        }
        getXsdElement().appendChild(xsElementElement);

        if (log.isDebugEnabled()) {
            log.debug("appendXml(NO): set target name in XSD, derived from FQCN of obj");
        }
        schema.setTargetNamespace(getXsdDocument(), fullyQualifiedClassName);

        if (log.isDebugEnabled()) {
            log.debug("appendXml(NO): set schema location file name to XSD, derived from FQCN of obj");
        }
        final String schemaLocationFileName = fullyQualifiedClassName + ".xsd";
        schema.assignSchema(getXmlDocument(), fullyQualifiedClassName, schemaLocationFileName);

        if (log.isDebugEnabled()) {
            log.debug("appendXml(NO): copy into snapshot obj");
        }
        setXmlElement(element);
        setSchemaLocationFileName(schemaLocationFileName);

        return place;
    }

    /**
     * Creates an Element representing this object, and appends it to the supplied
     * parentElement, provided that an element for the object is not already
     * appended.
     *
     * The method uses the OID to determine if an object's element is already
     * present. If the object is not yet persistent, then the hashCode is used
     * instead.
     *
     * The parentElement must have an owner document, and should define the
     * &quot;isis&quot; namespace. Additionally, the supplied schemaManager must be
     * populated with any application-level namespaces referenced in the document
     * that the parentElement resides within. (Normally this is achieved simply by
     * using appendXml passing in a rootElement and a new schemaManager - see
     * {@link XmlSnapshot}).
     */
    private Element appendXml(final Place parentPlace, final ManagedObject childObject) {

        if (log.isDebugEnabled()) {
            log.debug("appendXml({}{})", log("parentPlace", parentPlace), andlog("childObj", childObject));
        }

        final Element parentElement = parentPlace.getXmlElement();
        final Element parentXsElement = parentPlace.getXsdElement();

        if (parentElement.getOwnerDocument() != getXmlDocument()) {
            throw new IllegalArgumentException("parent XML Element must have snapshot's XML document as its owner");
        }

        if (log.isDebugEnabled()) {
            log.debug("appendXml(Pl, NO): invoking objectToElement() for {}", log("childObj", childObject));
        }
        final Place childPlace = objectToElement(childObject);
        Element childElement = childPlace.getXmlElement();
        final Element childXsElement = childPlace.getXsdElement();

        if (log.isDebugEnabled()) {
            log.debug("appendXml(Pl, NO): invoking mergeTree of parent with child");
        }
        childElement = mergeTree(parentElement, childElement);

        if (log.isDebugEnabled()) {
            log.debug("appendXml(Pl, NO): adding XS Element to schema if required");
        }
        schema.addXsElementIfNotPresent(parentXsElement, childXsElement);

        return childElement;
    }

    private boolean appendXmlThenIncludeRemaining(
            final Place parentPlace, final ManagedObject referencedObject,
            final Vector<String> fieldNames, final String annotation) {

        if (log.isDebugEnabled()) {
            log.debug("appendXmlThenIncludeRemaining(: {}{}{}{})", log("parentPlace", parentPlace),
                    andlog("referencedObj", referencedObject), andlog("fieldNames", fieldNames),
                    andlog("annotation", annotation));
            log.debug("appendXmlThenIncludeRemaining(..): invoking appendXml(parentPlace, referencedObject)");
        }

        final Element referencedElement = appendXml(parentPlace, referencedObject);
        final Place referencedPlace = new Place(referencedObject, referencedElement);

        final boolean includedField = includeField(referencedPlace, fieldNames, annotation);

        if (log.isDebugEnabled()) {
            log.debug("appendXmlThenIncludeRemaining(..): invoked includeField(referencedPlace, fieldNames){}",
                    andlog("returned", "" + includedField));
        }

        return includedField;
    }

    private Vector<Element> elementsUnder(final Element parentElement, final String localName) {
        final Vector<Element> v = new Vector<>();
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

    public ManagedObject getObject() {
        return rootPlace.getObject();
    }

    public XmlSchema getSchema() {
        return schema;
    }

    /**
     * The name of the <code>xsi:schemaLocation</code> in the XML document.
     *
     * Taken from the <code>fullyQualifiedClassName</code> (which also is used as
     * the basis for the <code>targetNamespace</code>.
     *
     * Populated in {@link #appendXml(ManagedObject)}.
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
        final Vector<String> fieldNames = new Vector<>();
        for (final StringTokenizer tok = new StringTokenizer(path, "/"); tok.hasMoreTokens();) {
            final String token = tok.nextToken();

            if (log.isDebugEnabled()) {
                log.debug("include(..): {}", log("token", token));
            }
            fieldNames.addElement(token);
        }

        if (log.isDebugEnabled()) {
            log.debug("include(..): {}", log("fieldNames", fieldNames));
        }

        // navigate first field, from the root.
        if (log.isDebugEnabled()) {
            log.debug("include(..): invoking includeField");
        }
        includeField(rootPlace, fieldNames, annotation);
    }

    /**
     * @return true if able to navigate the complete vector of field names
     *         successfully; false if a field could not be located or it turned out
     *         to be a value.
     */
    private boolean includeField(final Place place, final Vector<String> fieldNames, final String annotation) {

        if (log.isDebugEnabled()) {
            log.debug("includeField(: {}{}{})", log("place", place), andlog("fieldNames", fieldNames),
                    andlog("annotation", annotation));
        }

        final ManagedObject object = place.getObject();
        final Element xmlElement = place.getXmlElement();

        // we use a copy of the path so that we can safely traverse collections
        // without side-effects
        final Vector<String> originalNames = fieldNames;
        final Vector<String> names = new Vector<>();
        for (final Enumeration<String> e = originalNames.elements(); e.hasMoreElements();) {
            names.addElement(e.nextElement());
        }

        // see if we have any fields to process
        if (names.size() == 0) {
            return true;
        }

        // take the first field name from the list, and remove
        final String fieldName = names.elementAt(0);
        names.removeElementAt(0);

        if (log.isDebugEnabled()) {
            log.debug("includeField(Pl, Vec, Str):{}{}", log("processing field", fieldName),
                    andlog("left", "" + names.size()));
        }

        // locate the field in the object's class
        final ObjectSpecification nos = object.getSpecification();
        // HACK: really want a ObjectSpecification.hasField method to
        // check first.
        val field = nos.getAssociation(fieldName).orElse(null);
        if (field == null) {
            if (log.isInfoEnabled()) {
                log.info("includeField(Pl, Vec, Str): could not locate field, skipping");
            }
            return false;
        }

        // locate the corresponding XML element
        // (the corresponding XSD element will later be attached to xmlElement
        // as its userData)
        if (log.isDebugEnabled()) {
            log.debug("includeField(Pl, Vec, Str): locating corresponding XML element");
        }
        val xmlFieldElements = elementsUnder(xmlElement, field.getId());
        if (xmlFieldElements.size() != 1) {
            if (log.isInfoEnabled()) {
                log.info("includeField(Pl, Vec, Str): could not locate {}",
                        log("field", field.getId()) + andlog("xmlFieldElements.size", "" + xmlFieldElements.size()));
            }
            return false;
        }
        final Element xmlFieldElement = xmlFieldElements.elementAt(0);

        if (names.size() == 0 && annotation != null) {
            // nothing left in the path, so we will apply the annotation now
            isisMetaModel.setAnnotationAttribute(xmlFieldElement, annotation);
        }

        final Place fieldPlace = new Place(object, xmlFieldElement);

        if (field instanceof OneToOneAssociation) {

            if (field.getSpecification().streamAssociations(MixedIn.INCLUDED).limit(1).count() == 0L) {
                if (log.isDebugEnabled()) {
                    log.debug("includeField(Pl, Vec, Str): field is value; done");
                }
                return false;
            }

            if (log.isDebugEnabled()) {
                log.debug("includeField(Pl, Vec, Str): field is 1->1");
            }

            final OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);

            if(oneToOneAssociation.isNotPersisted()) {
                return false;
            }
            final ManagedObject referencedObject = oneToOneAssociation.get(fieldPlace.getObject(),
                    InteractionInitiatedBy.FRAMEWORK);

            if (referencedObject == null) {
                return true; // not a failure if the reference was null
            }

            final boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, names, annotation);
            if (log.isDebugEnabled()) {
                log.debug("includeField(Pl, Vec, Str): 1->1: invoked appendXmlThenIncludeRemaining for {}{}",
                        log("referencedObj", referencedObject), andlog("returned", "" + appendedXml));
            }

            return appendedXml;

        } else if (field instanceof OneToManyAssociation) {
            if (log.isDebugEnabled()) {
                log.debug("includeField(Pl, Vec, Str): field is 1->M");
            }

            final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
            final ManagedObject collection = oneToManyAssociation.get(fieldPlace.getObject(),
                    InteractionInitiatedBy.FRAMEWORK);

            if (log.isDebugEnabled()) {
                log.debug("includeField(Pl, Vec, Str): 1->M: {}",
                        log("collection.size", "" + CollectionFacet.elementCount(collection)));
            }

            final boolean[] allFieldsNavigated = { true }; // fast non-thread-safe value reference

            CollectionFacet.streamAdapters(collection).forEach(referencedObject -> {
                final boolean appendedXml = appendXmlThenIncludeRemaining(fieldPlace, referencedObject, names,
                        annotation);
                if (log.isDebugEnabled()) {
                    log.debug("includeField(Pl, Vec, Str): 1->M: + invoked appendXmlThenIncludeRemaining for {}{}",
                            log("referencedObj", referencedObject), andlog("returned", "" + appendedXml));
                }
                allFieldsNavigated[0] = allFieldsNavigated[0] && appendedXml;
            });

            log.debug("includeField(Pl, Vec, Str): {}", log("returning", "" + allFieldsNavigated));
            return allFieldsNavigated[0];
        }

        return false; // fall through, shouldn't get here but just in
        // case.
    }

    private String log(final String label, final ManagedObject adapter) {
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
     * The element returned will be either the supplied <code>childElement</code>,
     * or an existing child element if one already existed under
     * <code>parentElement</code>.
     */
    private Element mergeTree(final Element parentElement, final Element childElement) {

        if (log.isDebugEnabled()) {
            log.debug("mergeTree({}{})", log("parent", parentElement), andlog("child", childElement));
        }

        final String childElementOid = isisMetaModel.getAttribute(childElement, "oid");

        if (log.isDebugEnabled()) {
            log.debug("mergeTree(El,El): {}", log("childOid", childElementOid));
        }
        if (childElementOid != null) {

            // before we add the child element, check to see if it is already
            // there
            if (log.isDebugEnabled()) {
                log.debug("mergeTree(El,El): check if child already there");
            }
            final Vector<Element> existingChildElements = elementsUnder(parentElement, childElement.getLocalName());
            for (final Enumeration<Element> childEnum = existingChildElements.elements(); childEnum.hasMoreElements();) {
                final Element possibleMatchingElement = childEnum.nextElement();

                final String possibleMatchOid = isisMetaModel.getAttribute(possibleMatchingElement, "oid");
                if (possibleMatchOid == null || !possibleMatchOid.equals(childElementOid)) {
                    continue;
                }

                if (log.isDebugEnabled()) {
                    log.debug("mergeTree(El,El): child already there; merging grandchildren");
                }

                // match: transfer the children of the child (grandchildren) to
                // the
                // already existing matching child
                final Element existingChildElement = possibleMatchingElement;
                final Vector<Element> grandchildrenElements = elementsUnder(childElement, "*");
                for (final Enumeration<Element> grandchildEnum = grandchildrenElements.elements(); grandchildEnum
                        .hasMoreElements();) {
                    final Element grandchildElement = grandchildEnum.nextElement();
                    childElement.removeChild(grandchildElement);

                    if (log.isDebugEnabled()) {
                        log.debug("mergeTree(El,El): merging {}", log("grandchild", grandchildElement));
                    }

                    mergeTree(existingChildElement, grandchildElement);
                }
                return existingChildElement;
            }
        }

        parentElement.appendChild(childElement);
        return childElement;
    }

    Place objectToElement(final ManagedObject adapter) {

        if (log.isDebugEnabled()) {
            log.debug("objectToElement({})", log("object", adapter));
        }

        final ObjectSpecification spec = adapter.getSpecification();

        if (log.isDebugEnabled()) {
            log.debug("objectToElement(NO): create element and isis:title");
        }
        final Element element = schema.createElement(getXmlDocument(), spec.getShortIdentifier(),
                spec.getFullIdentifier(), spec.getSingularName(), spec.getPluralName());
        isisMetaModel.appendIsisTitle(element, adapter.titleString());

        if (log.isDebugEnabled()) {
            log.debug("objectToElement(NO): create XS element for Isis class");
        }
        final Element xsElement = schema.createXsElementForNofClass(getXsdDocument(), element, topLevelElementWritten,
                FacetUtil.getFacetsByType(spec));

        // hack: every element in the XSD schema apart from first needs minimum
        // cardinality setting.
        topLevelElementWritten = true;

        final Place place = new Place(adapter, element);

        isisMetaModel.setAttributesForClass(element, oidAsString(adapter).toString());

        final List<ObjectAssociation> fields = spec.streamAssociations(MixedIn.INCLUDED)
                .collect(Collectors.toList());
        if (log.isDebugEnabled()) {
            log.debug("objectToElement(NO): processing fields");
        }
        eachField: for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final String fieldName = field.getId();

            if (log.isDebugEnabled()) {
                log.debug("objectToElement(NO): {}", log("field", fieldName));
            }

            // Skip field if we have seen the name already
            for (int j = 0; j < i; j++) {
                if (Objects.equals(fieldName, fields.get(i).getFriendlyName(adapter.asProvider()))) {
                    log.debug("objectToElement(NO): {} SKIPPED", log("field", fieldName));
                    continue eachField;
                }
            }

            Element xmlFieldElement = getXmlDocument().createElementNS(
                    schema.getUri(), // scoped by namespace of class
                    /* of containing object*/ schema.getPrefix() + ":" + fieldName);

            Element xsdFieldElement = null;

            if (field.getSpecification().containsFacet(ValueFacet.class)) {
                if (log.isDebugEnabled()) {
                    log.debug("objectToElement(NO): {} is value", log("field", fieldName));
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
                if(valueAssociation.isNotPersisted()) {
                    continue eachField;
                }
                final Element xmlValueElement = xmlFieldElement; // more meaningful locally scoped name

                ManagedObject value;
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
                    log.warn("objectToElement(NO): {}: getField() threw exception - skipping XML generation",
                            log("field", fieldName));
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofValue(xsElement, xmlValueElement,
                        FacetUtil.getFacetsByType(valueAssociation));

            } else if (field instanceof OneToOneAssociation) {

                if (log.isDebugEnabled()) {
                    log.debug("objectToElement(NO): {} is OneToOneAssociation", log("field", fieldName));
                }

                final OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
                final String fullyQualifiedClassName = spec.getFullIdentifier();
                final Element xmlReferenceElement = xmlFieldElement; // more meaningful locally scoped name

                ManagedObject referencedObjectAdapter;

                try {
                    referencedObjectAdapter = oneToOneAssociation.get(adapter, InteractionInitiatedBy.FRAMEWORK);

                    // XML
                    isisMetaModel.setAttributesForReference(xmlReferenceElement, schema.getPrefix(),
                            fullyQualifiedClassName);

                    if (referencedObjectAdapter != null) {
                        isisMetaModel.appendIsisTitle(xmlReferenceElement, referencedObjectAdapter.titleString());
                    } else {
                        isisMetaModel.setIsEmptyAttribute(xmlReferenceElement, true);
                    }

                } catch (final Exception ex) {
                    log.warn("objectToElement(NO): {}: getAssociation() threw exception - skipping XML generation",
                            log("field", fieldName));
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofReference(xsElement, xmlReferenceElement,
                        oneToOneAssociation.getSpecification().getFullIdentifier(),
                        FacetUtil.getFacetsByType(oneToOneAssociation));

            } else if (field instanceof OneToManyAssociation) {

                if (log.isDebugEnabled()) {
                    log.debug("objectToElement(NO): {} is OneToManyAssociation", log("field", fieldName));
                }

                final OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
                final Element xmlCollectionElement = xmlFieldElement; // more meaningful locally scoped name

                ManagedObject collection;
                try {
                    collection = oneToManyAssociation.get(adapter, InteractionInitiatedBy.FRAMEWORK);
                    final ObjectSpecification referencedTypeNos = oneToManyAssociation.getSpecification();
                    final String fullyQualifiedClassName = referencedTypeNos.getFullIdentifier();

                    // XML
                    isisMetaModel.setIsisCollection(xmlCollectionElement, schema.getPrefix(), fullyQualifiedClassName,
                            collection);
                } catch (final Exception ex) {
                    log.warn("objectToElement(NO): {}: get(obj) threw exception - skipping XML generation",
                            log("field", fieldName));
                }

                // XSD
                xsdFieldElement = schema.createXsElementForNofCollection(xsElement, xmlCollectionElement,
                        oneToManyAssociation.getSpecification().getFullIdentifier(),
                        FacetUtil.getFacetsByType(oneToManyAssociation));

            } else {
                if (log.isInfoEnabled()) {
                    log.info("objectToElement(NO): {} is unknown type; ignored", log("field", fieldName));
                }
                continue;
            }

            Place.setXsdElement(xmlFieldElement, xsdFieldElement);

            // XML
            if (log.isDebugEnabled()) {
                log.debug("objectToElement(NO): invoking mergeTree for field");
            }
            xmlFieldElement = mergeTree(element, xmlFieldElement);

            // XSD
            if (log.isDebugEnabled()) {
                log.debug("objectToElement(NO): adding XS element for field to schema");
            }
            schema.addFieldXsElement(xsElement, xsdFieldElement);
        }

        return place;
    }

    private final Map<ManagedObject, String> viewModelFakeOids = _Maps.newHashMap();

    private String oidAsString(final ManagedObject adapter) {
        if (adapter.getPojo() instanceof ViewModel) {
            // return a fake oid for view models;
            // a snapshot may be used to create the memento/OID
            String fakeOid = viewModelFakeOids.get(adapter);
            if (fakeOid == null) {
                fakeOid = "viewmodel-fakeoid-" + UUID.randomUUID().toString();
                viewModelFakeOids.put(adapter, fakeOid);
            }
            return fakeOid;
        } else {
            return ManagedObjects.stringifyElseFail(adapter);
        }
    }

    /**
     * @param schemaLocationFileName The schemaLocationFileName to set.
     */
    private void setSchemaLocationFileName(final String schemaLocationFileName) {
        this.schemaLocationFileName = schemaLocationFileName;
    }

    /**
     * @param xmlElement The xmlElement to set.
     */
    private void setXmlElement(final Element xmlElement) {
        this.xmlElement = xmlElement;
    }


}
