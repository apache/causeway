package org.apache.isis.viewer.restful.viewer;

import org.apache.isis.core.runtime.system.SystemConstants;


public final class Constants {

	public static final String VIEWER_PREFIX_KEY = SystemConstants.VIEWER_KEY + ".restful";
	public static final String JAVASCRIPT_DEBUG_KEY = VIEWER_PREFIX_KEY + ".javascript-debug";
	
    public static final String NOF_REST_SUPPORT_JS = "nof-rest-support.js";
    public static final String XMLHTTP_REQUEST_SRC_JS = "XMLHttpRequest.src.js";
    public static final String XMLHTTP_REQUEST_JS = "XMLHttpRequest.js";
	public static final String URL_ENCODING_CHAR_SET = org.apache.isis.viewer.restful.applib.Constants.URL_ENCODING_CHAR_SET;
	
    private Constants() {}

}
