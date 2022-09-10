module org.apache.isis.viewer.wicket.applib {
    exports org.apache.isis.viewer.wicket.applib;
    exports org.apache.isis.viewer.wicket.applib.mixins;

    requires javax.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires spring.beans;
    requires spring.context;
}