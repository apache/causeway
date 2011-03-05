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


package org.apache.isis.alternatives.objectstore.xml.internal.services.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.isis.alternatives.objectstore.xml.internal.services.ServiceManager;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.xml.ContentWriter;
import org.apache.isis.core.commons.xml.XmlFile;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


class ServiceElement {
    final SerialOid oid;
    final String id;

    public ServiceElement(final SerialOid oid, final String id) {
        Assert.assertNotNull("oid", oid);
        Assert.assertNotNull("id", id);
        this.oid = oid;
        this.id = id;
    }
}

class ServiceHandler extends DefaultHandler {
    Vector services = new Vector();

    @Override
    public void startElement(final String ns, final String name, final String tagName, final Attributes attrs)
            throws SAXException {
        if (tagName.equals("service")) {
            final long oid = Long.valueOf(attrs.getValue("oid"), 16).longValue();
            final String id = attrs.getValue("id");
            final ServiceElement service = new ServiceElement(SerialOid.createPersistent(oid), id);
            services.addElement(service);
        }
    }

}

public class XmlServiceManager implements ServiceManager {
    private static final String SERVICES_FILE_NAME = "services";
    private Vector services;
    private final XmlFile xmlFile;

    public XmlServiceManager(final XmlFile xmlFile) {
        this.xmlFile = xmlFile;
    }

    private String encodedOid(final SerialOid oid) {
        return Long.toHexString(oid.getSerialNo()).toUpperCase();
    }

    public Oid getOidForService(final String name) {
        for (final Enumeration e = services.elements(); e.hasMoreElements();) {
            final ServiceElement element = (ServiceElement) e.nextElement();
            if (element.id.equals(name)) {
                return element.oid;
            }
        }
        return null;
    }

    public void loadServices() {
        final ServiceHandler handler = new ServiceHandler();
        xmlFile.parse(handler, SERVICES_FILE_NAME);
        services = handler.services;
    }

    public void registerService(final String name, final Oid oid) {
        final SerialOid soid = (SerialOid) oid;
        final ServiceElement element = new ServiceElement(soid, name);
        services.addElement(element);
        saveServices();
    }

    public final void saveServices() {
        xmlFile.writeXml(SERVICES_FILE_NAME, new ContentWriter() {
            public void write(Writer writer) throws IOException {
                final String tag = SERVICES_FILE_NAME;
                writer.append("<" + tag + ">\n");
                for (final Enumeration e = services.elements(); e.hasMoreElements();) {
                    final ServiceElement element = (ServiceElement) e.nextElement();
                    writer.append("  <service oid=\"");
                    writer.append(encodedOid(element.oid));
                    writer.append("\" id=\"");
                    writer.append(element.id);
                    writer.append("\" />\n");
                }
                writer.append("</" + tag + ">\n");
            }
        });
    }
}

