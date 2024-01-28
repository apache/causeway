module org.apache.causeway.incubator.viewer.graphql.applib {
    exports org.apache.causeway.viewer.graphql.applib;
    exports org.apache.causeway.viewer.graphql.applib.types;
    exports org.apache.causeway.viewer.graphql.applib.auth;

    requires spring.context;
    requires com.graphqljava;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
}