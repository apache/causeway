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


package org.apache.isis.example.domainservice.email;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.ApplicationException;


public class EmailTemplate extends AbstractDomainObject {
    private VelocityContext context;
    private VelocityEngine ve;
    private boolean loadFromClassPath;

    private void init() {
        if (ve == null) {
            ve = new VelocityEngine();
            try {
                String root = getContainer().getProperty("webapp.dir");
                ve.setProperty("runtime.log", root + "/velocity.log");
                if (loadFromClassPath) {
                    ve.setProperty("resource.loader", "class");
                    ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                }
                ve.init();
                context = new VelocityContext();
            } catch (Exception e) {
                throw new ApplicationException(e);
            }
        }
    }

    public void addContext(String name, Object object) {
        init();
        context.put(name, object);
    }

    public void setLoadFromClassPath(boolean loadFromClassPath) {
        this.loadFromClassPath = loadFromClassPath;
    }
    
    public Email createEmail(String templatePath, String subject, String emailAddress) {
        init();
        StringWriter writer = new StringWriter();
        try { 
            Template t = ve.getTemplate(templatePath);
            t.merge(context, writer);
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

        Email email = newTransientInstance(Email.class);
        email.setSubject(subject);
        email.setMessage(writer.toString());

        Address address = newTransientInstance(Address.class);
        address.setEmailAddress(emailAddress);
        email.addToTo(address);

        return email;
    }

}

