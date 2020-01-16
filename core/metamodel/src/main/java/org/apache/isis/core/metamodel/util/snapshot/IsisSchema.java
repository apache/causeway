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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Utility methods relating to the Isis meta model.
 */
final class IsisSchema {

    /**
     * The generated XML schema references the NOF metamodel schema. This is the
     * default location for this schema.
     */
    public static final String DEFAULT_LOCATION = "isis.xsd";
    /**
     * The base of the namespace URI to use for application namespaces if none
     * explicitly supplied in the constructor.
     */
    public final static String DEFAULT_URI_BASE = "http://isis.apache.org/ns/app/";

    /**
     * Enumeration of isis:feature attribute representing a class
     */
    public static final String FEATURE_CLASS = "class";
    /**
     * Enumeration of isis:feature attribute representing a collection (1:n
     * association)
     */
    public static final String FEATURE_COLLECTION = "collection";
    /**
     * Enumeration of isis:feature attribute representing a reference (1:1
     * association)
     */
    public static final String FEATURE_REFERENCE = "reference";
    /**
     * Enumeration of isis:feature attribute representing a value field
     */
    public static final String FEATURE_VALUE = "value";
    /**
     * Namespace prefix for {@link NS_URI}.
     *
     * The NamespaceManager will not allow any namespace to use this prefix.
     */
    public static final String NS_PREFIX = "isis";
    /**
     * URI representing the namespace of ObjectAdapter framework's metamodel.
     *
     * The NamespaceManager will not allow any namespaces with this URI to be
     * added.
     */
    public static final String NS_URI = "http://isis.apache.org/ns/0.1/metamodel";

    private final Helper helper;

    public IsisSchema() {
        this.helper = new Helper();
    }

    void addNamespace(final Element element) {
        helper.rootElementFor(element).setAttributeNS(XsMetaModel.W3_ORG_XMLNS_URI, XsMetaModel.W3_ORG_XMLNS_PREFIX + ":" + IsisSchema.NS_PREFIX, IsisSchema.NS_URI);
    }

    /**
     * Creates an element in the &quot;isis&quot; namespace, appends to parent, and adds &quot;isis&quot;
     * namespace to the root element if required.
     */
    Element appendElement(final Element parentElement, final String localName) {
        final Element element = helper.docFor(parentElement).createElementNS(IsisSchema.NS_URI, IsisSchema.NS_PREFIX + ":" + localName);
        parentElement.appendChild(element);
        // addNamespace(parentElement);
        return element;
    }

    /**
     * Appends an <code>isis:title</code> element with the supplied title string
     * to the provided element.
     */
    public void appendIsisTitle(final Element element, final String titleStr) {
        final Document doc = helper.docFor(element);
        final Element titleElement = appendElement(element, "title");
        titleElement.appendChild(doc.createTextNode(titleStr));
    }

    /**
     * Gets an attribute with the supplied name in the Isis namespace from the
     * supplied element
     */
    String getAttribute(final Element element, final String attributeName) {
        return element.getAttributeNS(IsisSchema.NS_URI, attributeName);
    }

    /**
     * Adds an <code>isis:annotation</code> attribute for the supplied class to
     * the supplied element.
     */
    void setAnnotationAttribute(final Element element, final String annotation) {
        setAttribute(element, "annotation", IsisSchema.NS_PREFIX + ":" + annotation);
    }

    /**
     * Sets an attribute of the supplied element with the attribute being in the
     * Isis namespace.
     */
    private void setAttribute(final Element element, final String attributeName, final String attributeValue) {
        element.setAttributeNS(IsisSchema.NS_URI, IsisSchema.NS_PREFIX + ":" + attributeName, attributeValue);
    }

    /**
     * Adds <code>isis:feature=&quot;class&quot;</code> attribute and
     * <code>isis:oid=&quote;...&quot;</code> for the supplied element.
     */
    void setAttributesForClass(final Element element, final String oid) {
        setAttribute(element, "feature", FEATURE_CLASS);
        setAttribute(element, "oid", oid);
    }

    /**
     * Adds <code>isis:feature=&quot;reference&quot;</code> attribute and
     * <code>isis:type=&quote;...&quot;</code> for the supplied element.
     */
    void setAttributesForReference(final Element element, final String prefix, final String fullyQualifiedClassName) {
        setAttribute(element, "feature", FEATURE_REFERENCE);
        setAttribute(element, "type", prefix + ":" + fullyQualifiedClassName);
    }

    /**
     * Adds <code>isis:feature=&quot;value&quot;</code> attribute and
     * <code>isis:datatype=&quote;...&quot;</code> for the supplied element.
     */
    void setAttributesForValue(final Element element, final String datatypeName) {
        setAttribute(element, "feature", FEATURE_VALUE);
        setAttribute(element, "datatype", IsisSchema.NS_PREFIX + ":" + datatypeName);
    }

    /**
     * Adds an <code>isis:isEmpty</code> attribute for the supplied class to the
     * supplied element.
     */
    void setIsEmptyAttribute(final Element element, final boolean isEmpty) {
        setAttribute(element, "isEmpty", "" + isEmpty);
    }

    /**
     * Adds <code>isis:feature=&quot;collection&quot;</code> attribute, the
     * <code>isis:type=&quote;...&quot;</code> and the
     * <code>isis:size=&quote;...&quot;</code> for the supplied element.
     *
     * Additionally, if the <code>addOids</code> parameter is set, also adds
     * <code>&lt;oids&gt;</code> child elements.
     */
    void setIsisCollection(final Element element, final String prefix, final String fullyQualifiedClassName, final ManagedObject collection) {
        setAttribute(element, "feature", FEATURE_COLLECTION);
        setAttribute(element, "type", prefix + ":" + fullyQualifiedClassName);
        setAttribute(element, "size", "" + CollectionFacet.elementCount(collection));
    }

}
