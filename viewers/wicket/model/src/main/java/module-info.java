module org.apache.isis.viewer.wicket.model {
    exports org.apache.isis.viewer.wicket.model.value;
    exports org.apache.isis.viewer.wicket.model.models.interaction.prop;
    exports org.apache.isis.viewer.wicket.model.modelhelpers;
    exports org.apache.isis.viewer.wicket.model.hints;
    exports org.apache.isis.viewer.wicket.model.models;
    exports org.apache.isis.viewer.wicket.model.models.binding;
    exports org.apache.isis.viewer.wicket.model;
    exports org.apache.isis.viewer.wicket.model.links;
    exports org.apache.isis.viewer.wicket.model.models.interaction.act;
    exports org.apache.isis.viewer.wicket.model.models.interaction;
    exports org.apache.isis.viewer.wicket.model.util;
    exports org.apache.isis.viewer.wicket.model.mementos;
    exports org.apache.isis.viewer.wicket.model.models.interaction.coll;
    exports org.apache.isis.viewer.wicket.model.isis;

    requires jakarta.activation;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.core.webapp;
    requires org.apache.isis.viewer.commons.applib;
    requires org.apache.isis.viewer.commons.model;
    requires org.apache.logging.log4j;
    requires org.danekja.jdk.serializable.functional;
    requires org.slf4j;
    requires spring.context;
    requires spring.core;
    //as of 9.11.0 only works when used as automatic named modules ...
    requires org.apache.wicket.core;
    requires org.apache.wicket.request;
    requires org.apache.wicket.util;
}