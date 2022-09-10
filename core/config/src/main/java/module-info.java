module org.apache.isis.core.config {
    exports org.apache.isis.core.config;
    exports org.apache.isis.core.config.applib;
    exports org.apache.isis.core.config.beans.aoppatch;
    exports org.apache.isis.core.config.beans;
    exports org.apache.isis.core.config.converters;
    exports org.apache.isis.core.config.datasources;
    exports org.apache.isis.core.config.environment;
    exports org.apache.isis.core.config.messages;
    exports org.apache.isis.core.config.metamodel.facets;
    exports org.apache.isis.core.config.metamodel.services;
    exports org.apache.isis.core.config.metamodel.specloader;
    exports org.apache.isis.core.config.presets;
    exports org.apache.isis.core.config.progmodel;
    exports org.apache.isis.core.config.util;
    exports org.apache.isis.core.config.validators;
    exports org.apache.isis.core.config.viewer.web;

    requires jakarta.activation;
    requires java.annotation;
    requires java.persistence;
    requires java.sql;
    requires java.validation;
    requires java.ws.rs;
    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.logging.log4j;
    requires org.eclipse.persistence.core;
    requires org.hibernate.validator;
    requires spring.aop;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.tx;

    uses org.apache.isis.core.config.beans.IsisBeanTypeClassifier;

    opens org.apache.isis.core.config to spring.core, org.hibernate.validator;
    opens org.apache.isis.core.config.environment to spring.core;
}