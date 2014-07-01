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

package org.apache.isis.objectstore.xml.internal.data.xml;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.xml.ContentWriter;
import org.apache.isis.core.commons.xml.XmlFile;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.xml.internal.data.CollectionData;
import org.apache.isis.objectstore.xml.internal.data.Data;
import org.apache.isis.objectstore.xml.internal.data.DataManager;
import org.apache.isis.objectstore.xml.internal.data.ListOfRootOid;
import org.apache.isis.objectstore.xml.internal.data.ObjectData;
import org.apache.isis.objectstore.xml.internal.data.ObjectDataVector;
import org.apache.isis.objectstore.xml.internal.data.PersistorException;
import org.apache.isis.objectstore.xml.internal.version.FileVersion;

public class XmlDataManager implements DataManager {
    private final XmlFile xmlFile;

    public XmlDataManager(final XmlFile xmlFile) {
        this.xmlFile = xmlFile;
    }

    // ////////////////////////////////////////////////////////
    // shutdown
    // ////////////////////////////////////////////////////////

    @Override
    public void shutdown() {
    }

    // ////////////////////////////////////////////////////////
    // SAX Handlers
    // ////////////////////////////////////////////////////////

    // TODO the following methods are being called repeatedly - is there no
    // caching? See the print statements
    private class DataHandler extends DefaultHandler {
        
        final StringBuilder data = new StringBuilder();
        CollectionData collection;
        String fieldName;
        ObjectData object;

        @Override
        public void characters(final char[] ch, final int start, final int end) throws SAXException {
            data.append(new String(ch, start, end));
        }

        @Override
        public void endElement(final String ns, final String name, final String tagName) throws SAXException {
            if (object != null) {
                if (tagName.equals("value")) {
                    final String value = data.toString();
                    object.set(fieldName, value);
                }
            }
        }

        @Override
        public void startElement(final String ns, final String name, final String tagName, final Attributes attributes) throws SAXException {
            if (object != null) {
                if (tagName.equals("value")) {
                    fieldName = attributes.getValue("field");
                    data.setLength(0);
                } else if (tagName.equals("association")) {
                    object.set(attributes.getValue("field"), oidFrom(attributes));
                } else if (tagName.equals("element")) {
                    object.addElement(fieldName, oidFrom(attributes));
                } else if (tagName.equals("multiple-association")) {
                    fieldName = attributes.getValue("field");
                    object.initCollection(fieldName);
                }
            } else if (collection != null) {
                if (tagName.equals("element")) {
                    
                    collection.addElement(oidFrom(attributes));
                }
            } else {
                if (tagName.equals("isis")) {
                    final RootOidDefault oid = oidFrom(attributes);
                    final Version fileVersion = fileVersionFrom(attributes);
                    
                    object = new ObjectData(oid, fileVersion);
                } else if (tagName.equals("collection")) {
                    
                    final RootOidDefault oid = oidFrom(attributes);
                    final Version fileVersion = fileVersionFrom(attributes);
                    
                    collection = new CollectionData(oid, fileVersion);
                } else {
                    throw new SAXException("Invalid data");
                }
            }
        }
    }

    private static RootOidDefault oidFrom(final Attributes attributes) {
        final String oid = attributes.getValue("oid");
        return RootOidDefault.deString(oid, getOidMarshaller());
    }

    private static Version fileVersionFrom(final Attributes attributes) {
        final String user = attributes.getValue("user");
        final long version = Long.valueOf(attributes.getValue("ver"), 16).longValue();
        final Version fileVersion = FileVersion.create(user, version);
        return fileVersion;
    }


    private class InstanceHandler extends DefaultHandler {
        Vector<RootOid> instances = new Vector<RootOid>();

        @Override
        public void characters(final char[] arg0, final int arg1, final int arg2) throws SAXException {
        }

        @Override
        public void startElement(final String ns, final String name, final String tagName, final Attributes attrs) throws SAXException {
            if (tagName.equals("instance")) {
                
                final String oidStr = attrs.getValue("oid");
                final RootOidDefault oid = RootOidDefault.deString(oidStr, getOidMarshaller());
                
                instances.addElement(oid);
            }
        }
    }

    private class NumberHandler extends DefaultHandler {
        boolean captureValue = false;
        long value = 0;

        @Override
        public void characters(final char[] arg0, final int arg1, final int arg2) throws SAXException {
            if (captureValue) {
                final String string = new String(arg0, arg1, arg2);
                value = Long.valueOf(string, 16).longValue();
            }
        }

        @Override
        public void startElement(final String ns, final String name, final String tagName, final Attributes attrs) throws SAXException {
            captureValue = tagName.equals("number");
        }

        public ContentWriter writer(final long nextId) {
            return new ContentWriter() {
                @Override
                public void write(final Writer writer) throws IOException {
                    writer.write("<number>");
                    final String nextIdHex = Long.toString(nextId, 16);
                    writer.append("" + nextIdHex);
                    writer.append("</number>");
                    writer.flush();
                }
            };
        }
        
        
    }

    // ////////////////////////////////////////////////////////
    // fixtures
    // ////////////////////////////////////////////////////////

    @Override
    public boolean isFixturesInstalled() {
        return xmlFile.isFixturesInstalled();
    }

    // ////////////////////////////////////////////////////////
    // loadData
    // ////////////////////////////////////////////////////////

    @Override
    public Data loadData(final RootOid oid) {
        final DataHandler handler = new DataHandler();
        xmlFile.parse(handler, filename(oid));

        if (handler.object != null) {
            return handler.object;
        } else {
            return handler.collection;
        }
    }

    // ////////////////////////////////////////////////////////
    // getInstances, numberOfInstances
    // ////////////////////////////////////////////////////////

    @Override
    public ObjectDataVector getInstances(final ObjectData pattern) {
        
        final Vector<RootOid> instances = loadInstances(pattern.getSpecification(getSpecificationLoader()));

        if (instances == null) {
            return new ObjectDataVector();
        }

        final ObjectDataVector matches = new ObjectDataVector();
        for (final RootOid oid : instances) {
            final ObjectData instanceData = (ObjectData) loadData(oid);
            // TODO check loader first
            if (instanceData == null) {
                throw new IsisException("No data found for " + oid + " (possible missing file)");
            }
            if (matchesPattern(pattern, instanceData)) {
                matches.addElement(instanceData);
            }
        }
        return matches;
    }

    @Override
    public int numberOfInstances(final ObjectData pattern) {
        return getInstances(pattern).size();
    }

    private Vector<RootOid> loadInstances(final ObjectSpecification noSpec) {
        final InstanceHandler handler = new InstanceHandler();
        parseSpecAndSubclasses(handler, noSpec);
        return handler.instances;
    }

    private void parseSpecAndSubclasses(final InstanceHandler handler, final ObjectSpecification noSpec) {
        parseIfNotAbstract(noSpec, handler);
        for (final ObjectSpecification subSpec : noSpec.subclasses()) {
            parseSpecAndSubclasses(handler, subSpec);
        }
    }

    private void parseIfNotAbstract(final ObjectSpecification noSpec, final InstanceHandler handler) {
        if (noSpec.isAbstract()) {
            return;
        }
        xmlFile.parse(handler, noSpec.getFullIdentifier());
    }

    /**
     * A helper that determines if two sets of data match. A match occurs when
     * the types are the same and any field in the pattern also occurs in the
     * data set under test.
     */
    // TODO we need to be able to find collection instances as well
    protected boolean matchesPattern(final ObjectData patternData, final ObjectData candidateData) {
        if (patternData == null || candidateData == null) {
            throw new NullPointerException("Can't match on nulls " + patternData + " " + candidateData);
        }

        if (!candidateData.getSpecification(getSpecificationLoader()).isOfType(patternData.getSpecification(getSpecificationLoader()))) {
            return false;
        }

        for (final String field : patternData.fields()) {
            final Object patternFieldValue = patternData.get(field);
            final Object candidateFieldValue = candidateData.get(field);

            if (candidateFieldValue instanceof ListOfRootOid) {
                final ListOfRootOid patternElements = (ListOfRootOid) patternFieldValue;
                for (int i = 0; i < patternElements.size(); i++) {
                    final RootOid requiredElement = patternElements.elementAt(i); // must have this element
                    boolean requiredFound = false;
                    final ListOfRootOid testElements = ((ListOfRootOid) candidateFieldValue);
                    for (int j = 0; j < testElements.size(); j++) {
                        if (requiredElement.equals(testElements.elementAt(j))) {
                            requiredFound = true;
                            break;
                        }
                    }
                    if (!requiredFound) {
                        return false;
                    }
                }
            } else {
                if (!patternFieldValue.equals(candidateFieldValue)) {
                    return false;
                }
            }

        }

        return true;
    }

    /**
     * Read in the next unique number for the object identifier.
     */
    protected long nextId() throws PersistorException {
        final NumberHandler handler = new NumberHandler();
        xmlFile.parse(handler, "oid");

        final long nextId = handler.value + 1;
        xmlFile.writeXml("oid", handler.writer(nextId));

        return nextId;
    }

    // ////////////////////////////////////////////////////////
    // insertObject, remove
    // ////////////////////////////////////////////////////////

    /**
     * Save the data for an object and adds the reference to a list of instances
     */
    @Override
    public final void insertObject(final ObjectData data) {
        if (data.getRootOid() == null) {
            throw new IllegalArgumentException("Oid must be non-null");
        }

        writeInstanceToItsDataFile(data);
        final ObjectSpecification objSpec = data.getSpecification(getSpecificationLoader());
        addReferenceToInstancesFile(data.getRootOid(), objSpec);
    }

    private void addReferenceToInstancesFile(final RootOid oid, final ObjectSpecification noSpec) {
        final Vector<RootOid> instances = loadInstances(noSpec);
        instances.addElement(oid);
        writeInstanceFile(noSpec, instances);
    }

    // ////////////////////////////////////////////////////////
    // remove
    // ////////////////////////////////////////////////////////

    @Override
    public final void remove(final RootOid oid) throws ObjectNotFoundException, ObjectPersistenceException {
        final Data data = loadData(oid);
        removeReferenceFromInstancesFile(oid, data.getSpecification(getSpecificationLoader()));
        deleteData(oid);
    }

    /**
     * Delete the data for an existing instance.
     */
    private void deleteData(final RootOid oid) {
        xmlFile.delete(filename(oid));
    }

    private void removeReferenceFromInstancesFile(final RootOid oid, final ObjectSpecification noSpec) {
        final Vector<RootOid> instances = loadInstances(noSpec);
        instances.removeElement(oid);
        writeInstanceFile(noSpec, instances);
    }

    // ////////////////////////////////////////////////////////
    // helpers (used by both add & remove)
    // ////////////////////////////////////////////////////////

    private void writeInstanceFile(final ObjectSpecification noSpec, final Vector<RootOid> instances) {
        writeInstanceFile(noSpec.getFullIdentifier(), instances);
    }

    private void writeInstanceFile(final String name, final Vector<RootOid> instances) {
        xmlFile.writeXml(name, new ContentWriter() {
            @Override
            public void write(final Writer writer) throws IOException {
                writer.write("<instances");
                Utils.appendAttribute(writer, "name", name);
                writer.append(">\n");

                for (final RootOid elementAt : instances) {
                    writer.append("  <instance");
                    Utils.appendAttribute(writer, "oid", elementAt.enString(getOidMarshaller()));
                    writer.append("/>\n");
                }
                writer.append("</instances>");
                writer.flush();
            }
        });
    }

    // ////////////////////////////////////////////////////////
    // save
    // ////////////////////////////////////////////////////////

    /**
     * Save the data for latter retrieval.
     */
    @Override
    public final void save(final Data data) {
        writeInstanceToItsDataFile(data);
    }

    private void writeInstanceToItsDataFile(final Data data) {
        xmlFile.writeXml(filename(data.getRootOid()), new ContentWriter() {
            @Override
            public void write(final Writer writer) throws IOException {
                final boolean isObject = data instanceof ObjectData;
                final String tag = isObject ? "isis" : "collection";
                writer.write("<");
                writer.write(tag);
                final RootOid oid = data.getRootOid();
                Utils.appendAttribute(writer, "oid", oid.enString(getOidMarshaller()));
                Utils.appendAttribute(writer, "user", "" + getAuthenticationSession().getUserName());

                final long sequence = data.getVersion().getSequence();
                final String sequenceString = Long.toHexString(sequence).toUpperCase();
                Utils.appendAttribute(writer, "ver", "" + sequenceString);

                writer.append(">\n");

                if (isObject) {
                    writeObject(data, writer);
                } else {
                    writeCollection(data, writer);
                }

                writer.append("</" + tag + ">\n");
                writer.flush();
            }

        });
    }

    private void writeObject(final Data data, final Writer writer) throws IOException {
        final ObjectData object = (ObjectData) data;
        for (final String field : object.fields()) {
            writeField(writer, object, field);
        }
    }

    private void writeField(final Writer writer, final ObjectData object, final String field) throws IOException {
        final Object entry = object.get(field);

        if (entry instanceof RootOidDefault) {
            writeAssociationField(writer, field, entry);
        } else if (entry instanceof ListOfRootOid) {
            writeMultipleAssociationField(writer, field, entry);
        } else {
            writeValueField(writer, field, entry);
        }
    }

    private void writeAssociationField(final Writer writer, final String field, final Object entry) throws IOException {
        final Oid rootOidDefault = (Oid)entry;
        Assert.assertFalse(rootOidDefault.isTransient());
        writer.append("  <association");
        Utils.appendAttribute(writer, "field", field);
        Utils.appendAttribute(writer, "oid", rootOidDefault.enString(getOidMarshaller()));
        writer.append("/>\n");
    }

    private void writeMultipleAssociationField(final Writer writer, final String field, final Object entry) throws IOException {
        final ListOfRootOid references = (ListOfRootOid) entry;
        final int size = references.size();

        if (size > 0) {
            writer.append("  <multiple-association field=\"" + field + "\" ");
            writer.append(">\n");
            for (int i = 0; i < size; i++) {
                final Object oid = references.elementAt(i);
                final RootOidDefault rootOidDefault = (RootOidDefault) oid;
                if (rootOidDefault.isTransient()) {
                    throw new ObjectPersistenceException("Can't add tranisent OID (" + oid + ") to " + field + " element.");
                }
                writer.append("    <element ");
                Utils.appendAttribute(writer, "oid", rootOidDefault.enString(getOidMarshaller()));
                writer.append("/>\n");
            }
            writer.append("  </multiple-association>\n");
            writer.flush();
        }
    }

    private static void writeValueField(final Writer writer, final String field, final Object entry) throws IOException {
        writer.append("  <value");
        Utils.appendAttribute(writer, "field", field);
        writer.append(">");
        writer.append(XmlFile.getValueWithSpecialsEscaped(entry.toString()));
        writer.append("</value>\n");
    }

    private static void writeCollection(final Data data, final Writer writer) throws IOException {
        final CollectionData collection = (CollectionData) data;
        final ListOfRootOid refs = collection.references();
        for (int i = 0; i < refs.size(); i++) {
            final Object oid = refs.elementAt(i);
            writer.append("  <element");
            final RootOid rootOid = (RootOid) oid;
            Utils.appendAttribute(writer, "oid", rootOid.enString(getOidMarshaller()));
            writer.append("/>\n");
        }
    }

    
    private static String filename(final RootOid oid) {
        return oid.getObjectSpecId() + File.separator + oid.getIdentifier();
    }



    // ////////////////////////////////////////////////////////
    // Debugging
    // ////////////////////////////////////////////////////////

    @Override
    public String getDebugData() {
        return "Data directory " + xmlFile.getDirectory();
    }

    
    // ////////////////////////////////////////////////////////
    // dependencies (from context)
    // ////////////////////////////////////////////////////////

    protected static OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    
}
