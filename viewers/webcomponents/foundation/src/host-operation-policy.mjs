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

export const FRAMEWORK_LOGOUT_ACTION = Object.freeze({
  serviceLogicalTypeName: 'causeway.security.LogoutMenu',
  actionId: 'logout'
});

export function isFrameworkLogoutAction(value) {
  return value?.serviceLogicalTypeName === FRAMEWORK_LOGOUT_ACTION.serviceLogicalTypeName
    && value?.actionId === FRAMEWORK_LOGOUT_ACTION.actionId;
}

export function removeFrameworkLogoutMenuActions(root = globalThis.document) {
  if (!root?.querySelectorAll) return 0;
  let removed = 0;
  for (const control of root.querySelectorAll('[data-causeway-service-action]')) {
    if (!isFrameworkLogoutAction({
      serviceLogicalTypeName: control.getAttribute('data-service-logical-type'),
      actionId: control.getAttribute('data-action-id')
    })) continue;
    const region = control.closest?.('[data-causeway-service-action-region]') ?? control;
    region.remove?.();
    removed += 1;
  }
  return removed;
}
