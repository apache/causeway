package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent2.Consent;
import org.apache.isis.core.metamodel.consent2.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class AddToCollection extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public AddToCollection(final Perform.Mode mode) {
        super("add to collection", Type.COLLECTION, NumParameters.ONE, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext
                .getObjectMember();
        final CellBinding onMemberBinding = performContext
                .getPeer().getOnMemberBinding();
        
        final OneToManyAssociation otma = (OneToManyAssociation) nakedObjectMember;

        final CollectionAddToFacet addToFacet = nakedObjectMember
                .getFacet(CollectionAddToFacet.class);
        if (addToFacet == null) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(cannot add to collection)");
        }
        
        // safe since guaranteed by superclass
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
        final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();
        final String toAddAlias = arg0Cell.getText();

        final ObjectAdapter toAddAdapter = performContext.getPeer().getAliasRegistry().getAliased(toAddAlias);
        if (toAddAdapter == null) {
			throw ScenarioBoundValueException.current(arg0Binding, "(unknown alias)");
        }

        // validate argument
        otma.createValidateAddInteractionContext(getSession(),
                InteractionInvocationMethod.BY_USER, onAdapter, toAddAdapter);
        final Consent validToAdd = otma.isValidToAdd(onAdapter, toAddAdapter);
        if (validToAdd.isVetoed()) {
        	 throw ScenarioBoundValueException.current(arg0Binding, validToAdd.getReason());
        }

        // add
        addToFacet.add(onAdapter, toAddAdapter);
    }

    public ObjectAdapter getResult() {
        return result;
    }

}
