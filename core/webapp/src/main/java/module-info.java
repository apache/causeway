module org.apache.isis.core.webapp {
    exports org.apache.isis.core.webapp;
    exports org.apache.isis.core.webapp.confmenu;
    exports org.apache.isis.core.webapp.health;
    exports org.apache.isis.core.webapp.keyvaluestore;
    exports org.apache.isis.core.webapp.modules;
    exports org.apache.isis.core.webapp.modules.logonlog;
    exports org.apache.isis.core.webapp.modules.templresources;
    exports org.apache.isis.core.webapp.routing;
    exports org.apache.isis.core.webapp.webappctx;

    requires java.annotation;
    requires javax.inject;
    requires javax.servlet.api;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.interaction;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.core.runtime;
    requires org.apache.isis.security.api;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.actuator;
    requires spring.context;
    requires spring.core;
    requires spring.web;
}