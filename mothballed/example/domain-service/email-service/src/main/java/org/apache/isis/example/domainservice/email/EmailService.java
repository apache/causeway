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

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import org.apache.isis.applib.AbstractFactoryAndRepository;


public class EmailService extends AbstractFactoryAndRepository {
    private static final String PROPERTY_ROOT = "service.email.";

    /*
    protected void send(String smtpHost, int smtpPort, String from, String to, String subject, String content) {
        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(smtpHost);
            email.addTo("rmatthews@isis.apache.org", "John Doe");
            email.setFrom("me@apache.org", "Me");
            email.setSubject(subject);
            email.setMsg(content);
            email.send();
        } catch (EmailException e) {
            throw new org.apache.isis.service.email.EmailException(e.getMessage(), e);
        }
    }
*/
    public Email createAnEmailMessage() {
        return newTransientInstance(Email.class);
    }

    public Address createAnEmailAddress() {
        return newTransientInstance(Address.class);
    }

    public void send(Email emailObject) {
        
        String smtpHost = getContainer().getProperty(PROPERTY_ROOT + "host", "localhost");
        String portValue = getContainer().getProperty(PROPERTY_ROOT + "port", "25");
        int port = Integer.valueOf(portValue).intValue();
        String authenticationName = getContainer().getProperty(PROPERTY_ROOT + "authentication.name");
        String authenticationPassword = getContainer().getProperty(PROPERTY_ROOT + "authentication.password", "");

        String fromName = getContainer().getProperty(PROPERTY_ROOT + "from.name", "No reply");
        String fromEmailAddress = getContainer().getProperty(PROPERTY_ROOT + "from.address", "noreply@domain.com");
        
        try {

            SimpleEmail simpleEmail = new SimpleEmail();
            simpleEmail.setHostName(smtpHost);
            simpleEmail.setSmtpPort(port);
            if (authenticationName != null) {
                simpleEmail.setAuthentication(authenticationName, authenticationPassword);
            }
            for (Address address : emailObject.getTo()) {
                String name = address.getName();
                if (name == null) {
                    simpleEmail.addTo(address.getEmailAddress());
                } else {
                    simpleEmail.addTo(address.getEmailAddress(), name);
                }
            }
            Address from = emailObject.getFrom();
            if (from == null) {
                simpleEmail.setFrom(fromEmailAddress, fromName);
            } else {
                simpleEmail.setFrom(from.getEmailAddress(), from.getName());
            }
            simpleEmail.setSubject(emailObject.getSubject());
            simpleEmail.setMsg(emailObject.getMessage());
            simpleEmail.send();
        } catch (EmailException e) {
            throw new org.apache.isis.example.domainservice.email.EmailException(e.getMessage(), e);
        }
    }
}

