package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections.ProposedRemoveFrom;

public class CheckRemoveFromCollection extends PerformCheckThatAbstract {

    public CheckRemoveFromCollection(final Perform.Mode mode) {
        super("check remove from collection", OnMemberColumn.REQUIRED, mode,
                new ProposedRemoveFrom(AssertsValidity.VALID),
                new ProposedRemoveFrom(AssertsValidity.INVALID));
    }

}
