module org.apache.isis.valuetypes.markdown.persistence.jpa {
    exports org.apache.isis.valuetypes.markdown.persistence.jpa.converters;
    exports org.apache.isis.valuetypes.markdown.persistence.jpa;

    requires java.persistence;
    requires org.apache.isis.core.config;
    requires org.apache.isis.valuetypes.markdown.applib;
    requires spring.boot.autoconfigure;
    requires spring.context;
}