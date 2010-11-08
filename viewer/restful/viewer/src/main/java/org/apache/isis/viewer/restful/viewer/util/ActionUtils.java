package org.apache.isis.viewer.restful.viewer.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.runtimecontext.spec.feature.ObjectActionSet;


public final class ActionUtils {

    private ActionUtils() {}

    public static ObjectAction[] flattened(final ObjectAction[] objectActions) {
        final List<ObjectAction> actions = new ArrayList<ObjectAction>();
        for (final ObjectAction action : objectActions) {
            if (action.getType() == ObjectActionType.SET) {
                final ObjectActionSet actionSet = (ObjectActionSet) action;
                final ObjectAction[] subActions = actionSet.getActions();
                for (final ObjectAction subAction : subActions) {
                    actions.add(subAction);
                }
            } else {
                actions.add(action);
            }
        }
        return actions.toArray(new ObjectAction[] {});
    }

}
