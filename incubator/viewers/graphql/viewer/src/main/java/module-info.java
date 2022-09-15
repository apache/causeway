module org.apache.isis.incubator.viewer.graphql.viewer {
    exports org.apache.isis.viewer.graphql.viewer;
    exports org.apache.isis.viewer.graphql.viewer.source;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.graphqljava;
    requires java.annotation;
    requires java.inject;
    requires java.net.http;
    requires java.persistence;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.incubator.viewer.graphql.model;
    requires org.reactivestreams;
    requires reactor.core;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.graphql;
    requires spring.tx;
}