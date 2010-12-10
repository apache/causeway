package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class GetActionParameterChoices extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public GetActionParameterChoices(final Perform.Mode mode) {
        super("get action parameter choices", Type.ACTION, NumParameters.ONE,
                mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext
                .getObjectMember();
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();

        int requestedParamNum = -1;
        try {
            requestedParamNum = Integer.valueOf(arg0Cell.getText());
        } catch (final NumberFormatException ex) {
            throw ScenarioBoundValueException.current(arg0Binding, ex.getMessage());
        }

        final ObjectAction noa = (ObjectAction) nakedObjectMember;
        final int parameterCount = noa.getParameterCount();
        if (requestedParamNum < 0 || requestedParamNum > parameterCount - 1) {
            throw ScenarioBoundValueException.current(arg0Binding, 
                    "(must be between 0 and " + (parameterCount - 1) + ")");
        }

        final ObjectAdapter[][] allParameterChoices = noa.getChoices(onAdapter);
        result = performContext.getPeer()
                .toAdaptedListOfPojos(allParameterChoices[requestedParamNum]);
    }

    public ObjectAdapter getResult() {
        return result;
    }

}
