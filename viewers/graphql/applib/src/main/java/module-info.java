module org.apache.causeway.incubator.viewer.graphql.applib {
    exports org.apache.causeway.viewer.graphql.applib;
    exports org.apache.causeway.viewer.graphql.applib.auth;
    exports org.apache.causeway.viewer.graphql.applib.marshallers;

    requires spring.context;
    requires com.graphqljava;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
}