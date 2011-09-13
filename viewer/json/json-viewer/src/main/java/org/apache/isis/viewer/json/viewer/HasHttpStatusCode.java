package org.apache.isis.viewer.json.viewer;

import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;

public interface HasHttpStatusCode {

    HttpStatusCode getHttpStatusCode();
}
