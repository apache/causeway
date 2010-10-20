package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.property.ProposedClear;

public class CheckClearProperty extends PerformCheckThatAbstract {

    public CheckClearProperty(final Perform.Mode mode) {
        super("check clear property", OnMemberColumn.REQUIRED, mode,
                new ProposedClear(AssertsValidity.VALID), new ProposedClear(
                AssertsValidity.INVALID));
    }

}
