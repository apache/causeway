package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class
CharWrapperHolder extends HolderAbstract<CharWrapperHolder> {
    private Character someCharWrapper;

    public void setSomeCharWrapper(Character someCharWrapper) {
        bump();
        this.someCharWrapper = broken ? (char) 0 : someCharWrapper;
    }
}
