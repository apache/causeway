module org.apache.isis.valuetypes.asciidoc.persistence.jpa {
    exports org.apache.isis.valuetypes.asciidoc.persistence.jpa;
    exports org.apache.isis.valuetypes.asciidoc.persistence.jpa.converters;

    requires java.persistence;
    requires org.apache.isis.core.config;
    requires org.apache.isis.valuetypes.asciidoc.applib;
    requires spring.boot.autoconfigure;
    requires spring.context;
}