package org.isisaddons.module.fakedata.dom;

import com.github.javafaker.service.FakeValuesService;
import org.apache.isis.applib.annotation.Programmatic;

public class CreditCards extends AbstractRandomValueGenerator {

    final com.github.javafaker.Business javaFakerBusiness;

    CreditCards(final FakeDataService fakeDataService, final FakeValuesService fakeValuesService) {
        super(fakeDataService);
        javaFakerBusiness = new com.github.javafaker.Business(fakeValuesService);
    }

    @Programmatic
    public String number() {
        return fake.fakeValuesService.fetchString("business.credit_card_numbers");
    }

    @Programmatic
    public String type() {
        return fake.fakeValuesService.fetchString("business.credit_card_types");
    }

}
