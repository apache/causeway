package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;


import java.time.OffsetDateTime;

import lombok.Getter;

@Getter
public class JavaOffsetDateTimeHolder extends HolderAbstract<JavaOffsetDateTimeHolder> {
    private OffsetDateTime offsetDateTime;

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        bump();
        this.offsetDateTime = broken ? null: offsetDateTime;
    }
}
