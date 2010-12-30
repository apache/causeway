package org.apache.isis.viewer.restful.viewer.util;

import java.util.List;

import com.google.inject.internal.Lists;

import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;


public final class ActionUtils {

    private ActionUtils() {}

    public static List<ObjectAction> flattened(final List<ObjectAction> objectActions) {
        final List<ObjectAction> actions = Lists.newArrayList();
        for (final ObjectAction action : objectActions) {
            if (action.getType() == ActionType.SET) {
                final ObjectActionSet actionSet = (ObjectActionSet) action;
                final List<ObjectAction> subActions = actionSet.getActions();
                for (final ObjectAction subAction : subActions) {
                    actions.add(subAction);
                }
            } else {
                actions.add(action);
            }
        }
        return actions;
    }

}
