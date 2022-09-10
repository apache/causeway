module org.apache.isis.security.bypass {
    exports org.apache.isis.security.bypass;
    exports org.apache.isis.security.bypass.authentication;
    exports org.apache.isis.security.bypass.authorization;

    requires org.apache.isis.applib;
    requires org.apache.isis.security.api;
    requires org.apache.isis.core.runtimeservices;
    requires java.annotation;
    requires java.inject;
    requires spring.beans;
    requires spring.context;
}