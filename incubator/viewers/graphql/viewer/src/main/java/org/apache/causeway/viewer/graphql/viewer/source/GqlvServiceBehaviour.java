package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.GraphQLCodeRegistry;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GqlvServiceBehaviour {

    private final GqlvServiceStructure structure;
    private final Object service;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;


}
