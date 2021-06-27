package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import org.joda.time.DateTime;

import lombok.Getter;

@Getter
public class JodaDateTimeHolder extends HolderAbstract<JodaDateTimeHolder> {
    private DateTime someDateTime;
    public void setSomeDateTime(DateTime dateTime) {
        bump();
        this.someDateTime = broken ? null : dateTime;
    }
}
