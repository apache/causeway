module org.apache.isis.viewer.commons.applib {
    exports org.apache.isis.viewer.commons.applib.services.userprof;
    exports org.apache.isis.viewer.commons.applib.services.header;
    exports org.apache.isis.viewer.commons.applib.services.branding;
    exports org.apache.isis.viewer.commons.applib.services.menu;
    exports org.apache.isis.viewer.commons.applib;
    exports org.apache.isis.viewer.commons.applib.mixins;

    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires spring.context;
    requires spring.core;
}