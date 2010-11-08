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


package org.apache.isis.extensions.xml.objectstore.internal.data.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class XmlFile {
    private static final String ENCODING_PROPERTY = ConfigurationConstants.ROOT + "xmlos.encoding";
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final String[] escapeString = { "&amp;", "&lt;", "&gt;", "&quot;", "&apos;" };
    private static final String[] specialChars = { "&", "<", ">", "\"", "'" };

    public static String getValueWithSpecialsEscaped(final String s) {
        String result = s;
        for (int i = 0; i < specialChars.length; i++) {
            final String special = specialChars[i];
            int pos = -1;
            while (true) {
                pos = result.indexOf(special, pos + 1);
                if (pos < 0) {
                    break;
                }
                result = result.substring(0, pos) + escapeString[i] + result.substring(pos + special.length());
            }
        }
        return result;
    }

    private final String charset;
    private final File directory;
    private final IsisConfiguration configuration;

    public XmlFile(final IsisConfiguration configuration, final String directory) {
        this.configuration = configuration;
        this.directory = new File(directory);
        if (!this.directory.exists()) {
            this.directory.mkdirs();
        }
        charset = this.configuration.getString(ENCODING_PROPERTY, DEFAULT_ENCODING);
    }

    public File getDirectory() {
        return directory;
    }
    
    private File file(final String fileName) {
        return new File(directory, fileName + ".xml");
    }

    public void writeXml(final String name, final ContentWriter writer) {
        OutputStreamWriter pw;

        try {
            pw = new OutputStreamWriter(new FileOutputStream(file(name)), charset);
            pw.write("<?xml version=\"1.0\" encoding=\"" + charset + "\" ?>\n");
            pw.write("\n");
            writer.write(pw);
            pw.write("\n");
            pw.close();
        } catch (final IOException e) {
            throw new IsisException("Problems writing data files", e);
        }
    }

    public boolean parse(final ContentHandler handler, final String fileName) {
        XMLReader parser;

        try {
            parser = XMLReaderFactory.createXMLReader();
        } catch (final SAXException e) {
            try {
                parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            } catch (final SAXException e2) {
                try {
                    parser = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
                } catch (final SAXException failed) {
                    throw new IsisException("Couldn't locate a SAX parser");
                }
            }
        }

        try {
            parser.setContentHandler(handler);
            parser.parse(new InputSource(new InputStreamReader(new FileInputStream(file(fileName)), charset)));

            return true;
        } catch (final FileNotFoundException e) {
            return false;
        } catch (final IOException e) {
            throw new IsisException("Error reading XML file", e);
        } catch (final SAXParseException e) {
            throw new IsisException("Error while parsing: " + e.getMessage() + " in " + file(fileName) + ")", e);
        } catch (final SAXException e) {
            throw new IsisException("Error in file " + file(fileName) + " ", e);
        }
    }

    public void delete(final String fileName) {
        file(fileName).delete();
    }

    /**
     * The XML store is deemed to be initialised if the directory used to store the data has no xml files in
     * it.
     */
    public boolean isFixturesInstalled() {
        final String[] list = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        return list.length > 0;
    }
}

