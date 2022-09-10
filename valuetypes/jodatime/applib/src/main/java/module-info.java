module org.apache.isis.valuetypes.jodatime.applib {
    exports org.apache.isis.valuetypes.jodatime.applib;
    exports org.apache.isis.valuetypes.jodatime.applib.jaxb;
    exports org.apache.isis.valuetypes.jodatime.applib.value;

    requires java.xml;
    requires java.xml.bind;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.joda.time;
    requires spring.context;
    requires spring.core;
}