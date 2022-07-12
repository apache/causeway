package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

@Data
public class GQLActionHideDisableValidateEtc {

    private final boolean hide;

    private final String disable;

    private final String validate;

    private final String semantics;

    private final Can<ObjectActionParameter> params;

}
