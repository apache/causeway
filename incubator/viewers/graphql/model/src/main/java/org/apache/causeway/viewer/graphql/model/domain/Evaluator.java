package org.apache.causeway.viewer.graphql.model.domain;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

@RequiredArgsConstructor
abstract class Evaluator<T> {
    private final T unexpected;

    abstract T evaluate(ActionInteractionHead head, ObjectActionParameter objectActionParameter, Can<ManagedObject> argumentManagedObjects);

    public T unexpected() {
        return unexpected;
    }
}
