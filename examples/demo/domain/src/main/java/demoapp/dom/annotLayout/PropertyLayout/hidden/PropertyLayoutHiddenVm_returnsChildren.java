package demoapp.dom.annotLayout.PropertyLayout.hidden;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotLayout.PropertyLayout.hidden.child.PropertyLayoutHiddenChildVm;

@Action(
    semantics = SemanticsOf.SAFE
)
@RequiredArgsConstructor
public class PropertyLayoutHiddenVm_returnsChildren {

    private final PropertyLayoutHiddenVm propertyLayoutHiddenVm;

//tag::meta-annotation[]
    public List<PropertyLayoutHiddenChildVm> act() {
        return propertyLayoutHiddenVm.getChildren();
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutHiddenVm.getPropertyUsingMetaAnnotation();
    }

}
