package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object.NotSaved;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object.NotValid;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object.Saved;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object.Valid;

public class CheckObject extends PerformCheckThatAbstract {

    public CheckObject(final Perform.Mode mode) {
        super("check object", OnMemberColumn.NOT_REQUIRED, mode, new Valid(),
                new NotValid(), new NotSaved(), new Saved());
    }

}
