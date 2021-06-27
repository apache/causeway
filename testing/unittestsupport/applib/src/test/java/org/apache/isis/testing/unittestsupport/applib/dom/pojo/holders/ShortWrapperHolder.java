package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class ShortWrapperHolder extends HolderAbstract<ShortWrapperHolder> {
    private Short someShortWrapper;

    public void setSomeShortWrapper(Short someShortWrapper) {
        bump();
        this.someShortWrapper = broken ? (short) 0 : someShortWrapper;
    }
}
