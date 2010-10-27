package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;

public class GetPropertyDefault extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public GetPropertyDefault(final Perform.Mode mode) {
        super("get property default", Type.PROPERTY, NumParameters.ZERO, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext
                .getNakedObjectMember();

        final OneToOneAssociation otoa = (OneToOneAssociation) nakedObjectMember;

        // TODO: the OTOA interface is wrong, should be declared as returning a
        // NakedObject
        // (which is indeed what the implementation does)
        result = otoa.getDefault(onAdapter);
    }

    public ObjectAdapter getResult() {
        return result;
    }

}
