package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class CharHolder extends HolderAbstract<CharHolder> {
    private char someChar;

    public void setSomeChar(char someChar) {
        bump();
        this.someChar = broken ? (char) 0 : someChar;
    }
}
