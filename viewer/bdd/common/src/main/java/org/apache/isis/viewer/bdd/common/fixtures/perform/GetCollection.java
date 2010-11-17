package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

public class GetCollection extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public GetCollection(final Perform.Mode mode) {
        super("get collection", Type.COLLECTION, NumParameters.ZERO, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext
                .getObjectMember();

        final OneToManyAssociation otma = (OneToManyAssociation) nakedObjectMember;

        result = otma.get(onAdapter);
    }

    public ObjectAdapter getResult() {
        return result;
    }

}
