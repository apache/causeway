package org.apache.isis.viewer.json.applib;


public enum RestfulRequestHeader {

    IF_UNMODIFIED_SINCE,
    X_FOLLOW_LINKS,
    X_VALIDATION_ONLY;

    public String getName() {
        return HttpHeaderUtils.enumToHttpHeader(name());
    }
}
