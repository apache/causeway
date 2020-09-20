package demoapp.dom.annotDomain._interactions;

import java.text.SimpleDateFormat;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.core.runtimeservices.urlencoding.UrlEncodingServiceWithCompression;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

//tag::class[]
@DomainObject(
    objectType = "demo.InteractionDtoVm"
    , nature = Nature.VIEW_MODEL
)
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDtoVm implements ViewModel {

    private final static UrlEncodingService encodingService = new UrlEncodingServiceWithCompression();

    public String title() {
        // nb: not thread-safe
        // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        val buf = new TitleBuffer();
        buf.append(format.format(getInteractionDto().getExecution().getMetrics().getTimings().getStartedAt().toString()));
        buf.append(" ").append(getInteractionDto().getExecution().getLogicalMemberIdentifier());
        return buf.toString();
    }


    @Property
    @PropertyLayout(labelPosition = LabelPosition.NONE)
    @Getter @Setter
    private InteractionDto interactionDto;

    @Override
    public String viewModelMemento() {
        return encodingService.encodeString(InteractionDtoUtils.toXml(interactionDto));
    }

    @Override
    public void viewModelInit(String memento) {
        interactionDto =  InteractionDtoUtils.fromXml(encodingService.decodeToString(memento));
    }

}
//end::class[]
