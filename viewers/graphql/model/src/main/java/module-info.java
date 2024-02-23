module org.apache.causeway.incubator.viewer.graphql.model {
    exports org.apache.causeway.viewer.graphql.model;
    exports org.apache.causeway.viewer.graphql.model.context;
    exports org.apache.causeway.viewer.graphql.model.domain;
    exports org.apache.causeway.viewer.graphql.model.domain.common;
    exports org.apache.causeway.viewer.graphql.model.domain.common.interactors;
    exports org.apache.causeway.viewer.graphql.model.domain.common.query;
    exports org.apache.causeway.viewer.graphql.model.domain.common.query.meta;
    exports org.apache.causeway.viewer.graphql.model.domain.rich;
    exports org.apache.causeway.viewer.graphql.model.domain.rich.query;
    exports org.apache.causeway.viewer.graphql.model.domain.rich.mutation;
    exports org.apache.causeway.viewer.graphql.model.domain.simple;
    exports org.apache.causeway.viewer.graphql.model.domain.simple.query;
    exports org.apache.causeway.viewer.graphql.model.domain.simple.mutation;
    exports org.apache.causeway.viewer.graphql.model.exceptions;
    exports org.apache.causeway.viewer.graphql.model.fetcher;
    exports org.apache.causeway.viewer.graphql.model.mmproviders;
    exports org.apache.causeway.viewer.graphql.model.registry;
    exports org.apache.causeway.viewer.graphql.model.toplevel;
    exports org.apache.causeway.viewer.graphql.model.types;

    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.incubator.viewer.graphql.applib;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires com.graphqljava;
    requires com.graphqljava.extendedscalars;
}