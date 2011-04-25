package org.apache.isis.core.commons.config;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

public class JmxBeanServer {

    private static final Logger LOG = Logger.getLogger(JmxBeanServer.class);

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
            throw new RuntimeException(e);
        } catch (final MBeanRegistrationException e) {
            throw new RuntimeException(e);
        } catch (final NotCompliantMBeanException e) {
            throw new RuntimeException(e);
        }

    }
}

// Copyright (c) Naked Objects Group Ltd.
