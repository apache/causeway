module org.apache.isis.security.spring {
    exports org.apache.isis.security.spring;
    exports org.apache.isis.security.spring.authconverters;
    exports org.apache.isis.security.spring.authentication;
    exports org.apache.isis.security.spring.webmodule;

    requires java.annotation;
    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.runtimeservices;
    requires org.apache.isis.core.webapp;
    requires org.apache.isis.security.api;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.security.core;
    requires spring.security.web;
    requires spring.web;
}