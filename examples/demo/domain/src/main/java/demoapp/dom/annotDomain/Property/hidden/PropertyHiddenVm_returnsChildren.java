package demoapp.dom.annotDomain.Property.hidden;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotDomain.Property.hidden.child.PropertyHiddenChildVm;

@Action(
    semantics = SemanticsOf.SAFE
)
@RequiredArgsConstructor
public class PropertyHiddenVm_returnsChildren {

    private final PropertyHiddenVm propertyHiddenVm;

//tag::meta-annotation[]
    public List<PropertyHiddenChildVm> act() {
        return propertyHiddenVm.getChildren();
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyHiddenVm.getPropertyUsingMetaAnnotation();
    }

}
