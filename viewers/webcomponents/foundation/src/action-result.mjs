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

import {namedType} from './introspection.mjs';
import {InteractionResultKind} from './types.mjs';

export const LOCAL_RESOURCE_PATH_GRAPHQL_TYPE = 'LocalResourcePathValue';
export const LOCAL_RESOURCE_STRATEGIES = Object.freeze(['SAME_WINDOW', 'NEW_WINDOW']);

export function normalizeActionResult(value, typeRef) {
  const inner = innermostType(typeRef);
  const list = unwrapNonNull(typeRef)?.kind === 'LIST';
  if (value == null) {
    return Object.freeze({kind: InteractionResultKind.VOID, value: null});
  }
  if (list || Array.isArray(value)) {
    return Object.freeze({kind: InteractionResultKind.COLLECTION, value});
  }
  if (namedType(typeRef) === LOCAL_RESOURCE_PATH_GRAPHQL_TYPE) {
    return normalizeLocalResourceResult(value);
  }
  if (inner?.kind === 'SCALAR' || inner?.kind === 'ENUM' || typeof value !== 'object') {
    return Object.freeze({kind: InteractionResultKind.SCALAR, value});
  }
  return Object.freeze({kind: InteractionResultKind.OBJECT, value});
}

function normalizeLocalResourceResult(value) {
  const path = typeof value?.path === 'string' ? value.path.trim() : '';
  const openUrlStrategy = typeof value?.openUrlStrategy === 'string' ? value.openUrlStrategy : '';
  if (!path || !LOCAL_RESOURCE_STRATEGIES.includes(openUrlStrategy)) {
    return Object.freeze({
      kind: InteractionResultKind.UNSUPPORTED,
      value: null,
      reason: 'LOCAL_RESOURCE_RESULT_INVALID'
    });
  }
  return Object.freeze({
    kind: InteractionResultKind.LOCAL_RESOURCE,
    value: Object.freeze({path, openUrlStrategy})
  });
}

function unwrapNonNull(typeRef) {
  let current = typeRef;
  while (current?.kind === 'NON_NULL') current = current.ofType;
  return current;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) current = current.ofType;
  return current;
}
