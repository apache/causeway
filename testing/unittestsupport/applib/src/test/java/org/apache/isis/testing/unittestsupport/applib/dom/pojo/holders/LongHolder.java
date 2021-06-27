package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class LongHolder extends HolderAbstract<LongHolder> {
    private long someLong;

    public void setSomeLong(long someLong) {
        bump();
        this.someLong = broken ? 0 : someLong;
    }
}
