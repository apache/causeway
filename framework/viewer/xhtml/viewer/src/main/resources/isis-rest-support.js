/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Sends a PUT to /objects/oid/property/propertyId?proposedValue=valueOrOid
 */
function modifyProperty(objectUri, propertyId, proposedValue) {
	modifyAssociation("PUT", objectUri, "property", propertyId, proposedValue);
}

/**
 * Sends a DELETE to /objects/oid/property/propertyId
 */
function clearProperty(objectUri, propertyId, proposedValue) {
	modifyAssociation("DELETE", objectUri, "property", propertyId, null);
}

/**
 * Sends a PUT to /objects/oid/collection/collectionId?proposedValue=oidToAdd
 */
function addToCollection(objectUri, collectionId, proposedValue) {
	modifyAssociation("PUT", objectUri, "collection", collectionId, proposedValue);
}

/**
 * Sends a DELETE to /objects/oid/collection/collectionId?proposedValue=oidToRemove
 */
function removeFromCollection(objectUri, collectionId, proposedValue) {
	modifyAssociation("DELETE", objectUri, "collection", collectionId, proposedValue);
}

/**
 * Helper that factors out common functionality in the four functions that modify
 * either properties or collections.
 */
function modifyAssociation(verb, objectUri, associationType, associationId, proposedValue) {
	var uri = uriOnSuccess(objectUri, associationType, associationId);
	if (proposedValue) {
		uri += "?" + "proposedValue=" + proposedValue;
	}
	var reasonInvalidDomId = reasonInvalidDomIdOnFailure(associationType, associationId);
	invokeUri(verb, uri, objectUri, reasonInvalidDomId);
}



function uriOnSuccess(objectUri, memberType, memberId) {
	return objectUri + "/" + memberType + "/" + memberId;
}

function reasonInvalidDomIdOnFailure(memberType, memberId) {
	return memberType + "-invalid-" + memberId;
}

/**
 * Helper function that does the heavily lifting.
 * 
 * <p>
 * Makes the request, and redirects to the objectUri if successful, or updates the
 * DOM element with id <tt>association-invalid-xxx</tt> otherwise. 
 */
function invokeUri(verb, uri, uriOnSuccess, reasonInvalidDomIdOnFailure) {
	//debugger;
	var xhr = jQuery.ajax( {
	    url: uri,
	    type: verb,
	    dataType: 'xml',
	    success: function(data, textStatus, jqXHR) {
	        window.location = uriOnSuccess;
	    },
	    error: function(jqXHR, textStatus, errorThrown) {
	        if (jqXHR.status >= 400 && jqXHR.status < 500) {
                reason = jqXHR.getResponseHeader("isis-reason");
                if (!reason) {
                    reason = "invalid (unable to determine reason)";
                }
                reasonInvalidPara = document.getElementById(reasonInvalidDomIdOnFailure);
                if (reasonInvalidPara) {
                    reasonInvalidPara.innerHTML = reason;
                }
	        } else {
                alert(jqXHR.status)
	        }
	    }
	}
	);
}
