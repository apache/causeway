/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

export function readAuthenticationMetadata(document) {
  const value = name => document.querySelector(`meta[name="${name}"]`)?.content ?? '';
  const loginPath = value('causeway-auth-login');
  const csrfHeaderName = value('causeway-auth-csrf-header');
  const csrfParameterName = value('causeway-auth-csrf-parameter');
  const csrfToken = value('causeway-auth-csrf-token');
  if (!loginPath || !csrfHeaderName || !csrfParameterName || !csrfToken) {
    return null;
  }
  return Object.freeze({
    loginPath,
    csrfHeaderName,
    csrfParameterName,
    csrfToken,
    excludedActions: new Set(value('causeway-auth-excluded-actions').split(',').filter(Boolean))
  });
}

export function csrfHeaders(authentication) {
  return authentication
    ? {[authentication.csrfHeaderName]: authentication.csrfToken}
    : {};
}

export function loginDestination(authentication, location) {
  const destination = location.pathname + location.search;
  const separator = authentication.loginPath.includes('?') ? '&' : '?';
  return `${authentication.loginPath}${separator}continue=${encodeURIComponent(destination)}`;
}

export function isUnsafeMethod(method) {
  return !['get', 'head', 'options', 'trace'].includes(String(method ?? '').toLowerCase());
}

export function isExcludedAction(authentication, detail) {
  const identity = `${detail?.serviceLogicalTypeName}#${detail?.actionId}`;
  return authentication?.excludedActions.has(identity) ?? false;
}

export function applyAuthenticationMenuPolicy(authentication, document) {
  if (!authentication?.excludedActions.size) {
    return;
  }
  for (const button of document.querySelectorAll('[data-causeway-service-action]')) {
    if (isExcludedAction(authentication, {
      serviceLogicalTypeName: button.getAttribute('data-service-logical-type'),
      actionId: button.getAttribute('data-action-id')
    })) {
      button.closest('[data-causeway-service-action-region]')?.remove();
    }
  }
}
