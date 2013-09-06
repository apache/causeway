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

package org.apache.isis.core.commons.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;

public class XmlFile {
    private static final String[] ESCAPE_STRING = { "&amp;", "&lt;", "&gt;", "&quot;", "&apos;" };
    private static final String[] SPECIAL_CHARACTERS = { "&", "<", ">", "\"", "'" };

    public static String getValueWithSpecialsEscaped(final String s) {
        String result = s;
        for (int i = 0; i < SPECIAL_CHARACTERS.length; i++) {
            final String special = SPECIAL_CHARACTERS[i];
            int pos = -1;
            while (true) {
                pos = result.indexOf(special, pos + 1);
                if (pos < 0) {
                    break;
                }
                result = result.substring(0, pos) + ESCAPE_STRING[i] + result.substring(pos + special.length());
            }
        }
        return result;
    }

    private final String charset;
    private final File directory;

    public XmlFile(final String charset, final String directory) {
        this.directory = new File(directory);
        createDirectoryIfRequired();
        this.charset = charset;
    }

    private void createDirectoryIfRequired() {
        if (this.directory.exists()) {
            return;
        }
        this.directory.mkdirs();
    }

    private void createDirectoryIfRequired(File file) {
        if(file.exists()) {
            return;
        }
        file.getParentFile().mkdirs();
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
            final File file = file(name);
            createDirectoryIfRequired(file);
            pw = new OutputStreamWriter(new FileOutputStream(file), charset);
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

        final File file = file(fileName);
        try {
            parser.setContentHandler(handler);
            final FileInputStream fis = new FileInputStream(file);
            final InputStreamReader isr = new InputStreamReader(fis, charset);
            final InputSource is = new InputSource(isr);
            parser.parse(is);

            return true;
        } catch (final FileNotFoundException e) {
            return false;
        } catch (final IOException e) {
            throw new IsisException("Error reading XML file", e);
        } catch (final SAXParseException e) {
            throw new IsisException("Error while parsing: " + e.getMessage() + " in " + file + ")", e);
        } catch (final SAXException e) {
            throw new IsisException("Error in file " + file + " ", e);
        }
    }

    public void delete(final String fileName) {
        file(fileName).delete();
    }

    /**
     * The XML store is deemed to be initialised if the directory used to store
     * the data has no xml files in it.
     */
    public boolean isFixturesInstalled() {
        final String[] list = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        return list.length > 0;
    }
    
    @Override
    public String toString() {
        return new ToString(this).append("directory", this.directory).toString();
    }
    
}
