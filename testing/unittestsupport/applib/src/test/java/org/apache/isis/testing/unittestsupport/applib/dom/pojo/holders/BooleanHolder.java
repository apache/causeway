package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class BooleanHolder extends HolderAbstract<BooleanHolder> {
    private boolean someBoolean;

    public void setSomeBoolean(boolean someBoolean) {
        bump();
        this.someBoolean = broken ? false : someBoolean;
    }
}
