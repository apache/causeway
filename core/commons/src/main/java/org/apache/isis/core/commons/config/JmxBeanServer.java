package org.apache.isis.core.commons.config;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;


public class JmxBeanServer {

    private static JmxBeanServer instance;
    private final MBeanServer server;

    private JmxBeanServer() {
        server = ManagementFactory.getPlatformMBeanServer();
        instance = this;
    }
    
    public static JmxBeanServer getInstance() {
        if (instance == null) {
            instance = new JmxBeanServer();
        }
        return instance;
    }

    public void register(String name, Object object) {
        try {
            ObjectName objectName = new ObjectName("Isis:name=" + name);
            server.registerMBean(object, objectName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        } catch (InstanceAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (MBeanRegistrationException e) {
            throw new RuntimeException(e);
        } catch (NotCompliantMBeanException e) {
            throw new RuntimeException(e);
        }

    }
}

// Copyright (c) Naked Objects Group Ltd.
