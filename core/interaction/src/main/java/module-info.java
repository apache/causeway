module org.apache.isis.core.interaction {
    exports org.apache.isis.core.interaction;
    exports org.apache.isis.core.interaction.integration;
    exports org.apache.isis.core.interaction.scope;
    exports org.apache.isis.core.interaction.session;

    requires java.annotation;
    requires java.sql;
    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.schema;
    requires org.apache.logging.log4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
}