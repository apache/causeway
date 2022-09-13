module org.apache.isis.valuetypes.asciidoc.ui.wkt {
    exports org.apache.isis.valuetypes.asciidoc.ui.wkt;
    exports org.apache.isis.valuetypes.asciidoc.ui.wkt.components;

    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.valuetypes.asciidoc.applib;
    requires org.apache.isis.viewer.commons.model;
    requires org.apache.isis.viewer.wicket.model;
    requires org.apache.isis.viewer.wicket.ui;
    requires org.apache.wicket.core;
    requires org.apache.wicket.util;
    requires spring.context;
}