module org.apache.isis.valuetypes.jodatime.integration {
    exports org.apache.isis.valuetypes.jodatime.integration;
    exports org.apache.isis.valuetypes.jodatime.integration.valuesemantics;

    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.schema;
    requires org.apache.isis.valuetypes.jodatime.applib;
    requires org.joda.time;
    requires spring.context;
}