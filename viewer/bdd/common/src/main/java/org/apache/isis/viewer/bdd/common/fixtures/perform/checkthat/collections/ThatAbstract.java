package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public abstract class ThatAbstract extends ThatSubcommandAbstract {

    public ThatAbstract(final String key) {
        super(key);
    }

    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final OneToManyAssociation otma = (OneToManyAssociation) performContext
                .getNakedObjectMember();

        final ObjectAdapter nakedObjectRepresentingCollection = otma
                .get(onAdapter);
        final CollectionFacet collectionFacet = nakedObjectRepresentingCollection
                .getSpecification().getFacet(CollectionFacet.class);

        doThat(performContext, collectionFacet
                .iterable(nakedObjectRepresentingCollection));

        return nakedObjectRepresentingCollection; // can alias if wish
    }

    protected abstract void doThat(PerformContext performContext,
            Iterable<ObjectAdapter> collection) throws StoryBoundValueException;

}
