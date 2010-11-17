package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;

public class GetActionParameterDefault extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public GetActionParameterDefault(final Perform.Mode mode) {
        super("get action parameter default", Type.ACTION, NumParameters.ONE,
                mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws StoryBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext
                .getObjectMember();
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final StoryCell arg0Cell = arg0Binding.getCurrentCell();
        int requestedParamNum = -1;
        try {
            requestedParamNum = Integer.valueOf(arg0Cell.getText());
        } catch (final NumberFormatException ex) {
            throw StoryBoundValueException.current(arg0Binding, ex.getMessage());
        }

        final ObjectAction noa = (ObjectAction) nakedObjectMember;
        final int parameterCount = noa.getParameterCount();
        if (requestedParamNum < 0 || requestedParamNum > parameterCount - 1) {
            throw StoryBoundValueException.current(arg0Binding, 
                    "(must be between 0 and " + (parameterCount - 1) + ")");
        }

        final ObjectAdapter[] defaults = noa.getDefaults(onAdapter);
        result = defaults[requestedParamNum];
    }

    public ObjectAdapter getResult() {
        return result;
    }

}
