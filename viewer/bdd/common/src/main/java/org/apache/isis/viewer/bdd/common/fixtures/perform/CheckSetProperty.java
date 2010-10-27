package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property.ProposedSet;

public class CheckSetProperty extends PerformCheckThatAbstract {

    public CheckSetProperty(final Perform.Mode mode) {
        super("check set property", OnMemberColumn.REQUIRED, mode,
                new ProposedSet(AssertsValidity.VALID), new ProposedSet(
                AssertsValidity.INVALID));
    }

}
