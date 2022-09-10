module org.apache.isis.security.shiro {
    exports org.apache.isis.security.shiro;
    exports org.apache.isis.security.shiro.authentication;
    exports org.apache.isis.security.shiro.authorization;
    exports org.apache.isis.security.shiro.context;
    exports org.apache.isis.security.shiro.permrolemapper;
    exports org.apache.isis.security.shiro.webmodule;

    requires java.annotation;
    requires javax.inject;
    requires javax.servlet.api;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.runtimeservices;
    requires org.apache.isis.core.webapp;
    requires org.apache.isis.security.api;
    requires org.apache.logging.log4j;
    requires org.slf4j;
    requires shiro.core;
    requires shiro.web;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
}