module org.apache.isis.schema {
    exports org.apache.isis.schema;
    exports org.apache.isis.schema.metamodel.v2;
    exports org.apache.isis.schema.common.v2;
    exports org.apache.isis.schema.cmd.v2;
    exports org.apache.isis.schema.ixn.v2;
    exports org.apache.isis.schema.chg.v2;

    requires java.xml;
    requires java.xml.bind;
    requires spring.context;
}