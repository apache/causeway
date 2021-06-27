package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class JavaLocalDateHolder extends HolderAbstract<JavaLocalDateHolder> {
    private LocalDate someLocalDate;
    public void setSomeLocalDate(LocalDate someLocalDate) {
        bump();
        this.someLocalDate = broken ? null : someLocalDate;
    }
}
