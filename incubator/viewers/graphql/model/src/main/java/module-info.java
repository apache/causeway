module org.apache.causeway.incubator.viewer.graphql.model {
    exports org.apache.causeway.viewer.graphql.model;
    exports org.apache.causeway.viewer.graphql.model.parts;
    exports org.apache.causeway.viewer.graphql.model.util;
    exports org.apache.causeway.viewer.graphql.model.types;
    exports org.apache.causeway.viewer.graphql.model.registry;
    exports org.apache.causeway.viewer.graphql.model.domain;

    requires org.apache.causeway.core.config;
    requires org.apache.causeway.incubator.viewer.graphql.applib;
    requires spring.context;
    requires org.apache.causeway.core.metamodel;
    requires com.graphqljava;
}