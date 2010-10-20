package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsContainment;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsEmpty;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.Disabled;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.Hidden;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.Usable;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.Visible;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections.Containment;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections.Emptiness;

public class CheckCollection extends PerformCheckThatAbstract {

    public CheckCollection(final Perform.Mode mode) {
        super("check collection", OnMemberColumn.REQUIRED, mode, new Hidden(),
                new Visible(), new Disabled(), new Usable(), new Emptiness(
                AssertsEmpty.EMPTY), new Emptiness(
                AssertsEmpty.NOT_EMPTY), new Containment(
                AssertsContainment.CONTAINS), new Containment(
                AssertsContainment.DOES_NOT_CONTAIN));
    }

}
