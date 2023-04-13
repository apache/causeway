package demoapp.dom.domain.objects.DomainObject.nature.viewmodel;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action()
@RequiredArgsConstructor
public class DomainObjectNatureViewModel_updateMessage {
    // ...
//end::class[]
    private final DomainObjectNatureViewModel mixee;

//tag::class[]
    public DomainObjectNatureViewModel act(@Nullable final String newMessage) {   // <.>
        mixee.setMessage(newMessage);
        return mixee;
    }
    public String default0Act() {
        return mixee.getMessage();
    }
}
//end::class[]
