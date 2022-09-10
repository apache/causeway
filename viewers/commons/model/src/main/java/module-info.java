module org.apache.isis.viewer.commons.model {
    exports org.apache.isis.viewer.commons.model;
    exports org.apache.isis.viewer.commons.model.action.decorator;
    exports org.apache.isis.viewer.commons.model.layout;
    exports org.apache.isis.viewer.commons.model.components;
    exports org.apache.isis.viewer.commons.model.binding;
    exports org.apache.isis.viewer.commons.model.mixin;
    exports org.apache.isis.viewer.commons.model.mock;
    exports org.apache.isis.viewer.commons.model.object;
    exports org.apache.isis.viewer.commons.model.action;
    exports org.apache.isis.viewer.commons.model.decorators;
    exports org.apache.isis.viewer.commons.model.scalar;

    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires spring.core;
}