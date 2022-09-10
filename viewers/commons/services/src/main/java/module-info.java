module org.apache.isis.viewer.commons.services {
    exports org.apache.isis.viewer.commons.services.userprof;
    exports org.apache.isis.viewer.commons.services.branding;
    exports org.apache.isis.viewer.commons.services.header;
    exports org.apache.isis.viewer.commons.services.menu;
    exports org.apache.isis.viewer.commons.services;

    requires java.annotation;
    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.viewer.commons.applib;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.context;
}