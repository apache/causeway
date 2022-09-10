module org.apache.isis.core.transaction {
    exports org.apache.isis.core.transaction;
    exports org.apache.isis.core.transaction.changetracking;
    exports org.apache.isis.core.transaction.changetracking.events;
    exports org.apache.isis.core.transaction.events;

    requires java.annotation;
    requires java.sql;
    requires javax.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
}