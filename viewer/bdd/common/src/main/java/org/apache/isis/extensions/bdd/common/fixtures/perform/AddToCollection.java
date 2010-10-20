package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;

public class AddToCollection extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public AddToCollection(final Perform.Mode mode) {
        super("add to collection", Type.COLLECTION, NumParameters.ONE, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws StoryBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext
                .getNakedObjectMember();
        final CellBinding onMemberBinding = performContext
                .getPeer().getOnMemberBinding();
        
        final OneToManyAssociation otma = (OneToManyAssociation) nakedObjectMember;

        final CollectionAddToFacet addToFacet = nakedObjectMember
                .getFacet(CollectionAddToFacet.class);
        if (addToFacet == null) {
            throw StoryBoundValueException.current(onMemberBinding, "(cannot add to collection)");
        }
        
        // safe since guaranteed by superclass
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
        final StoryCell arg0Cell = arg0Binding.getCurrentCell();
        final String toAddAlias = arg0Cell.getText();

        final ObjectAdapter toAddAdapter = performContext.getPeer().getAliasRegistry().getAliased(toAddAlias);
        if (toAddAdapter == null) {
			throw StoryBoundValueException.current(arg0Binding, "(unknown alias)");
        }

        // validate argument
        otma.createValidateAddInteractionContext(getSession(),
                InteractionInvocationMethod.BY_USER, onAdapter, toAddAdapter);
        final Consent validToAdd = otma.isValidToAdd(onAdapter, toAddAdapter);
        if (validToAdd.isVetoed()) {
        	 throw StoryBoundValueException.current(arg0Binding, validToAdd.getReason());
        }

        // add
        addToFacet.add(onAdapter, toAddAdapter);
    }

    public ObjectAdapter getResult() {
        return result;
    }

}
