package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Disabled;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Hidden;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Usable;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Visible;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property.Contains;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property.DoesNotContain;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property.Empty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property.NotEmpty;

public class CheckProperty extends PerformCheckThatAbstract {

    public CheckProperty(final Perform.Mode mode) {
        super("check property", OnMemberColumn.REQUIRED, mode, new Hidden(),
                new Visible(), new Disabled(), new Usable(), new Contains(),
                new DoesNotContain(), new Empty(), new NotEmpty());
    }

}
