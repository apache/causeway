package demoapp.dom.annotLayout.PropertyLayout.renderDay;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
)
@RequiredArgsConstructor
public class PropertyLayoutRenderDayVm_downloadAsXml {

    private final PropertyLayoutRenderDayVm propertyLayoutRenderDayVm;

    public Clob act(final String fileName) {
        val xml = jaxbService.toXml(propertyLayoutRenderDayVm);
        return Clob.of(fileName, NamedWithMimeType.CommonMimeType.XML, xml);
    }
    public String default0Act() {
        return "PropertyLayoutRenderVm.xml";
    }

    @Inject
    JaxbService jaxbService;

}
//end::class[]
