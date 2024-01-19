package org.apache.causeway.viewer.graphql.viewer.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

@RequiredArgsConstructor
public class GqlvServiceStructure {

    @Getter private final ObjectSpecification serviceSpec;

}
