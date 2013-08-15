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
package org.apache.isis.core.commons.config;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxBeanServer {

    private static final Logger LOG = LoggerFactory.getLogger(JmxBeanServer.class);

    private static JmxBeanServer instance;
    private final MBeanServer server;

    private JmxBeanServer() {
        server = ManagementFactory.getPlatformMBeanServer();
        instance = this;
    }

    public static JmxBeanServer getInstance() {
        if (instance == null) {
            LOG.info("JMX bean server created");
            instance = new JmxBeanServer();
        }
        return instance;
    }

    public void register(final String name, final Object object) {
        try {
            final ObjectName objectName = new ObjectName("Isis:name=" + name);
            server.registerMBean(object, objectName);
            LOG.info(name + " JMX mbean registered: " + object);
        } catch (final MalformedObjectNameException e) {
            throw new RuntimeException(e);
        } catch (final NullPointerException e) {
            throw new RuntimeException(e);
        } catch (final InstanceAlreadyExistsException e) {
            LOG.info(name + " JMX mbean already registered: " + object);
        } catch (final MBeanRegistrationException e) {
            throw new RuntimeException(e);
        } catch (final NotCompliantMBeanException e) {
            throw new RuntimeException(e);
        }

    }
}

// Copyright (c) Naked Objects Group Ltd.
