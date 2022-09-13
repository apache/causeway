module org.apache.isis.valuetypes.markdown.metamodel {
    exports org.apache.isis.valuetypes.markdown.metamodel;
    exports org.apache.isis.valuetypes.markdown.metamodel.semantics;

    requires java.inject;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.schema;
    requires org.apache.isis.valuetypes.markdown.applib;
    requires spring.context;
}