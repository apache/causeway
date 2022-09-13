module org.apache.isis.valuetypes.asciidoc.metamodel {
    exports org.apache.isis.valuetypes.asciidoc.metamodel;
    exports org.apache.isis.valuetypes.asciidoc.metamodel.semantics;

    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.schema;
    requires org.apache.isis.valuetypes.asciidoc.applib;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
}