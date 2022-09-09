module org.apache.isis.security.bypass {
    exports org.apache.isis.security.bypass;
    exports org.apache.isis.security.bypass.authentication;
    exports org.apache.isis.security.bypass.authorization;

    requires isis.core.runtimeservices;
    requires java.annotation;
    requires javax.inject;
    requires org.apache.isis.applib;
    requires org.apache.isis.security.api;
    requires spring.beans;
    requires spring.context;
}