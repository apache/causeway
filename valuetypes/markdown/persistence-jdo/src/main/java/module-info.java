module org.apache.isis.valuetypes.markdown.persistence.jdo {
    exports org.apache.isis.valuetypes.markdown.persistence.jdo.dn.converters;
    exports org.apache.isis.valuetypes.markdown.persistence.jdo.dn;

    requires org.apache.isis.core.config;
    requires org.apache.isis.valuetypes.markdown.applib;
    requires org.datanucleus;
    requires spring.context;
}