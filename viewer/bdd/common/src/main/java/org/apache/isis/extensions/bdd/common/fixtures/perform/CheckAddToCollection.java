package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections.ProposedAddTo;

public class CheckAddToCollection extends PerformCheckThatAbstract {

    public CheckAddToCollection(final Perform.Mode mode) {
        super("check add to collection", OnMemberColumn.REQUIRED, mode,
                new ProposedAddTo(AssertsValidity.VALID), new ProposedAddTo(
                AssertsValidity.INVALID));
    }

}
