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

import java.util.function.BiConsumer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService.Snapshot;

/**
 * Represents the schema for the derived snapshot.
 */
public final class XmlSchema {

    private final String prefix;
    private final String uriBase;
    private String uri;

    private final IsisSchema isisMeta;
    private final XsMetaModel xsMeta;
    private final Helper helper;

    /**
     * The base part of the namespace prefix to use if none explicitly supplied
     * in the constructor.
     */
    public final static String DEFAULT_PREFIX = "app";
    
    public static interface ExtensionData<T> {
        public int size();
        public void visit(BiConsumer<Class<T>, T> elementConsumer);
    }

    public XmlSchema() {
        this(IsisSchema.DEFAULT_URI_BASE, XmlSchema.DEFAULT_PREFIX);
    }

    /**
     * @param uriBase
     *            the prefix for the application namespace's URIs
     * @param prefix
     *            the prefix for the application namespace's prefix
     */
    public XmlSchema(final String uriBase, final String prefix) {
        this.isisMeta = new IsisSchema();
        this.xsMeta = new XsMetaModel();
        this.helper = new Helper();

        final String base = new Helper().trailingSlash(uriBase);
        if (XsMetaModel.W3_ORG_XMLNS_URI.equals(base)) {
            throw new IllegalArgumentException("Namespace URI reserved for w3.org XMLNS namespace");
        }
        if (XsMetaModel.W3_ORG_XMLNS_PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("Namespace prefix reserved for w3.org XMLNS namespace.");
        }
        if (XsMetaModel.W3_ORG_XS_URI.equals(base)) {
            throw new IllegalArgumentException("Namespace URI reserved for w3.org XML schema namespace.");
        }
        if (XsMetaModel.W3_ORG_XS_PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("Namespace prefix reserved for w3.org XML schema namespace.");
        }
        if (XsMetaModel.W3_ORG_XSI_URI.equals(base)) {
            throw new IllegalArgumentException("Namespace URI reserved for w3.org XML schema-instance namespace.");
        }
        if (XsMetaModel.W3_ORG_XSI_PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("Namespace prefix reserved for w3.org XML schema-instance namespace.");
        }
        if (IsisSchema.NS_URI.equals(base)) {
            throw new IllegalArgumentException("Namespace URI reserved for NOF metamodel namespace.");
        }
        if (IsisSchema.NS_PREFIX.equals(prefix)) {
            throw new IllegalArgumentException("Namespace prefix reserved for NOF metamodel namespace.");
        }
        this.uriBase = base;
        this.prefix = prefix;
    }

    /**
     * The base of the Uri in use. All namespaces are concatenated with this.
     *
     * The namespace string will be the concatenation of the plus the package
     * name of the class of the object being referenced.
     *
     * If not specified in the constructor, then {@link #DEFAULT_URI_PREFIX} is
     * used.
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
     * The value returned will be <code>null</code> until a {@link Snapshot} is
     * created.
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
     * Creates an element with the specified localName, in the appropriate
     * namespace for the NOS.
     *
     * If necessary the namespace definition is added to the root element of the
     * doc used to create the element. The element is not parented but to avoid
     * an error can only be added as a child of another element in the same doc.
     */
    Element createElement(final Document doc, final String localName, final String fullyQualifiedClassName, final String singularName, final String pluralName) {
        final Element element = doc.createElementNS(getUri(), getPrefix() + ":" + localName);
        element.setAttributeNS(IsisSchema.NS_URI, IsisSchema.NS_PREFIX + ":fqn", fullyQualifiedClassName);
        element.setAttributeNS(IsisSchema.NS_URI, IsisSchema.NS_PREFIX + ":singular", singularName);
        element.setAttributeNS(IsisSchema.NS_URI, IsisSchema.NS_PREFIX + ":plural", pluralName);
        isisMeta.addNamespace(element); // good a place as any

        addNamespace(element, getPrefix(), getUri());
        return element;
    }

    /**
     * Sets the target namespace for the XSD document to a URI derived from the
     * fully qualified class name of the supplied object
     */
    void setTargetNamespace(final Document xsdDoc, final String fullyQualifiedClassName) {

        final Element xsSchemaElement = xsdDoc.getDocumentElement();
        if (xsSchemaElement == null) {
            throw new IllegalArgumentException("XSD Document must have <xs:schema> element attached");
        }

        // targetNamespace="http://isis.apache.org/ns/app/<fully qualified class
        // name>
        xsSchemaElement.setAttribute("targetNamespace", getUri());

        addNamespace(xsSchemaElement, getPrefix(), getUri());
    }
    


    /**
     * Creates an &lt;xs:element&gt; element defining the presence of the named
     * element representing a class
     */
    <T> Element createXsElementForNofClass(
            final Document xsdDoc, 
            final Element element, 
            final boolean addCardinality, 
            final ExtensionData<T> extensions) {

        // gather details from XML element
        final String localName = element.getLocalName();

        // <xs:element name="AO11ConfirmAnimalRegistration">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element ref="isis:title"/>
        // <!-- placeholder -->
        // </xs:sequence>
        // <xs:attribute ref="isis:feature"
        // default="class"/>
        // <xs:attribute ref="isis:oid"/>
        // <xs:attribute ref="isis:annotation"/>
        // <xs:attribute ref="isis:fqn"/>
        // </xs:complexType>
        // </xs:element>

        // xs:element/@name="class name"
        // add to XML schema as a global attribute
        final Element xsElementForNofClassElement = xsMeta.createXsElementElement(xsdDoc, localName, addCardinality);

        // xs:element/xs:complexType
        // xs:element/xs:complexType/xs:sequence
        final Element xsComplexTypeElement = xsMeta.complexTypeFor(xsElementForNofClassElement);
        final Element xsSequenceElement = xsMeta.sequenceFor(xsComplexTypeElement);

        // xs:element/xs:complexType/xs:sequence/xs:element ref="isis:title"
        final Element xsTitleElement = xsMeta.createXsElement(helper.docFor(xsSequenceElement), "element");
        xsTitleElement.setAttribute("ref", IsisSchema.NS_PREFIX + ":" + "title");
        xsSequenceElement.appendChild(xsTitleElement);
        xsMeta.setXsCardinality(xsTitleElement, 0, 1);

        // xs:element/xs:complexType/xs:sequence/xs:element ref="extensions"
        addXsElementForAppExtensions(xsSequenceElement, extensions);

        // xs:element/xs:complexType/xs:attribute ...
        xsMeta.addXsIsisFeatureAttributeElements(xsComplexTypeElement, "class");
        xsMeta.addXsIsisAttribute(xsComplexTypeElement, "oid");
        xsMeta.addXsIsisAttribute(xsComplexTypeElement, "fqn");
        xsMeta.addXsIsisAttribute(xsComplexTypeElement, "singular");
        xsMeta.addXsIsisAttribute(xsComplexTypeElement, "plural");
        xsMeta.addXsIsisAttribute(xsComplexTypeElement, "annotation");

        Place.setXsdElement(element, xsElementForNofClassElement);

        return xsElementForNofClassElement;
    }

    /**
     * Creates an <code>xs:element</code> element to represent a collection of
     * application-defined extensions
     *
     * The returned element should be appended to <code>xs:sequence</code>
     * element of the xs:element representing the type of the owning object.
     */
    <T> void addXsElementForAppExtensions(final Element parentXsElementElement, final ExtensionData<T> extensions) {

        if (extensions.size() == 0) {
            return;
        }

        // <xs:element name="extensions">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element name="app:%extension class short name%" minOccurs="0"
        // maxOccurs="1" default="%value%"/>
        // <xs:element name="app:%extension class short name%" minOccurs="0"
        // maxOccurs="1" default="%value%"/>
        // ...
        // <xs:element name="app:%extension class short name%" minOccurs="0"
        // maxOccurs="1" default="%value%"/>
        // </xs:sequence>
        // </xs:complexType>
        // </xs:element>

        // xs:element name="isis-extensions"
        // xs:element/xs:complexType/xs:sequence
        final Element xsExtensionsSequenceElement = addExtensionsElement(parentXsElementElement);

        addExtensionElements(xsExtensionsSequenceElement, extensions);

        return;
    }

    /**
     * Adds an isis-extensions element and a complexType and sequence elements
     * underneath.
     *
     * <p>
     * Returns the sequence element so that it can be appended to.
     */
    private Element addExtensionsElement(final Element parentXsElement) {
        final Element xsExtensionsElementElement = xsMeta.createXsElementElement(helper.docFor(parentXsElement), "isis-extensions");
        parentXsElement.appendChild(xsExtensionsElementElement);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence
        final Element xsExtensionsComplexTypeElement = xsMeta.complexTypeFor(xsExtensionsElementElement);
        final Element xsExtensionsSequenceElement = xsMeta.sequenceFor(xsExtensionsComplexTypeElement);

        return xsExtensionsSequenceElement;
    }

    private String shortName(final String className) {
        final int lastPeriodIdx = className.lastIndexOf('.');
        if (lastPeriodIdx < 0) {
            return className;
        }
        return className.substring(lastPeriodIdx + 1);
    }

    /**
     * Creates an <code>xs:element</code> element to represent a value field in
     * a class.
     *
     * The returned element should be appended to <code>xs:sequence</code>
     * element of the xs:element representing the type of the owning object.
     */
    <T> Element createXsElementForNofValue(
            final Element parentXsElementElement, 
            final Element xmlValueElement, 
            final ExtensionData<T> extensions) {

        // gather details from XML element
        final String datatype = xmlValueElement.getAttributeNS(IsisSchema.NS_URI, "datatype");
        final String fieldName = xmlValueElement.getLocalName();

        // <xs:element name="%owning object%">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element name="%%field object%%">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element name="isis-extensions">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element name="%extensionClassShortName%"
        // default="%extensionObjString" minOccurs="0"/>
        // <xs:element name="%extensionClassShortName%"
        // default="%extensionObjString" minOccurs="0"/>
        // ...
        // <xs:element name="%extensionClassShortName%"
        // default="%extensionObjString" minOccurs="0"/>
        // </xs:sequence>
        // </xs:complexType>
        // </xs:element>
        // </xs:sequence>
        // <xs:attribute ref="isis:feature" fixed="value"/>
        // <xs:attribute ref="isis:datatype" fixed="isis:%datatype%"/>
        // <xs:attribute ref="isis:isEmpty"/>
        // <xs:attribute ref="isis:annotation"/>
        // </xs:complexType>
        // </xs:element>
        // </xs:sequence>
        // </xs:complexType>
        // </xs:element>

        // xs:element/xs:complexType/xs:sequence
        final Element parentXsComplexTypeElement = xsMeta.complexTypeFor(parentXsElementElement);
        final Element parentXsSequenceElement = xsMeta.sequenceFor(parentXsComplexTypeElement);

        // xs:element/xs:complexType/xs:sequence/xs:element
        // name="%%field object%"
        final Element xsFieldElementElement = xsMeta.createXsElementElement(helper.docFor(parentXsSequenceElement), fieldName);
        parentXsSequenceElement.appendChild(xsFieldElementElement);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType
        final Element xsFieldComplexTypeElement = xsMeta.complexTypeFor(xsFieldElementElement);

        // NEW CODE TO SUPPORT EXTENSIONS;
        // uses a complexType/sequence

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence
        final Element xsFieldSequenceElement = xsMeta.sequenceFor(xsFieldComplexTypeElement);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element
        // name="isis-extensions"
        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence
        addXsElementForAppExtensions(xsFieldSequenceElement, extensions);

        xsMeta.addXsIsisFeatureAttributeElements(xsFieldComplexTypeElement, "value");
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "datatype", datatype);
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "isEmpty");
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "annotation");

        return xsFieldElementElement;
    }

    private <T> void addExtensionElements(final Element parentElement, final ExtensionData<T> extensions) {
        
        extensions.visit((final Class<T> extensionClass, final T extensionObject)->{
            
            // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element
            // name="%extensionClassShortName%"
            final Element xsExtensionElementElement = xsMeta.createXsElementElement(
                    helper.docFor(parentElement), "x-" + shortName(extensionClass.getName()));
            xsExtensionElementElement.setAttribute("default", extensionObject.toString()); // the
            // value
            xsExtensionElementElement.setAttribute("minOccurs", "0"); // doesn't
            // need to
            // appear
            // in XML
            // (and
            // indeed won't)
            parentElement.appendChild(xsExtensionElementElement);
        });
       
    }

    /**
     * Creates an &lt;xs:element&gt; element defining the presence of the named
     * element representing a reference to a class; appended to xs:sequence
     * element
     */
    <T> Element createXsElementForNofReference(
            final Element parentXsElementElement, 
            final Element xmlReferenceElement, 
            final String referencedClassName, 
            final ExtensionData<T> extensions) {

        // gather details from XML element
        final String fieldName = xmlReferenceElement.getLocalName();

        // <xs:element name="%owning object%">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element name="%%field object%%">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element ref="isis:title" minOccurs="0"/>
        // <xs:element name="isis-extensions">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element name="app:%extension class short name%" minOccurs="0"
        // maxOccurs="1" default="%value%"/>
        // <xs:element name="app:%extension class short name%" minOccurs="0"
        // maxOccurs="1" default="%value%"/>
        // ...
        // <xs:element name="app:%extension class short name%" minOccurs="0"
        // maxOccurs="1" default="%value%"/>
        // </xs:sequence>
        // </xs:complexType>
        // </xs:element>
        // <xs:sequence minOccurs="0" maxOccurs="1"/>
        // </xs:sequence>
        // <xs:attribute ref="isis:feature" fixed="reference"/>
        // <xs:attribute ref="isis:type" default="%%appX%%:%%type%%"/>
        // <xs:attribute ref="isis:isEmpty"/>
        // <xs:attribute ref="isis:annotation"/>
        // </xs:complexType>
        // </xs:element>
        // </xs:sequence>
        // </xs:complexType>
        // </xs:element>

        // xs:element/xs:complexType/xs:sequence
        final Element parentXsComplexTypeElement = xsMeta.complexTypeFor(parentXsElementElement);
        final Element parentXsSequenceElement = xsMeta.sequenceFor(parentXsComplexTypeElement);

        // xs:element/xs:complexType/xs:sequence/xs:element
        // name="%%field object%"
        final Element xsFieldElementElement = xsMeta.createXsElementElement(helper.docFor(parentXsSequenceElement), fieldName);
        parentXsSequenceElement.appendChild(xsFieldElementElement);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence
        final Element xsFieldComplexTypeElement = xsMeta.complexTypeFor(xsFieldElementElement);
        final Element xsFieldSequenceElement = xsMeta.sequenceFor(xsFieldComplexTypeElement);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element
        // ref="isis:title"
        final Element xsFieldTitleElement = xsMeta.createXsElement(helper.docFor(xsFieldSequenceElement), "element");
        xsFieldTitleElement.setAttribute("ref", IsisSchema.NS_PREFIX + ":" + "title");
        xsFieldSequenceElement.appendChild(xsFieldTitleElement);
        xsMeta.setXsCardinality(xsFieldTitleElement, 0, 1);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element
        // name="isis-extensions"
        addXsElementForAppExtensions(xsFieldSequenceElement, extensions);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:sequence
        // //
        // placeholder
        final Element xsReferencedElementSequenceElement = xsMeta.sequenceFor(xsFieldSequenceElement);
        xsMeta.setXsCardinality(xsReferencedElementSequenceElement, 0, 1);

        xsMeta.addXsIsisFeatureAttributeElements(xsFieldComplexTypeElement, "reference");
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "type", "app:" + referencedClassName, false);
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "isEmpty");
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "annotation");

        return xsFieldElementElement;
    }

    /**
     * Creates an &lt;xs:element&gt; element defining the presence of the named
     * element representing a collection in a class; appended to xs:sequence
     * element
     */
    <T> Element createXsElementForNofCollection(
            final Element parentXsElementElement, 
            final Element xmlCollectionElement, 
            final String referencedClassName, 
            final ExtensionData<T> extensions) {

        // gather details from XML element
        final String fieldName = xmlCollectionElement.getLocalName();

        // <xs:element name="%owning object%">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element name="%%field object%%">
        // <xs:complexType>
        // <xs:sequence>
        // <xs:element ref="isis:oids" minOccurs="0" maxOccurs="1"/>
        // <!-- nested element definitions go here -->
        // </xs:sequence>
        // <xs:attribute ref="isis:feature" fixed="collection"/>
        // <xs:attribute ref="isis:type" fixed="%%appX%%:%%type%%"/>
        // <xs:attribute ref="isis:size"/>
        // <xs:attribute ref="isis:annotation"/>
        // </xs:complexType>
        // </xs:element>
        // </xs:sequence>
        // </xs:complexType>
        // </xs:element>

        // xs:element/xs:complexType/xs:sequence
        final Element parentXsComplexTypeElement = xsMeta.complexTypeFor(parentXsElementElement);
        final Element parentXsSequenceElement = xsMeta.sequenceFor(parentXsComplexTypeElement);

        // xs:element/xs:complexType/xs:sequence/xs:element
        // name="%field object%%"
        final Element xsFieldElementElement = xsMeta.createXsElementElement(helper.docFor(parentXsSequenceElement), fieldName);
        parentXsSequenceElement.appendChild(xsFieldElementElement);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType
        final Element xsFieldComplexTypeElement = xsMeta.complexTypeFor(xsFieldElementElement);
        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence
        final Element xsFieldSequenceElement = xsMeta.sequenceFor(xsFieldComplexTypeElement);

        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:element
        // ref="isis:oids"
        final Element xsFieldOidsElement = xsMeta.createXsElement(helper.docFor(xsFieldSequenceElement), "element");
        xsFieldOidsElement.setAttribute("ref", IsisSchema.NS_PREFIX + ":" + "oids");
        xsFieldSequenceElement.appendChild(xsFieldOidsElement);
        xsMeta.setXsCardinality(xsFieldOidsElement, 0, 1);

        // extensions
        addXsElementForAppExtensions(xsFieldSequenceElement, extensions);

        // //
        // xs:element/xs:complexType/xs:sequence/xs:element/xs:complexType/xs:sequence/xs:choice
        // Element xsFieldChoiceElement =
        // xsMeta.choiceFor(xsFieldComplexTypeElement); // placeholder
        // xsMeta.setXsCardinality(xsFieldChoiceElement, 0, Integer.MAX_VALUE);

        // Element xsFieldTitleElement =
        // addXsNofRefElementElement(xsFieldSequenceElement, "title");

        // Element xsReferencedElementSequenceElement =
        // sequenceFor(xsFieldSequenceElement);
        // setXsCardinality(xsReferencedElementSequenceElement, 0, 1);

        xsMeta.addXsIsisFeatureAttributeElements(xsFieldComplexTypeElement, "collection");
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "type", "app:" + referencedClassName, false);
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "size");
        xsMeta.addXsIsisAttribute(xsFieldComplexTypeElement, "annotation");

        return xsFieldElementElement;
    }

    /**
     *
     * <pre>
     *     xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;
     *     xsi:schemaLocation=&quot;http://isis.apache.org/ns/app/sdm.common.fixture.schemes.ao.communications ddd.xsd&quot;
     * </pre>
     *
     * Assumes that the URI has been specified.
     *
     * @param xmlDoc
     * @param fullyQualifiedClassName
     * @param schemaLocationFileName
     */
    void assignSchema(final Document xmlDoc, final String fullyQualifiedClassName, final String schemaLocationFileName) {

        final String xsiSchemaLocationAttrValue = getUri() + " " + schemaLocationFileName;

        final Element rootElement = xmlDoc.getDocumentElement();

        // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        addNamespace(rootElement, XsMetaModel.W3_ORG_XSI_PREFIX, XsMetaModel.W3_ORG_XSI_URI);

        // xsi:schemaLocation="http://isis.apache.org/ns/app/<fully qualified
        // class name>
        // sdm.common.fixture.schemes.ao.communications
        // sdm.common.fixture.schemes.ao.communications.AO11ConfirmAnimalRegistration.xsd"
        rootElement.setAttributeNS(XsMetaModel.W3_ORG_XSI_URI, "xsi:schemaLocation", xsiSchemaLocationAttrValue);
    }

    /**
     * Adds a previously created &lt;xs:element&gt; element (representing a
     * field of an object) to the supplied element (presumed to be a
     * <code>complexType/sequence</code>).
     */
    void addFieldXsElement(final Element xsElement, final Element xsFieldElement) {
        if (xsFieldElement == null) {
            return;
        }
        final Element sequenceElement = xsMeta.sequenceForComplexTypeFor(xsElement);
        sequenceElement.appendChild(xsFieldElement);
    }

    /**
     * Adds a namespace using the supplied prefix and the supplied URI to the
     * root element of the document that is the parent of the supplied element.
     *
     * If the namespace declaration already exists but has a different URI
     * (shouldn't normally happen) overwrites with supplied URI.
     */
    private void addNamespace(final Element element, final String prefix, final String nsUri) {
        final Element rootElement = helper.rootElementFor(element);
        // see if we have the NS prefix there already
        final String existingNsUri = rootElement.getAttributeNS(XsMetaModel.W3_ORG_XMLNS_URI, prefix);
        // if there is none (or it is different from what we want), then set the
        // attribute
        if (existingNsUri == null || !existingNsUri.equals(nsUri)) {
            helper.rootElementFor(element).setAttributeNS(XsMetaModel.W3_ORG_XMLNS_URI, XsMetaModel.W3_ORG_XMLNS_PREFIX + ":" + prefix, nsUri);
        }
    }

    Element addXsElementIfNotPresent(final Element parentXsElement, final Element childXsElement) {

        final Element parentChoiceOrSequenceElement = xsMeta.choiceOrSequenceFor(xsMeta.complexTypeFor(parentXsElement));

        if (parentChoiceOrSequenceElement == null) {
            throw new IllegalArgumentException("Unable to locate complexType/sequence or complexType/choice under supplied parent XSD element");
        }

        final NamedNodeMap childXsElementAttributeMap = childXsElement.getAttributes();
        final Attr childXsElementAttr = (Attr) childXsElementAttributeMap.getNamedItem("name");
        final String localName = childXsElementAttr.getValue();

        final NodeList existingElements = parentChoiceOrSequenceElement.getElementsByTagNameNS("*", childXsElement.getLocalName());
        for (int i = 0; i < existingElements.getLength(); i++) {
            final Element xsElement = (Element) existingElements.item(i);
            final NamedNodeMap xsElementAttributeMap = xsElement.getAttributes();
            final Attr attr = (Attr) xsElementAttributeMap.getNamedItem("name");
            if (attr != null && attr.getValue().equals(localName)) {
                return xsElement;
            }
        }

        parentChoiceOrSequenceElement.appendChild(childXsElement);
        return childXsElement;
    }

}
