module org.apache.isis.security.api {
    exports org.apache.isis.core.security;
    exports org.apache.isis.core.security._testing;
    exports org.apache.isis.core.security.authentication.fixtures;
    exports org.apache.isis.core.security.authentication.login;
    exports org.apache.isis.core.security.authentication.logout;
    exports org.apache.isis.core.security.authentication.manager;
    exports org.apache.isis.core.security.authentication.singleuser;
    exports org.apache.isis.core.security.authentication.standard;
    exports org.apache.isis.core.security.authentication;
    exports org.apache.isis.core.security.authorization.manager;
    exports org.apache.isis.core.security.authorization.standard;
    exports org.apache.isis.core.security.authorization;
    exports org.apache.isis.core.security.util;

    requires java.annotation;
    requires java.desktop;
    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.schema;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
}