module org.apache.isis.core.codegen.bytebuddy {
    exports org.apache.isis.core.codegen.bytebuddy;
    exports org.apache.isis.core.codegen.bytebuddy.services;

    requires net.bytebuddy;
    requires org.objenesis;
    requires org.apache.isis.commons;
    requires spring.context;
    requires spring.core;
}