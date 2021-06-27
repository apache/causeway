package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import org.joda.time.LocalDate;

import lombok.Getter;

@Getter
public class JodaLocalDateHolder extends HolderAbstract<JodaLocalDateHolder> {
    private LocalDate someLocalDate;
    public void setSomeLocalDate(LocalDate someLocalDate) {
        bump();
        this.someLocalDate = broken ? null : someLocalDate;
    }
}
