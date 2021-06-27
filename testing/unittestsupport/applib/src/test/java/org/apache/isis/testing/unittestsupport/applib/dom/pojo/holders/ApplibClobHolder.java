package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import org.apache.isis.applib.value.Clob;

import lombok.Getter;

@Getter
public class ApplibClobHolder extends HolderAbstract<ApplibClobHolder> {
    private Clob someClob;
    public void setSomeClob(Clob someClob) {
        bump();
        this.someClob = broken ? null : someClob;
    }
}
