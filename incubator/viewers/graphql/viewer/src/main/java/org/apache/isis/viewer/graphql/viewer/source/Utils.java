package org.apache.isis.viewer.graphql.viewer.source;

public class Utils {

    public static final String GQL_INPUTTYPE_PREFIX = "_gql_input__";
    public static final String GQL_MUTATTIONS_FIELDNAME = "_gql_mutations";

    public static String metaTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_meta";
    }

    public static String mutatorsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_mutators";
    }

    public static String logicalTypeNameSanitized(final String logicalTypeName) {
        return logicalTypeName.replace('.', '_');
    }

}
