package org.apache.isis.viewer.json.viewer.resources;

import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public class StatusTypes {

    private static class StatusTypeImpl implements StatusType {

		private int statusCode;
		private Family family;
		private String reasonPhrase;

		private StatusTypeImpl(final int statusCode, final Family family,
				final String reasonPhrase) {
			this.statusCode = statusCode;
			this.family = family;
			this.reasonPhrase = reasonPhrase;
		}

		@Override
		public int getStatusCode() {
			return statusCode;
		}

		@Override
		public Family getFamily() {
			return family;
		}

		@Override
		public String getReasonPhrase() {
			return reasonPhrase;
		}
    }


    public final static StatusType METHOD_NOT_ALLOWED = new StatusTypeImpl(405, Family.CLIENT_ERROR, "Method not allowed");
    public final static StatusType PRECONDITION_FAILED = new StatusTypeImpl(412, Family.CLIENT_ERROR, "Precondition failed");

}
