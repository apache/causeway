package org.nakedobjects.persistence.file;

import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.persistence.SerialOid;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


public class XmlDataManager implements DataManager {
    public static String directory() {
        return NakedObjects.getConfiguration().getString("xml-object-store.directory", "xml");
    }

    public String getDebugData() {
        return "Data directory " + directory();
    }

    // TODO the following methods are being called repeatedly - is there no
    // caching? See the print statemens
    private class DataHandler extends DefaultHandler {
        CollectionData collection;
        StringBuffer data = new StringBuffer();
        String fieldName;
        ObjectData object;

        public void characters(char[] ch, int start, int end) throws SAXException {
            data.append(new String(ch, start, end));
            //	System.out.println("XML DataHandler " + data);
        }

        public void endElement(String ns, String name, String tagName) throws SAXException {
            if (object != null) {
                if (tagName.equals("value")) {
                    String value = data.toString();
                    object.set(fieldName, value);
                    //		System.out.println("XML DataHandler " + data);
                }
            }
        }

        public void startElement(String ns, String name, String tagName, Attributes attrs) throws SAXException {
            if (object != null) {
                if (tagName.equals("value")) {
                    fieldName = attrs.getValue("field");
                    data.setLength(0);
                    //		System.out.println("XML DataHandler" + fieldName);
                } else if (tagName.equals("association")) {
                    String fieldName = attrs.getValue("field");
                    //                    String type = attrs.getValue("type");
                    long id = Long.valueOf(attrs.getValue("ref"), 16).longValue();
                    object.set(fieldName, new SerialOid(id));
                } else if (tagName.equals("element")) {
                    //                    String type = attrs.getValue("type");
                    long id = Long.valueOf(attrs.getValue("ref"), 16).longValue();
                    object.addElement(fieldName, new SerialOid(id));
                } else if (tagName.equals("multiple-association")) {
                    fieldName = attrs.getValue("field");
                    //               	long id = Long.valueOf(attrs.getValue("ref"),
                    // 16).longValue();
                    SerialOid internalCollection = null; //new
                    // SerialOid(id);
                    object.initCollection(internalCollection, fieldName);
                }
            } else if (collection != null) {
                if (tagName.equals("element")) {
                    //                    String type = attrs.getValue("type");
                    long id = Long.valueOf(attrs.getValue("ref"), 16).longValue();
                    collection.addElement(new SerialOid(id));
                }
            } else {
                if (tagName.equals("naked-object")) {
                    String type = attrs.getValue("type");
                    long id = Long.valueOf(attrs.getValue("id"), 16).longValue();
                    object = new ObjectData(NakedObjects.getSpecificationLoader().loadSpecification(type), new SerialOid(
                            id));
                } else if (tagName.equals("collection")) {
                    String type = attrs.getValue("type");
                    long id = Long.valueOf(attrs.getValue("id"), 16).longValue();
                    collection = new CollectionData(NakedObjects.getSpecificationLoader().loadSpecification(type),
                            new SerialOid(id));
                } else {
                    throw new SAXException("Invalid data");
                }
            }
        }
    }

    private class InstanceHandler extends DefaultHandler {
        Vector instances = new Vector();

        public void characters(char[] arg0, int arg1, int arg2) throws SAXException {}

        public void startElement(String ns, String name, String tagName, Attributes attrs) throws SAXException {
            if (tagName.equals("instance")) {
                long oid = Long.valueOf(attrs.getValue("id"), 16).longValue();
                instances.addElement(new SerialOid(oid));
            }
        }
    }

    private class NumberHandler extends DefaultHandler {
        boolean captureValue = false;
        long value = 0;

        public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
            if (captureValue) {
                value = Long.valueOf(new String(arg0, arg1, arg2), 16).longValue();
            }
        }

        public void startElement(String ns, String name, String tagName, Attributes attrs) throws SAXException {
            captureValue = tagName.equals("number");
        }
    }

    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final String ENCODING_PROPERTY = "xml-object-store.encoding";
    private static final String[] escapeString = { "&amp;", "&lt;", "&gt;", "&quot;", "&apos;" };

    private static final String[] specialChars = { "&", "<", ">", "\"", "'" };

    protected static void clearTestDirectory() {
        File directory = new File("tmp" + File.separator + "tests");
        String[] files = directory.list(new FilenameFilter() {
            public boolean accept(File arg0, String name) {
                return name.endsWith(".xml");
            }
        });

        if (files != null) {
            for (int f = 0; f < files.length; f++) {
                new File(directory, files[f]).delete();
            }
        }
    }

    public static String getValueWithSpecialsEscaped(String s) {
        String result = s;
        for (int i = 0; i < specialChars.length; i++) {
            String special = specialChars[i];
            int pos = -1;
            while (true) {
                pos = result.indexOf(special, pos + 1);
                if (pos < 0)
                    break;
                result = result.substring(0, pos) + escapeString[i] + result.substring(pos + special.length());
            }
        }
        return result;
    }

    private String charset;
    private File directory;

    public XmlDataManager() {
        this(directory());
    }

    public XmlDataManager(String directory) {
        this.directory = new File(directory);

        if (!this.directory.exists()) {
            this.directory.mkdirs();
        }

        charset = NakedObjects.getConfiguration().getString(ENCODING_PROPERTY, DEFAULT_ENCODING);
    }

    /**
     * Write out the data for a new instance.
     */
    protected void addData(SerialOid oid, String type, Data data) throws ObjectPerstsistenceException {
        writeData(oid, data);
    }

    /**
     * Add the reference for an instance to the list of all instances.
     */
    protected void addInstance(SerialOid oid, String type) throws ObjectPerstsistenceException {
        Vector instances = loadInstances(type);
        instances.addElement(oid);
        writeInstanceFile(type, instances);
    }

    private String attribute(String name, String value) {
        return " " + name + "=\"" + value + "\"";
    }

    public final SerialOid createOid() throws PersistorException {
        return new SerialOid(nextId());
    }

    /**
     * Delete the data for an existing instance.
     */
    protected void deleteData(SerialOid oid, String type) {
        file(filename(oid)).delete();
    }

    private String encodedOid(SerialOid oid) {
        return Long.toHexString(oid.getSerialNo()).toUpperCase();
    }

    private File file(String fileName) {
        return new File(directory, fileName + ".xml");
    }

    private String filename(SerialOid oid) {
        return encodedOid(oid);
    }

    
    public ObjectDataVector getInstances(ObjectData pattern) {
        Vector instances = loadInstances(pattern.getClassName());

        if (instances == null) {
            return new ObjectDataVector();
        }

        ObjectDataVector matches = new ObjectDataVector();
        for (int i = 0; i < instances.size(); i++) {
            SerialOid oid = (SerialOid) instances.elementAt(i);
            ObjectData instanceData = (ObjectData) loadData(oid);
            if (instanceData == null) {
                throw new NakedObjectRuntimeException("No data found for " + oid + " (possible missing file)");
            }
            if (matchesPattern(pattern, instanceData)) {
                matches.addElement(instanceData);
            }
        }
        return matches;
    }

    public void getNakedClass(String name) {}

    /**
     * Save the data for an object and adds the reference to a list of instances
     */
    public final void insert(Data data) throws ObjectPerstsistenceException {
        if (data.getOid() == null) {
            throw new IllegalArgumentException("Oid must be non-null");
        }

        String type = data.getClassName();
        SerialOid oid = data.getOid();
        addData(oid, type, data);
        addInstance(oid, type);
    }

    /**
     * Loads in data for a collection for the specified identifier.
     */
    public final CollectionData loadCollectionData(SerialOid oid) {
        return (CollectionData) loadData(oid);
    }

    public Data loadData(SerialOid oid) {
        DataHandler handler = new DataHandler();
        parse(handler, filename(oid));

        if (handler.object != null) {
            return handler.object;
        } else {
            return handler.collection;
        }
    }

    private Vector loadInstances(String type) {
        InstanceHandler handler = new InstanceHandler();
        parse(handler, type);

        return handler.instances;
    }

    /**
     * Loads in data for an object for the specified identifier.
     */
    public final ObjectData loadObjectData(SerialOid oid) {
        return (ObjectData) loadData(oid);
    }

    /**
     * A helper that determines if two sets of data match. A match occurs when
     * the types are the same and any field in the pattern also occurs in the
     * data set under test.
     */
    // TODO we need to be able to find collection instances as well
    protected boolean matchesPattern(ObjectData patternData, ObjectData testData) {
        if (patternData == null || testData == null) {
            throw new NullPointerException("Can't match on nulls " + patternData + " " + testData);
        }
        if (!patternData.getClassName().equals(testData.getClassName())) {
            return false;
        }

        Enumeration fields = patternData.fields();

        while (fields.hasMoreElements()) {
            String field = (String) fields.nextElement();
            Object patternFieldValue = patternData.get(field);

            Object testFieldValue = testData.get(field);

            if (testFieldValue instanceof ReferenceVector) {
                ReferenceVector patternElements = (ReferenceVector) patternFieldValue;
                for (int i = 0; i < patternElements.size(); i++) {
                    SerialOid requiredElement = patternElements.elementAt(i); // must
                    // have
                    // this
                    // element
                    boolean requiredFound = false;
                    ReferenceVector testElements = ((ReferenceVector) testFieldValue);
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
                if (!patternFieldValue.equals(testFieldValue)) {
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
        NumberHandler handler = new NumberHandler();
        parse(handler, "oid");

        StringBuffer data = new StringBuffer();
        data.append("<number>");
        data.append(handler.value + 1);
        data.append("</number>");
        writeXml("oid", data);

        return handler.value + 1;
    }

    public int numberOfInstances(ObjectData pattern) {
        Vector instances = loadInstances(pattern.getClassName());

        if (instances == null) {
            return 0;
        }

        int instanceCount = 0;
        for (int i = 0; i < instances.size(); i++) {
            SerialOid oid = (SerialOid) instances.elementAt(i);
            ObjectData instanceData = (ObjectData) loadData(oid);
            if (instanceData != null && matchesPattern(pattern, instanceData)) {
                instanceCount++;
            }
        }
        return instanceCount;
    }

    private boolean parse(ContentHandler handler, String fileName) {
        XMLReader parser;

        try {
            parser = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            try {
                parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            } catch (SAXException e2) {
                try {
                    parser = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
                } catch (SAXException failed) {
                    throw new NakedObjectRuntimeException("Couldn't locate a SAX parser");
                }
            }
        }

        try {
            parser.setContentHandler(handler);
            parser.parse(new InputSource(new InputStreamReader(new FileInputStream(file(fileName)), charset)));

            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            throw new NakedObjectRuntimeException("Error reading XML file", e);
        } catch (SAXParseException e) {
            throw new NakedObjectRuntimeException("Error while parsing: " + e.getMessage() + " in " + file(fileName) + ")", e);
        } catch (SAXException e) {
            throw new NakedObjectRuntimeException("?? Error parsing XML file " + file(fileName) + " " + e.getClass(), e
                    .getException());
        }
    }

    public final void remove(SerialOid oid) throws ObjectNotFoundException, ObjectPerstsistenceException {
        Data data = loadData(oid);
        String type = data.getClassName();
        removeInstance(oid, type);
        deleteData(oid, type);
    }

    /**
     * Remove the reference for an instance from the list of all instances.
     */
    protected void removeInstance(SerialOid oid, String type) throws ObjectPerstsistenceException {
        Vector instances = loadInstances(type);
        instances.removeElement(oid);
        writeInstanceFile(type, instances);
    }

    /**
     * Save the data for latter retrieval.
     */
    public final void save(Data data) throws ObjectPerstsistenceException {
        writeData(data.getOid(), data);
    }

    public void shutdown() {}

    private void writeData(SerialOid xoid, Data data) throws ObjectPerstsistenceException {
        StringBuffer xml = new StringBuffer();
        boolean isObject = data instanceof ObjectData;
        String tag = isObject ? "naked-object" : "collection";
        xml.append("<" + tag);
        xml.append(attribute("type", data.getClassName()));
        xml.append(attribute("id", "" + encodedOid(data.getOid())));
        xml.append(">\n");

        if (isObject) {
            ObjectData object = (ObjectData) data;
            Enumeration fields = object.fields();

            while (fields.hasMoreElements()) {
                String field = (String) fields.nextElement();
                Object entry = object.get(field);

                if (entry instanceof SerialOid) {
                    xml.append("  <association field=\"" + field + "\" ");
                    xml.append("ref=\"" + encodedOid((SerialOid) entry) + "\"/>\n");
                } else if (entry instanceof ReferenceVector) {
                    ReferenceVector references = (ReferenceVector) entry;
                    int size = references.size();

                    if(size > 0) {
                        xml.append("  <multiple-association field=\"" + field + "\" ");
                        xml.append(">\n");
                        for (int i = 0; i < size; i++) {
                            Object oid = references.elementAt(i);
                            xml.append("    <element ");
                            xml.append("ref=\"" + encodedOid((SerialOid) oid) + "\"/>\n");
                        }
                        xml.append("  </multiple-association>\n");
                    }
                } else {
                    xml.append("  <value field=\"" + field + "\">");
                    xml.append(getValueWithSpecialsEscaped(entry.toString()));
                    xml.append("</value>\n");
                }
            }
        } else {
            CollectionData collection = (CollectionData) data;
            ReferenceVector refs = collection.references();
            for (int i = 0; i < refs.size(); i++) {
                Object oid = refs.elementAt(i);
                xml.append("  <element ");
                xml.append("ref=\"" + encodedOid((SerialOid) oid) + "\"/>\n");
            }
        }

        xml.append("</" + tag + ">\n");
        writeXml(filename(xoid), xml);
    }

    private void writeInstanceFile(String name, Vector instances) throws ObjectPerstsistenceException {
        StringBuffer data = new StringBuffer();
        data.append("<instances name=\"" + name + "\">\n");

        for (int i = 0; i < instances.size(); i++) {
            data.append("  <instance id=\"" + encodedOid((SerialOid) instances.elementAt(i)) + "\"/>\n");
        }

        data.append("</instances>");
        writeXml(name, data);
    }

    private void writeXml(String name, StringBuffer buf) {
        OutputStreamWriter pw;

        try {
            pw = new OutputStreamWriter(new FileOutputStream(file(name)), charset);
            pw.write("<?xml version=\"1.0\" encoding=\"" + charset + "\" ?>\n");
            pw.write("\n");
            pw.write(buf.toString());
            pw.write("\n");
            pw.close();
        } catch (IOException e) {
            throw new NakedObjectRuntimeException("Problems writing data files", e);
        }
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
