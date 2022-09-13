module org.apache.isis.valuetypes.asciidoc.persistence.jdo {
    exports org.apache.isis.valuetypes.asciidoc.persistence.jdo.dn;
    exports org.apache.isis.valuetypes.asciidoc.persistence.jdo.dn.converters;

    requires org.apache.isis.core.config;
    requires org.apache.isis.valuetypes.asciidoc.applib;
    requires org.datanucleus;
    requires spring.context;
}