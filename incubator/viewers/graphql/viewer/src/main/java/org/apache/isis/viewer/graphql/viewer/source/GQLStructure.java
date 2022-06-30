package org.apache.isis.viewer.graphql.viewer.source;

import lombok.Data;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.isis.applib.services.metamodel.BeanSort.*;

@Data
public class GQLStructure {

    private final ObjectSpecification objectSpecification;

    public List<String> properties(){
        return objectSpecification.streamProperties(MixedIn.INCLUDED)
                .filter(otoa -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otoa.getElementType().getBeanSort()))
                .map(OneToOneAssociation::getId)
                .collect(Collectors.toList());
    }

    public List<String> collections(){
        return objectSpecification.streamCollections(MixedIn.INCLUDED)
                .filter(otom -> Arrays.asList(VIEW_MODEL, ENTITY, VALUE).contains(otom.getElementType().getBeanSort()))
                .map(OneToManyAssociation::getId)
                .collect(Collectors.toList());
    }

    public List<String> safeActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isSafeInNature())
                .map(ObjectAction::getId)
                .collect(Collectors.toList());
    }

    public List<String> idempotentActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> objectAction.getSemantics().isIdempotentInNature())
                .map(ObjectAction::getId)
                .collect(Collectors.toList());
    }
    public List<String> nonIdempotentActions(){
        return objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .filter(objectAction -> !objectAction.getSemantics().isSafeInNature())
                .filter(objectAction -> !objectAction.getSemantics().isIdempotentInNature())
                .map(ObjectAction::getId)
                .collect(Collectors.toList());
    }

    public String layoutXml(){
        // TODO: implement
     return "<xml>todo: implement .... </xml>";
    }

}
