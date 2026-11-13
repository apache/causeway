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

import {fieldsByName, namedType} from './introspection.mjs';
import {
  argumentsFromValues,
  collectionResultSelectionForType,
  resultSelectionForType
} from './interaction-operations.mjs';

export const ActionInvocationPlacement = Object.freeze({
  NESTED_QUERY: 'nested-query',
  ROOT_MUTATION: 'root-mutation',
  LEGACY_NESTED_MUTATION: 'legacy-nested-mutation'
});

export class ActionDispatchPlanError extends Error {
  constructor(code, message) {
    super(message);
    this.name = 'ActionDispatchPlanError';
    this.code = code;
  }
}

export function createActionInvocationPlan({
  targetKind,
  description,
  descriptor,
  mutationType = null,
  mutationFieldName = null
} = {}) {
  const safeField = ['invoke', 'invokeIdempotent']
    .map(name => descriptor?.fields?.get(name))
    .find(Boolean) ?? null;
  const legacyMutationField = descriptor?.fields?.get('invokeNonIdempotent') ?? null;
  const rootMutationField = mutationType && mutationFieldName
    ? fieldsByName(mutationType).get(mutationFieldName) ?? null
    : null;

  let placement;
  let field;
  if (safeField) {
    placement = ActionInvocationPlacement.NESTED_QUERY;
    field = safeField;
  } else if (rootMutationField) {
    placement = ActionInvocationPlacement.ROOT_MUTATION;
    field = rootMutationField;
  } else if (legacyMutationField) {
    placement = ActionInvocationPlacement.LEGACY_NESTED_MUTATION;
    field = legacyMutationField;
  } else {
    return Object.freeze({
      supported: false,
      targetKind,
      placement: null,
      mutating: false,
      field: null,
      fieldName: null,
      mutationFieldName: null,
      targetArgumentName: null
    });
  }

  const mutating = placement !== ActionInvocationPlacement.NESTED_QUERY;
  const targetArgumentName = placement === ActionInvocationPlacement.ROOT_MUTATION && targetKind === 'object'
    ? findTargetArgument(field, description?.generatedInputTypeName)
    : null;
  if (placement === ActionInvocationPlacement.ROOT_MUTATION && targetKind === 'object' && !targetArgumentName) {
    throw new ActionDispatchPlanError(
      'ACTION_TARGET_ARGUMENT_UNAVAILABLE',
      'The advertised object action mutation has no compatible target argument.');
  }

  return Object.freeze({
    supported: true,
    targetKind,
    placement,
    mutating,
    field,
    fieldName: field.name,
    mutationFieldName: placement === ActionInvocationPlacement.ROOT_MUTATION ? mutationFieldName : null,
    targetArgumentName
  });
}

export function actionInvocationArguments(plan, values = {}, identity = null) {
  if (!plan?.supported || !plan.field) {
    throw new ActionDispatchPlanError('ACTION_INVOCATION_UNAVAILABLE', 'No executable action invocation is advertised.');
  }
  const args = argumentsFromValues(plan.field, values);
  if (plan.targetArgumentName) {
    if (!identity?.id) {
      throw new ActionDispatchPlanError('ACTION_TARGET_IDENTITY_UNAVAILABLE', 'The object action target identity is unavailable.');
    }
    args[plan.targetArgumentName] = {id: identity.id};
  }
  for (const argument of plan.field.args ?? []) {
    if (argument.type?.kind !== 'NON_NULL' || Object.prototype.hasOwnProperty.call(args, argument.name)) {
      continue;
    }
    throw new ActionDispatchPlanError(
      'ACTION_REQUIRED_ARGUMENT_UNAVAILABLE',
      `Required action argument '${argument.name}' is unavailable.`);
  }
  return args;
}

export async function ensureActionInvocationResultTypes(client, description, plan, signal, columns = []) {
  await ensureType(client, description, plan?.field?.type, signal);
  const describedType = description.types.get(namedType(plan?.field?.type)) ?? null;
  const resultsField = describedType ? fieldsByName(describedType).get('results') ?? null : null;
  const resultType = resultsField?.type ?? plan?.field?.type ?? null;
  if (resultsField) await ensureType(client, description, resultType, signal);
  const resultDescription = description.types.get(namedType(resultType)) ?? null;
  for (const column of [...(columns ?? [])].slice(0, 32)) {
    const member = typeof column === 'string' ? column : column?.member;
    const memberField = member ? fieldsByName(resultDescription).get(member) ?? null : null;
    if (!memberField) continue;
    await ensureType(client, description, memberField.type, signal);
    const wrapper = description.types.get(namedType(memberField.type)) ?? null;
    const getField = fieldsByName(wrapper).get('get') ?? null;
    if (getField) await ensureType(client, description, getField.type, signal);
  }
}

export function actionInvocationResultPlan(plan, types, columns = []) {
  if (!plan?.supported || !plan.field) {
    throw new ActionDispatchPlanError('ACTION_INVOCATION_UNAVAILABLE', 'No executable action invocation is advertised.');
  }
  const directType = plan.field.type;
  const describedType = types.get(namedType(directType)) ?? null;
  const resultsField = describedType ? fieldsByName(describedType).get('results') ?? null : null;
  if (resultsField) {
    return Object.freeze({
      selection: Object.freeze({results: columns.length > 0
        ? collectionResultSelectionForType(resultsField.type, columns, types)
        : resultSelectionForType(resultsField.type, types) ?? true}),
      resultType: resultsField.type,
      extractionPath: Object.freeze(['results'])
    });
  }
  return Object.freeze({
    selection: columns.length > 0
      ? collectionResultSelectionForType(directType, columns, types)
      : resultSelectionForType(directType, types) ?? true,
    resultType: directType,
    extractionPath: Object.freeze([])
  });
}

export function extractActionInvocationResult(value, resultPlan) {
  let current = value;
  for (const segment of resultPlan?.extractionPath ?? []) {
    current = current?.[segment] ?? null;
  }
  return current;
}

export function secureActionInvocationResult(result, parameters = []) {
  if (!parameters.some(parameter => isProtectedType(parameter.inputType))) {
    return result;
  }
  const operation = result?.operation
    ? Object.freeze({
        ...result.operation,
        variables: Object.freeze(Object.fromEntries(
          Object.keys(result.operation.variables ?? {}).map(name => [name, '<redacted>'])))
      })
    : null;
  const errors = Object.freeze((result?.errors ?? []).map(error => Object.freeze({
    message: 'Protected action input was not accepted.',
    path: Object.freeze([...(error?.path ?? [])]),
    extensions: Object.freeze({...error?.extensions, code: error?.extensions?.code ?? 'PROTECTED_ACTION_INPUT_FAILED'})
  })));
  return Object.freeze({...result, data: null, operation, errors});
}

async function ensureType(client, description, typeRef, signal) {
  const typeName = namedType(typeRef);
  if (!typeName || ['SCALAR', 'ENUM'].includes(innermostKind(typeRef))) {
    return;
  }
  if (!description.types.has(typeName)) {
    const described = await client.describeTypes([typeName], {signal});
    const type = described.get(typeName);
    if (type) {
      description.types.set(typeName, type);
    }
  }
  const type = description.types.get(typeName);
  const metadataTypeName = namedType(fieldsByName(type).get('_meta')?.type);
  if (metadataTypeName && !description.types.has(metadataTypeName)) {
    const described = await client.describeTypes([metadataTypeName], {signal});
    const metadataType = described.get(metadataTypeName);
    if (metadataType) {
      description.types.set(metadataTypeName, metadataType);
    }
  }
}

function innermostKind(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current?.kind ?? null;
}

function isProtectedType(typeRef) {
  const typeName = namedType(typeRef)?.toLocaleLowerCase() ?? '';
  return typeName.includes('password') || typeName.includes('protected');
}

function findTargetArgument(field, generatedInputTypeName) {
  return field.args?.find(argument => namedType(argument.type) === generatedInputTypeName)?.name
    ?? field.args?.find(argument => argument.name === '_target')?.name
    ?? null;
}
