module org.apache.isis.valuetypes.markdown.applib {
    exports org.apache.isis.valuetypes.markdown.applib;
    exports org.apache.isis.valuetypes.markdown.applib.value;
    exports org.apache.isis.valuetypes.markdown.applib.jaxb;

    requires flexmark;
    requires flexmark.ext.gfm.strikethrough;
    requires flexmark.ext.tables;
    requires flexmark.util.ast;
    requires flexmark.util.builder;
    requires flexmark.util.data;
    requires flexmark.util.misc;
    requires flexmark.util.sequence;
    requires java.inject;
    requires java.xml.bind;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires spring.context;
}