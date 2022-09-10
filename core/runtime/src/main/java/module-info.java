module org.apache.isis.core.runtime {
    exports org.apache.isis.core.runtime;
    exports org.apache.isis.core.runtime.context;
    exports org.apache.isis.core.runtime.idstringifier;
    exports org.apache.isis.core.runtime.events;

    requires java.annotation;
    requires java.desktop;
    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.interaction;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.core.transaction;
    requires org.apache.isis.security.api;
    requires org.apache.isis.valuetypes.jodatime.integration;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
}