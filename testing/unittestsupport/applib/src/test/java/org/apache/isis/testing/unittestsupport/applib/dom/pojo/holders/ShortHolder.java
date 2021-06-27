package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class ShortHolder extends HolderAbstract<ShortHolder> {
    private short someShort;

    public void setSomeShort(short someShort) {
        bump();
        this.someShort = broken ? 0 : someShort;
    }
}
