package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import org.joda.time.LocalDateTime;

import lombok.Getter;

@Getter
public class JodaLocalDateTimeHolder extends HolderAbstract<JodaLocalDateTimeHolder> {
    private LocalDateTime someLocalDateTime;
    public void setSomeLocalDateTime(LocalDateTime localDateTime) {
        bump();
        this.someLocalDateTime = broken ? null : localDateTime;
    }
}
