package org.apache.causeway.viewer.graphql.model.domain;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

/**
 *
 * @param <T>
 * @param <U> - either an {@link org.apache.causeway.core.metamodel.spec.feature.ObjectAction} or an {@link ObjectActionParameter}.
 */
@RequiredArgsConstructor
abstract class Evaluator<T, U extends ObjectFeature> {
    private final T unexpected;

    abstract T evaluate(ActionInteractionHead head, U objectFeature, Can<ManagedObject> argumentManagedObjects);

    public T unexpected() {
        return unexpected;
    }
}
