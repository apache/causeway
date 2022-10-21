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
package org.apache.causeway.core.metamodel.util.snapshot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * Utility methods relating to the Causeway meta model.
 */
final class CausewaySchema {

    /**
     * The generated XML schema references the <i>Apache Causeway</i> metamodel schema. This is the
     * default location for this schema.
     */
    public static final String DEFAULT_LOCATION = "causeway.xsd";
    /**
     * The base of the namespace URI to use for application namespaces if none
     * explicitly supplied in the constructor.
     */
    public static final String DEFAULT_URI_BASE = "http://causeway.apache.org/ns/app/";

    /**
     * Enumeration of causeway:feature attribute representing a class
     */
    public static final String FEATURE_CLASS = "class";
    /**
     * Enumeration of causeway:feature attribute representing a collection (1:n
     * association)
     */
    public static final String FEATURE_COLLECTION = "collection";
    /**
     * Enumeration of causeway:feature attribute representing a reference (1:1
     * association)
     */
    public static final String FEATURE_REFERENCE = "reference";
    /**
     * Enumeration of causeway:feature attribute representing a value field
     */
    public static final String FEATURE_VALUE = "value";
    /**
     * Namespace prefix for {@link #NS_URI}.
     *
     * The NamespaceManager will not allow any namespace to use this prefix.
     */
    public static final String NS_PREFIX = "causeway";
    /**
     * URI representing the namespace of ObjectAdapter framework's metamodel.
     *
     * The NamespaceManager will not allow any namespaces with this URI to be
     * added.
     */
    public static final String NS_URI = "http://causeway.apache.org/ns/0.1/metamodel";

    private final Helper helper;

    public CausewaySchema() {
        this.helper = new Helper();
    }

    void addNamespace(final Element element) {
        helper.rootElementFor(element).setAttributeNS(XsMetaModel.W3_ORG_XMLNS_URI, XsMetaModel.W3_ORG_XMLNS_PREFIX + ":" + CausewaySchema.NS_PREFIX, CausewaySchema.NS_URI);
    }

    /**
     * Creates an element in the &quot;causeway&quot; namespace, appends to parent, and adds &quot;causeway&quot;
     * namespace to the root element if required.
     */
    Element appendElement(final Element parentElement, final String localName) {
        final Element element = helper.docFor(parentElement).createElementNS(CausewaySchema.NS_URI, CausewaySchema.NS_PREFIX + ":" + localName);
        parentElement.appendChild(element);
        // addNamespace(parentElement);
        return element;
    }

    /**
     * Appends an <code>causeway:title</code> element with the supplied title string
     * to the provided element.
     */
    public void appendCausewayTitle(final Element element, final String titleStr) {
        final Document doc = helper.docFor(element);
        final Element titleElement = appendElement(element, "title");
        titleElement.appendChild(doc.createTextNode(titleStr));
    }

    /**
     * Gets an attribute with the supplied name in the Causeway namespace from the
     * supplied element
     */
    String getAttribute(final Element element, final String attributeName) {
        return element.getAttributeNS(CausewaySchema.NS_URI, attributeName);
    }

    /**
     * Adds an <code>causeway:annotation</code> attribute for the supplied class to
     * the supplied element.
     */
    void setAnnotationAttribute(final Element element, final String annotation) {
        setAttribute(element, "annotation", CausewaySchema.NS_PREFIX + ":" + annotation);
    }

    /**
     * Sets an attribute of the supplied element with the attribute being in the
     * Causeway namespace.
     */
    private void setAttribute(final Element element, final String attributeName, final String attributeValue) {
        element.setAttributeNS(CausewaySchema.NS_URI, CausewaySchema.NS_PREFIX + ":" + attributeName, attributeValue);
    }

    /**
     * Adds <code>causeway:feature=&quot;class&quot;</code> attribute and
     * <code>causeway:oid=&quote;...&quot;</code> for the supplied element.
     */
    void setAttributesForClass(final Element element, final String oid) {
        setAttribute(element, "feature", FEATURE_CLASS);
        setAttribute(element, "oid", oid);
    }

    /**
     * Adds <code>causeway:feature=&quot;reference&quot;</code> attribute and
     * <code>causeway:type=&quote;...&quot;</code> for the supplied element.
     */
    void setAttributesForReference(final Element element, final String prefix, final String fullyQualifiedClassName) {
        setAttribute(element, "feature", FEATURE_REFERENCE);
        setAttribute(element, "type", prefix + ":" + fullyQualifiedClassName);
    }

    /**
     * Adds <code>causeway:feature=&quot;value&quot;</code> attribute and
     * <code>causeway:datatype=&quote;...&quot;</code> for the supplied element.
     */
    void setAttributesForValue(final Element element, final String datatypeName) {
        setAttribute(element, "feature", FEATURE_VALUE);
        setAttribute(element, "datatype", CausewaySchema.NS_PREFIX + ":" + datatypeName);
    }

    /**
     * Adds an <code>causeway:isEmpty</code> attribute for the supplied class to the
     * supplied element.
     */
    void setIsEmptyAttribute(final Element element, final boolean isEmpty) {
        setAttribute(element, "isEmpty", "" + isEmpty);
    }

    /**
     * Adds <code>causeway:feature=&quot;collection&quot;</code> attribute, the
     * <code>causeway:type=&quote;...&quot;</code> and the
     * <code>causeway:size=&quote;...&quot;</code> for the supplied element.
     *
     * Additionally, if the <code>addOids</code> parameter is set, also adds
     * <code>&lt;oids&gt;</code> child elements.
     */
    void setCausewayCollection(final Element element, final String prefix, final String fullyQualifiedClassName, final ManagedObject collection) {
        setAttribute(element, "feature", FEATURE_COLLECTION);
        setAttribute(element, "type", prefix + ":" + fullyQualifiedClassName);
        setAttribute(element, "size", "" + CollectionFacet.elementCount(collection));
    }

}
