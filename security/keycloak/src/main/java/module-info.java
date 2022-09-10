module org.apache.isis.security.keycloak {
    exports org.apache.isis.security.keycloak;
    exports org.apache.isis.security.keycloak.handler;
    exports org.apache.isis.security.keycloak.services;

    requires org.apache.isis.core.webapp;
    requires org.apache.isis.security.spring;
    requires lombok;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.runtimeservices;
    requires org.apache.isis.security.api;
    requires org.slf4j;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.oauth2.client;
    requires spring.security.oauth2.core;
    requires spring.security.oauth2.jose;
    requires spring.security.web;
    requires spring.web;
}