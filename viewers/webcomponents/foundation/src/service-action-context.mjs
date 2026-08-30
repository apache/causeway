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

import {
  actionInvocationArguments,
  actionInvocationResultPlan,
  createActionInvocationPlan,
  ensureActionInvocationResultTypes,
  extractActionInvocationResult,
  secureActionInvocationResult
} from './action-dispatch.mjs';
import {fieldsByName, namedType} from './introspection.mjs';
import {
  argumentsFromValues,
  autoCompleteWindowPlan,
  commandSelection,
  normalizeAutoCompleteWindow,
  resultSelectionForType
} from './interaction-operations.mjs';
import {InteractionResultKind, InteractionStatus} from './types.mjs';

export class ServiceActionContextController {
  constructor({
    client,
    logicalTypeName,
    serializeMutation = execute => execute(),
    onChanged = () => {}
  } = {}) {
    if (!client) {
      throw new Error('A Causeway GraphQL client is required for service actions.');
    }
    if (!logicalTypeName) {
      throw new Error('A public service logical type is required.');
    }
    this.client = client;
    this.logicalTypeName = logicalTypeName;
    this.identity = null;
    this.interactionTarget = Object.freeze({kind: 'service', logicalTypeName});
    this.serializeMutation = serializeMutation;
    this.onChanged = onChanged;
    this.description = null;
    this.capabilities = new Map();
    this.transientRequests = new Map();
    this.closed = false;
  }

  async describeService() {
    if (this.closed) {
      throw new Error('Cannot describe a disconnected service-action context.');
    }
    this.description ??= await this.client.describeService(this.logicalTypeName);
    return this.description;
  }

  async loadActionStates(actionIds, {signal} = {}) {
    const description = await this.describeService();
    const selection = {};
    const states = new Map();
    for (const actionId of [...new Set(actionIds)]) {
      const descriptor = description.members.get(actionId);
      if (!descriptor || descriptor.kind !== 'action') {
        states.set(actionId, Object.freeze({hidden: false, disabled: null, error: 'SERVICE_ACTION_UNAVAILABLE'}));
        continue;
      }
      const actionSelection = {};
      if (descriptor.fields.has('hidden')) {
        actionSelection.hidden = true;
      }
      if (descriptor.fields.has('disabled')) {
        actionSelection.disabled = true;
      }
      const metadataFields = ['areYouSure', 'promptStyle'].filter(fieldName => descriptor.metadata?.fields.has(fieldName));
      if (metadataFields.length > 0) {
        actionSelection.metadata = Object.fromEntries(metadataFields.map(fieldName => [fieldName, true]));
      }
      if (Object.keys(actionSelection).length > 0) {
        selection[actionId] = actionSelection;
      } else {
        states.set(actionId, Object.freeze({hidden: false, disabled: null, error: null}));
      }
    }
    if (Object.keys(selection).length === 0) {
      return Object.freeze({states, errors: Object.freeze([]), operation: null});
    }
    const result = await this.client.executeServiceInteraction({
      description,
      selection,
      operationName: 'CausewayReadServiceActionStates',
      signal
    });
    for (const actionId of Object.keys(selection)) {
      const error = result.errors?.find(candidate => candidate.path?.includes(actionId));
      const data = result.data?.[actionId] ?? null;
      states.set(actionId, Object.freeze({
        hidden: data == null || data.hidden === true,
        disabled: typeof data?.disabled === 'string' && data.disabled.length > 0 ? data.disabled : null,
        error: error ? 'SERVICE_ACTION_STATE_FAILED' : null,
        metadata: data?.metadata ?? null
      }));
    }
    return Object.freeze({states, errors: result.errors ?? Object.freeze([]), operation: result.operation});
  }

  describeActionInteraction(actionId) {
    if (!this.capabilities.has(actionId)) {
      const promise = this.#describeActionInteraction(actionId).catch(error => {
        this.capabilities.delete(actionId);
        throw error;
      });
      this.capabilities.set(actionId, promise);
    }
    return this.capabilities.get(actionId);
  }

  async prepareAction(actionId, values = {}, {signal} = {}) {
    const capabilities = await this.describeActionInteraction(actionId);
    if (!capabilities.invokable) {
      return interactionResult(
        capabilities.planningError ? InteractionStatus.FAILED : InteractionStatus.UNSUPPORTED,
        {capabilities},
        [capabilities.planningError ?? commandError(`Service action '${actionId}' does not expose an invocation capability.`)]);
    }
    if (capabilities.parameters.length === 0) {
      return interactionResult(InteractionStatus.SUCCESS, {capabilities, parameters: []});
    }
    return this.#runTransient(`action:${actionId}:prepare`, signal, async commandSignal => {
      const description = await this.describeService();
      const parameterSelection = {};
      for (const parameter of capabilities.parameters) {
        const stateSelection = {};
        for (const fieldName of ['hidden', 'disabled', 'default', 'choices', 'validity', 'datatype']) {
          const field = parameter.fields.get(fieldName);
          if (!field) {
            continue;
          }
          const args = argumentsFromValues(field, values);
          const resultSelection = resultSelectionForType(field.type, description.types);
          stateSelection[fieldName] = Object.keys(args).length > 0
            ? commandSelection(args, resultSelection ?? true)
            : resultSelection ?? true;
        }
        parameterSelection[parameter.id] = stateSelection;
      }
      const result = await this.client.executeServiceInteraction({
        description,
        selection: {[actionId]: {params: parameterSelection}},
        operationName: 'CausewayPrepareServiceAction',
        signal: commandSignal
      });
      const parameterData = result.data?.[actionId]?.params ?? null;
      if (!parameterData && result.errors?.length) {
        return commandResponse(result, null);
      }
      return interactionResult(InteractionStatus.SUCCESS, {
        capabilities,
        parameters: Object.freeze(capabilities.parameters.map(parameter => {
          const parameterError = result.errors?.find(error => error.path?.includes(parameter.id));
          return Object.freeze({
            ...parameter,
            state: Object.freeze({
              ...(parameterData?.[parameter.id] ?? {}),
              ...(parameterError ? {error: parameterError.message} : {})
            })
          });
        }))
      }, result.errors, result.operation);
    });
  }

  async autoCompleteActionParameter(actionId, parameterId, search, values = {}, {signal} = {}) {
    return this.#runTransient(`action:${actionId}:${parameterId}:autocomplete`, signal, async commandSignal => {
      const description = await this.describeService();
      const capabilities = await this.describeActionInteraction(actionId);
      const parameter = capabilities.parameters.find(candidate => candidate.id === parameterId);
      const field = parameter?.fields.get('autoComplete');
      if (!field) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Service-action parameter '${actionId}.${parameterId}' does not expose autocomplete.`)]);
      }
      const args = argumentsFromValues(field, values);
      const searchArgument = field.args.find(argument => argument.name === 'search')?.name ?? field.args.at(-1)?.name;
      if (searchArgument) {
        args[searchArgument] = search;
      }
      await ensureResultTypes(this.client, description, field.type, commandSignal);
      const selection = resultSelectionForType(field.type, description.types);
      const result = await this.client.executeServiceInteraction({
        description,
        selection: {
          [actionId]: {
            params: {
              [parameterId]: {
                autoComplete: commandSelection(args, selection ?? true)
              }
            }
          }
        },
        operationName: 'CausewayServiceActionParameterAutoComplete',
        signal: commandSignal
      });
      return commandResponse(result, result.data?.[actionId]?.params?.[parameterId]?.autoComplete ?? null);
    });
  }

  async autoCompleteActionParameterWindow(
    actionId,
    parameterId,
    search,
    values = {},
    {offset = 0, size = null, signal} = {}
  ) {
    const description = await this.describeService();
    const capabilities = await this.describeActionInteraction(actionId);
    const parameter = capabilities.parameters.find(candidate => candidate.id === parameterId);
    const field = parameter?.fields.get('autoCompleteWindow');
    if (!field) {
      const legacy = await this.autoCompleteActionParameter(actionId, parameterId, search, values, {signal});
      return legacy.status === InteractionStatus.SUCCESS
        ? interactionResult(legacy.status, normalizeAutoCompleteWindow(legacy.data, {
            legacy: true, offset, requestedSize: size
          }), legacy.errors, legacy.operation)
        : legacy;
    }
    return this.#runTransient(`action:${actionId}:${parameterId}:autocomplete`, signal, async commandSignal => {
      const plan = autoCompleteWindowPlan(field, description.types);
      if (!plan) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(
          `Service-action parameter '${actionId}.${parameterId}' exposes an incomplete autocomplete window.`)]);
      }
      const args = {
        ...argumentsFromValues(field, values),
        ...windowArguments(field, search, offset, size)
      };
      const result = await this.client.executeServiceInteraction({
        description,
        selection: {
          [actionId]: {params: {[parameterId]: {autoCompleteWindow: commandSelection(args, plan.selection)}}}
        },
        operationName: 'CausewayServiceActionParameterAutoCompleteWindow',
        signal: commandSignal
      });
      return commandResponse(result, normalizeAutoCompleteWindow(
        result.data?.[actionId]?.params?.[parameterId]?.autoCompleteWindow ?? null,
        {offset, requestedSize: size ?? plan.sizeDefault}
      ));
    });
  }

  async validateAction(actionId, values = {}, {signal} = {}) {
    return this.#runTransient(`action:${actionId}:validate`, signal, async commandSignal => {
      const description = await this.describeService();
      const descriptor = description.members.get(actionId);
      const field = descriptor?.fields.get('validate');
      if (!field) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Service action '${actionId}' does not expose argument validation.`)]);
      }
      const result = await this.client.executeServiceInteraction({
        description,
        selection: {[actionId]: {validate: commandSelection(argumentsFromValues(field, values))}},
        operationName: 'CausewayValidateServiceAction',
        signal: commandSignal
      });
      return commandResponse(result, result.data?.[actionId]?.validate ?? null);
    });
  }

  async invokeAction(actionId, values = {}, {signal} = {}) {
    const capabilities = await this.describeActionInteraction(actionId);
    if (!capabilities.invokable) {
      return interactionResult(
        capabilities.planningError ? InteractionStatus.FAILED : InteractionStatus.UNSUPPORTED,
        null,
        [capabilities.planningError ?? commandError(`Service action '${actionId}' does not expose an invocation capability.`)]);
    }
    const execute = async () => {
      try {
        const description = await this.describeService();
        const plan = capabilities.invocationPlan;
        await ensureActionInvocationResultTypes(this.client, description, plan, signal);
        const resultPlan = actionInvocationResultPlan(plan, description.types);
        const args = actionInvocationArguments(plan, values);
        let result;
        if (plan.placement === 'root-mutation') {
          const mutationType = await this.client.describeMutation({signal});
          result = await this.client.executeMutationInteraction({
            description,
            mutationType,
            fieldName: plan.mutationFieldName,
            args,
            resultSelection: resultPlan.selection === true ? null : resultPlan.selection,
            operationName: 'CausewayInvokeServiceAction',
            signal
          });
        } else {
          result = await this.client.executeServiceInteraction({
            description,
            selection: {
              [actionId]: {
                [plan.fieldName]: commandSelection(args, resultPlan.selection)
              }
            },
            operationName: 'CausewayInvokeServiceAction',
            signal
          });
          const invocationValue = result.data?.[actionId]?.[plan.fieldName] ?? null;
          result = {...result, data: extractActionInvocationResult(invocationValue, resultPlan)};
        }
        result = secureActionInvocationResult(result, capabilities.parameters);
        const response = commandResponse(result, normalizeActionResult(result.data, resultPlan.resultType));
        if (response.status === InteractionStatus.SUCCESS && plan.mutating) {
          this.onChanged();
        }
        return response;
      } catch (error) {
        if (signal?.aborted) {
          return interactionResult(InteractionStatus.OBSOLETE);
        }
        return interactionResult(InteractionStatus.FAILED, null, [commandError({
          message: 'The service action could not be dispatched safely.',
          extensions: {code: error?.code ?? 'ACTION_DISPATCH_FAILED'}
        })]);
      }
    };
    return capabilities.mutating ? this.serializeMutation(execute) : execute();
  }

  disconnect() {
    this.closed = true;
    for (const request of this.transientRequests.values()) {
      request.abortController.abort();
    }
    this.transientRequests.clear();
  }

  async #describeActionInteraction(actionId) {
    const description = await this.describeService();
    const descriptor = description.members.get(actionId);
    if (!descriptor || descriptor.kind !== 'action') {
      throw new Error(`Service action '${this.logicalTypeName}.${actionId}' is unavailable.`);
    }
    const paramsField = descriptor.fields.get('params');
    const paramsType = paramsField ? description.types.get(namedType(paramsField.type)) ?? null : null;
    const validateField = descriptor.fields.get('validate');
    const parameters = (paramsType?.fields ?? []).map(field => {
      const wrapper = description.types.get(namedType(field.type)) ?? null;
      const actionArgument = validateField?.args.find(argument => argument.name === field.name) ?? null;
      const inputType = actionArgument?.type ?? parameterInputType(wrapper);
      const enumType = inputType ? description.types.get(namedType(inputType)) ?? null : null;
      const fields = fieldsByName(wrapper);
      return Object.freeze({
        id: field.name,
        description: field.description ?? null,
        generatedTypeName: wrapper?.name ?? namedType(field.type),
        fields,
        autoCompleteWindow: autoCompleteWindowPlan(fields.get('autoCompleteWindow'), description.types),
        inputType,
        enumValues: Object.freeze(enumType?.enumValues?.map(value => value.name) ?? [])
      });
    });
    const mutationType = await this.client.describeMutation();
    const mutationFieldName = `${description.generatedFieldName}__${actionId}`;
    let invocationPlan;
    let planningError = null;
    try {
      invocationPlan = createActionInvocationPlan({
        targetKind: 'service',
        description,
        descriptor,
        mutationType,
        mutationFieldName
      });
    } catch (error) {
      invocationPlan = Object.freeze({supported: false});
      planningError = commandError({
        message: 'The advertised service action cannot be dispatched safely.',
        extensions: {code: error?.code ?? 'ACTION_DISPATCH_PLAN_FAILED'}
      });
    }
    return Object.freeze({
      descriptor,
      parameters: Object.freeze(parameters),
      validate: Boolean(validateField),
      invocationPlan,
      invocationField: invocationPlan.placement === 'root-mutation' ? null : invocationPlan.field ?? null,
      mutationFieldName: invocationPlan.mutationFieldName ?? null,
      mutating: invocationPlan.mutating ?? false,
      planningError,
      invokable: invocationPlan.supported === true
    });
  }

  async #runTransient(key, signal, execute) {
    const previous = this.transientRequests.get(key);
    previous?.abortController.abort();
    const generation = (previous?.generation ?? 0) + 1;
    const abortController = new AbortController();
    const abort = () => abortController.abort();
    signal?.addEventListener?.('abort', abort, {once: true});
    if (signal?.aborted) {
      abortController.abort();
    }
    this.transientRequests.set(key, {generation, abortController});
    try {
      const result = await execute(abortController.signal);
      if (this.transientRequests.get(key)?.generation !== generation) {
        return interactionResult(InteractionStatus.OBSOLETE);
      }
      return result;
    } catch (error) {
      if (error?.name === 'AbortError') {
        return interactionResult(InteractionStatus.OBSOLETE);
      }
      return interactionResult(InteractionStatus.FAILED, null, [commandError(error)]);
    } finally {
      signal?.removeEventListener?.('abort', abort);
      if (this.transientRequests.get(key)?.generation === generation) {
        this.transientRequests.delete(key);
      }
    }
  }
}

async function ensureResultTypes(client, description, typeRef, signal) {
  const resultTypeName = namedType(typeRef);
  if (!resultTypeName || description.types.has(resultTypeName)) {
    return;
  }
  const resultTypes = await client.describeTypes([resultTypeName], {signal});
  const resultType = resultTypes.get(resultTypeName);
  if (!resultType) {
    return;
  }
  description.types.set(resultTypeName, resultType);
  const metadataTypeName = namedType(fieldsByName(resultType).get('_meta')?.type);
  if (metadataTypeName && !description.types.has(metadataTypeName)) {
    const metadataTypes = await client.describeTypes([metadataTypeName], {signal});
    const metadataType = metadataTypes.get(metadataTypeName);
    if (metadataType) {
      description.types.set(metadataTypeName, metadataType);
    }
  }
}

function windowArguments(field, search, offset, size) {
  const values = {search, offset};
  if (size != null) values.size = size;
  return argumentsFromValues(field, values);
}

function parameterInputType(wrapper) {
  if (!wrapper) {
    return null;
  }
  for (const fieldName of ['validity', 'default', 'choices', 'autoComplete']) {
    const field = fieldsByName(wrapper).get(fieldName);
    const type = field?.args.at(-1)?.type;
    if (type) {
      return type;
    }
  }
  return null;
}

function commandResponse(result, data) {
  if (result.errors?.length) {
    return interactionResult(InteractionStatus.FAILED, data, result.errors, result.operation);
  }
  return interactionResult(InteractionStatus.SUCCESS, data, [], result.operation);
}

function interactionResult(status, data = null, errors = [], operation = null) {
  return Object.freeze({
    status,
    data,
    errors: Object.freeze([...(errors ?? [])]),
    operation
  });
}

function normalizeActionResult(value, typeRef) {
  const inner = innermostType(typeRef);
  const list = unwrapNonNull(typeRef)?.kind === 'LIST';
  if (value == null) {
    return Object.freeze({kind: InteractionResultKind.VOID, value: null});
  }
  if (list || Array.isArray(value)) {
    return Object.freeze({kind: InteractionResultKind.COLLECTION, value});
  }
  if (inner?.kind === 'SCALAR' || inner?.kind === 'ENUM' || typeof value !== 'object') {
    return Object.freeze({kind: InteractionResultKind.SCALAR, value});
  }
  return Object.freeze({kind: InteractionResultKind.OBJECT, value});
}

function unwrapNonNull(typeRef) {
  let current = typeRef;
  while (current?.kind === 'NON_NULL') {
    current = current.ofType;
  }
  return current;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
}

function commandError(error) {
  if (typeof error === 'string') {
    return Object.freeze({message: error, path: [], extensions: Object.freeze({})});
  }
  return Object.freeze({
    message: error?.message ?? String(error),
    path: Object.freeze([...(error?.path ?? [])]),
    extensions: Object.freeze({...error?.extensions})
  });
}
