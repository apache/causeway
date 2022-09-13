module org.apache.isis.valuetypes.asciidoc.applib {
    exports org.apache.isis.valuetypes.asciidoc.applib;
    exports org.apache.isis.valuetypes.asciidoc.applib.value;
    exports org.apache.isis.valuetypes.asciidoc.applib.jaxb;

    requires asciidoctorj.api;
    requires java.inject;
    requires java.xml.bind;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires spring.context;
    requires spring.core;
}