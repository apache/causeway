module org.apache.causeway.incubator.viewer.graphql.viewer {
    exports org.apache.causeway.viewer.graphql.viewer;
    exports org.apache.causeway.viewer.graphql.viewer.source;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.graphqljava;
    requires java.annotation;
    requires java.inject;
    requires java.net.http;
    requires java.persistence;
    requires lombok;
    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.incubator.viewer.graphql.model;
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