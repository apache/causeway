package org.apache.causeway.viewer.graphql.model.registry;

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Just a simple wrapper around the set of discovered {@link GraphQLType}s.
 */
@Component
@Log4j2
public class GraphQLTypeRegistry {

    Set<GraphQLType> graphQLObjectTypes = new HashSet<>();

    public Set<GraphQLType> getGraphQLObjectTypes() {
        return Collections.unmodifiableSet(graphQLObjectTypes);
    }


    void addTypeIfNotAlreadyPresent(
            final GraphQLObjectType typeToAdd,
            final String logicalTypeName){

        if (isPresent(typeToAdd, GraphQLObjectType.class)){
            // For now we just log and skip
            log.info("GraphQLObjectType for {} already present", logicalTypeName);
            return;
        }
        graphQLObjectTypes.add(typeToAdd);
    }


    public void addTypeIfNotAlreadyPresent(final GraphQLType typeToAdd) {

        if (typeToAdd instanceof GraphQLObjectType) {
            addTypeIfNotAlreadyPresent((GraphQLObjectType) typeToAdd);
            return;
        }

        if (typeToAdd instanceof GraphQLInputObjectType) {
            addTypeIfNotAlreadyPresent((GraphQLInputObjectType) typeToAdd);
            return;
        }

        // TODO: none of these types yet handled
        // GraphQLTypeReference
        // GraphQLScalarType
        // GraphQLCompositeType
        // GraphQLUnionType
        // GraphQLEnumType
        // GraphQLInterfaceType
        // GraphQLList
        // GraphQLNonNull
        log.warn("GraphQLType {} not yet implemented", typeToAdd.getClass().getName());
    }

    void addTypeIfNotAlreadyPresent(final GraphQLObjectType typeToAdd){
        if (isPresent(typeToAdd, GraphQLObjectType.class)){
            // For now we just log and skip
            log.info("GraphQLObjectType for {} already present", typeToAdd.getName());
            return;
        }
        add(typeToAdd);
    }


    void addTypeIfNotAlreadyPresent(final GraphQLInputObjectType typeToAdd) {
        if (isPresent(typeToAdd, GraphQLInputObjectType.class)){
            // For now we just log and skip
            log.info("GraphQLInputObjectType for {} already present", typeToAdd.getName());
            return;
        }
        add(typeToAdd);
    }

    private boolean isPresent(
            final GraphQLNamedType typeToAdd,
            final Class<? extends GraphQLNamedType> cls) {
        return  graphQLObjectTypes.stream()
                .filter(o -> o.getClass().isAssignableFrom(cls))
                .map(cls::cast)
                .anyMatch(ot -> ot.getName().equals(typeToAdd.getName()));
    }


    private void add(GraphQLType typeToAdd) {
        graphQLObjectTypes.add(typeToAdd);
    }

}
