
package org.nakedobjects.persistence.file;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.defaults.SerialOid;

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


public class XmlDataManager extends DataManager {
	public static final String DEFAULT_ENCODING = "ISO-8859-1";
	private static final String ENCODING_PROPERTY = "xml-object-store.encoding";
	private String charset;
    private File directory;

    public XmlDataManager() {
        this("data");
    }

    public XmlDataManager(String directory) {
        this.directory = new File(directory);

        if (!this.directory.exists()) {
            this.directory.mkdirs();
        }
        
		charset = Configuration.getInstance().getString(ENCODING_PROPERTY, DEFAULT_ENCODING);
    }

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

    protected void deleteData(SerialOid oid, String type) {
        file(filename(oid)).delete();
    }

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

    protected Data loadData(SerialOid oid) {
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

    private void writeData(SerialOid xoid, Data data) throws ObjectStoreException {
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
                	xml.append("ref=\"" + encodedOid((SerialOid)entry) + "\"/>\n");
                } else if (entry instanceof ReferenceVector) {
                	ReferenceVector references = (ReferenceVector) entry;
                	xml.append("  <multiple-association field=\"" + field + "\" ");
                	xml.append("ref=\"" + encodedOid(references.getOid()) + "\">\n");
                	
                	for (int i = 0; i < references.size(); i++) {
                		Object oid = references.elementAt(i);
                		xml.append("    <element ");
                		xml.append("ref=\"" +encodedOid((SerialOid)oid)  + "\"/>\n");
                	}

                	xml.append("  </multiple-association>\n");
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
                xml.append("ref=\"" + encodedOid((SerialOid)oid) + "\"/>\n");
            }
        }

        xml.append("</" + tag + ">\n");
        writeXml(filename(xoid), xml);
    }

	private String filename(SerialOid oid) {
		return encodedOid(oid);
	}

	private String encodedOid(SerialOid oid) {
		return Long.toHexString(oid.getSerialNo()).toUpperCase();
	}

	private static final String[] specialChars = {"&","<",">","\"","'"};
	private static final String[] escapeString = {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};

	public static String getValueWithSpecialsEscaped(String s) {
		String result = s;
		for (int i = 0; i < specialChars.length; i++) {
			String special = specialChars[i];
			int pos = -1;
			while (true) {
				pos = result.indexOf(special,pos+1);
				if (pos < 0)
					break;
				result = result.substring(0,pos)+escapeString[i]+result.substring(pos+special.length());
			}
		}
		return result;
	}

    private void writeInstanceFile(String name, Vector instances)
        throws ObjectStoreException {
        StringBuffer data = new StringBuffer();
        data.append("<instances name=\"" + name + "\">\n");

        for (int i = 0; i < instances.size(); i++) {
            data.append("  <instance id=\"" + encodedOid((SerialOid) instances.elementAt(i)) + "\"/>\n");
        }

        data.append("</instances>");
        writeXml(name, data);
    }

    private String attribute(String name, String value) {
        return " " + name + "=\"" + value + "\"";
    }

    private File file(String fileName) {
        return new File(directory, fileName + ".xml");
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
            throw new NakedObjectRuntimeException("Parse error: " + e.getMessage() + " (" +
                file(fileName) + ")");
        } catch (SAXException e) {
            throw new NakedObjectRuntimeException("?? Error parsing XML file " + file(fileName) + " " +
                e.getClass(), e.getException());
        }
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

    private class NumberHandler extends DefaultHandler {
        boolean captureValue = false;
        long value = 0;

        public void characters(char[] arg0, int arg1, int arg2)
            throws SAXException {
            if (captureValue) {
                value = Long.valueOf(new String(arg0, arg1, arg2), 16).longValue();
            }
        }

        public void startElement(String ns, String name, String tagName, Attributes attrs)
            throws SAXException {
            captureValue = tagName.equals("number");
        }
    }

	// TODO the following methods are being called repeatedly - is there no caching?  See the print statemens
    private class DataHandler extends DefaultHandler {
        CollectionData collection;
        ObjectData object;
		StringBuffer data = new StringBuffer();
		String fieldName;
				
        public void characters(char[] ch, int start, int end)
            throws SAXException {
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

        public void startElement(String ns, String name, String tagName, Attributes attrs)
            throws SAXException {
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
                	long id = Long.valueOf(attrs.getValue("ref"), 16).longValue();
                    SerialOid internalCollection = new SerialOid(id);
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
                    object = new ObjectData(NakedObjectSpecification.getSpecification(type), new SerialOid(id));
                } else if (tagName.equals("collection")) {
                    String type = attrs.getValue("type");
                    long id = Long.valueOf(attrs.getValue("id"), 16).longValue();
                    collection = new CollectionData(NakedObjectSpecification.getSpecification(type), new SerialOid(id));
                } else {
                    throw new SAXException("Invalid data");
                }
            }
        }
    }

    private class InstanceHandler extends DefaultHandler {
        Vector instances = new Vector();

        public void characters(char[] arg0, int arg1, int arg2)
            throws SAXException {
        }

        public void startElement(String ns, String name, String tagName, Attributes attrs)
            throws SAXException {
            if (tagName.equals("instance")) {
                long oid = Long.valueOf(attrs.getValue("id"), 16).longValue();
                instances.addElement(new SerialOid(oid));
            }
        }
    }

	protected void addInstance(SerialOid oid, String type) throws ObjectStoreException {
		Vector instances = loadInstances(type);
		instances.addElement(oid);
		writeInstanceFile(type, instances);
	}

	protected void addData(SerialOid oid, String type, Data data) throws ObjectStoreException {
		writeData(oid, data);
	}

	protected void updateData(SerialOid oid, String type, Data data) throws ObjectStoreException {
		writeData(oid, data);
	}
	

	protected int numberOfInstances(ObjectData pattern) {
		Vector instances = loadInstances(pattern.getClassName());

		if(instances == null) {
			return 0;
		}
		
		int instanceCount = 0;
		for (int i = 0; i < instances.size(); i++) {
			SerialOid oid = (SerialOid) instances.elementAt(i);
			ObjectData instanceData = (ObjectData) loadData(oid);
			if(instanceData != null && matchesPattern(pattern, instanceData)) {
				instanceCount++;
			}
		}
		return instanceCount;
	}

	protected ObjectDataVector getInstances(ObjectData pattern) {
		Vector instances = loadInstances(pattern.getClassName());

		if(instances == null) {
			return new ObjectDataVector();
		}
		
		ObjectDataVector matches = new ObjectDataVector();
		for (int i = 0; i < instances.size(); i++) {
			SerialOid oid = (SerialOid) instances.elementAt(i);
			ObjectData instanceData = (ObjectData) loadData(oid);
			if(instanceData == null) {
				throw new NakedObjectRuntimeException("No data found for " + oid +" (possible missing file)");
			}
			if(matchesPattern(pattern, instanceData)) {
				matches.addElement(instanceData);
			}
		}
		return matches;
	}

	protected void removeInstance(SerialOid oid, String type) throws ObjectStoreException {
		Vector instances = loadInstances(type);
		instances.removeElement(oid);
		writeInstanceFile(type, instances);
	}

	
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
