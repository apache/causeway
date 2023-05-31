package demoapp.dom.domain.actions.ActionLayout.redirectPolicy;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class ActionLayoutRedirectPolicyPage_objects {

    @SuppressWarnings("unused")
    private final ActionLayoutRedirectPolicyPage page;

    @MemberSupport public List<? extends ActionLayoutRedirectPolicyEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends ActionLayoutRedirectPolicyEntity> objectRepository;
}
//end::class[]
