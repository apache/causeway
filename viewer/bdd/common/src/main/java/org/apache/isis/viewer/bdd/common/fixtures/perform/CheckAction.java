package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Disabled;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Hidden;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Usable;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Visible;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.action.ArgumentSetNotValid;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.action.ArgumentSetValid;

public class CheckAction extends PerformCheckThatAbstract {

    public CheckAction(final Perform.Mode mode) {
        super("check action", OnMemberColumn.REQUIRED, mode, new Hidden(),
                new Visible(), new Disabled(), new Usable(),
                new ArgumentSetValid(), new ArgumentSetNotValid());
    }

}
