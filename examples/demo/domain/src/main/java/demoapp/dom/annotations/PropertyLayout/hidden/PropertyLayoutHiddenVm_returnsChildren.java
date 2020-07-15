package demoapp.dom.annotations.PropertyLayout.hidden;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;
import lombok.val;

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
