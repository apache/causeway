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

import type {CausewayActionRequest} from './contracts';

export const FRAMEWORK_LOGOUT_ACTION = Object.freeze({
  serviceLogicalTypeName: 'causeway.security.LogoutMenu',
  actionId: 'logout'
});

export function isFrameworkLogoutAction(value: Partial<CausewayActionRequest> | undefined): boolean {
  return value?.serviceLogicalTypeName === FRAMEWORK_LOGOUT_ACTION.serviceLogicalTypeName
    && value?.actionId === FRAMEWORK_LOGOUT_ACTION.actionId;
}

export function removeFrameworkLogoutMenuActions(root: ParentNode): number {
  let removed = 0;
  for (const control of root.querySelectorAll<HTMLElement>('[data-causeway-service-action]')) {
    if (!isFrameworkLogoutAction({
      serviceLogicalTypeName: control.getAttribute('data-service-logical-type') ?? undefined,
      actionId: control.getAttribute('data-action-id') ?? ''
    })) continue;
    (control.closest<HTMLElement>('[data-causeway-service-action-region]') ?? control).remove();
    removed += 1;
  }
  return removed;
}
