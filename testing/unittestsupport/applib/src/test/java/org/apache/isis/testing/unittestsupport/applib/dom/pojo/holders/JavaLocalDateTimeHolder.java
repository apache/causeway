package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class JavaLocalDateTimeHolder extends HolderAbstract<JavaLocalDateTimeHolder> {
    private LocalDateTime someLocalDateTime;
    public void setSomeLocalDateTime(LocalDateTime localDateTime) {
        bump();
        this.someLocalDateTime = broken ? null : localDateTime;
    }
}
