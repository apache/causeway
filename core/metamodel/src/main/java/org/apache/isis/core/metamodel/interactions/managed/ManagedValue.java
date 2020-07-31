package org.apache.isis.core.metamodel.interactions.managed;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface ManagedValue {

    ObjectSpecification getSpecification();
    Bindable<ManagedObject> getValue();
    Observable<String> getValidationMessage();
    Bindable<String> getSearchArgument();
    Observable<Can<ManagedObject>> getChoices();
    
    
}
