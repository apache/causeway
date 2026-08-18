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

import {createSemanticEvent, OBJECT_CONTEXT_STATE_EVENT} from './context-events.mjs';
import {fieldsByName, namedType} from './introspection.mjs';
import {commandSelection, resultSelectionForType} from './interaction-operations.mjs';
import {deepMerge, differenceSelection, isSelectionEmpty, mergeSelections} from './selection.mjs';
import {InteractionResultKind, InteractionStatus, ObjectContextStatus, RequirementStatus} from './types.mjs';

const MAX_STRUCTURAL_RESOURCE_CHARACTERS = 1_048_576;

export class StructuralResourceError extends Error {
  constructor(code, message, {status = null} = {}) {
    super(message);
    this.name = 'StructuralResourceError';
    this.code = code;
    this.status = status;
  }
}

export class ObjectContextController extends EventTarget {
  constructor({
    client,
    logicalTypeName,
    objectId,
    hydration = null,
    fetchImpl = globalThis.fetch,
    schedule = callback => globalThis.queueMicrotask(callback)
  } = {}) {
    super();
    this.client = client ?? null;
    this.identity = Object.freeze({logicalTypeName: logicalTypeName ?? '', id: objectId ?? ''});
    this.fetchImpl = fetchImpl;
    this.schedule = callback => schedule(callback);
    this.description = hydration?.description ?? null;
    this.snapshot = hydration?.data
      ? deepFreeze({
        data: hydration.data,
        selection: hydration.selection ?? {},
        errors: hydration.errors ?? [],
        generation: hydration.generation ?? 0
      })
      : null;
    this.secondaryCache = new Map();
    this.secondaryAbortControllers = new Set();
    this.secondaryRequests = new Map();
    this.secondaryGeneration = 0;
    this.commandGenerations = new Map();
    this.commandAbortControllers = new Map();
    this.mutationTail = Promise.resolve();
    this.registrations = new Map();
    this.stateListeners = new Set();
    this.scheduled = false;
    this.revision = 0;
    this.generation = 0;
    this.forceFull = false;
    this.abortController = null;
    this.closed = false;
    this.state = freezeState({
      status: ObjectContextStatus.IDLE,
      generation: 0,
      snapshot: null,
      errors: [],
      error: null
    });
    const identityError = validateIdentity(this.identity, this.client);
    if (identityError) {
      this.#setState({
        status: ObjectContextStatus.TERMINAL_ERROR,
        error: identityError,
        errors: [asGraphQLError(identityError)]
      });
    }
  }

  registerRequirement(requirement, listener = () => {}) {
    if (this.closed) {
      throw new Error('Cannot register a requirement on a disconnected object context.');
    }
    const normalized = normalizeRequirement(requirement);
    const token = Symbol(`${normalized.kind}:${normalized.member ?? ''}`);
    this.registrations.set(token, {requirement: normalized, listener, error: null, descriptor: null});
    this.#notifyRegistration(this.registrations.get(token));
    this.#scheduleFlush();
    let released = false;
    return () => {
      if (!released) {
        released = true;
        this.registrations.delete(token);
        this.#scheduleFlush();
      }
    };
  }

  async describeObject() {
    if (this.closed) {
      throw new Error('Cannot describe an object using a disconnected object context.');
    }
    const description = this.description ?? await this.client.describeObject(this.identity.logicalTypeName);
    this.description ??= description;
    return description;
  }

  async loadStructuralResource(resourcePath, {accept = 'application/xml', signal} = {}) {
    if (this.closed) {
      throw new StructuralResourceError('CONTEXT_DISCONNECTED', 'Cannot load a structural resource using a disconnected object context.');
    }
    if (!isSafeStructuralResourcePath(resourcePath)) {
      throw new StructuralResourceError('INVALID_RESOURCE_PATH', 'A structural resource must be an opaque origin-relative path beginning with exactly one slash.');
    }
    if (typeof this.fetchImpl !== 'function') {
      throw new StructuralResourceError('FETCH_UNAVAILABLE', 'No Fetch API implementation is available for structural resources.');
    }
    const abortController = new AbortController();
    this.secondaryAbortControllers.add(abortController);
    const abort = () => abortController.abort();
    signal?.addEventListener?.('abort', abort, {once: true});
    if (signal?.aborted) {
      abortController.abort();
    }
    try {
      const fetchImpl = this.fetchImpl;
      const response = await fetchImpl(resourcePath, {
        method: 'GET',
        headers: {accept},
        credentials: 'same-origin',
        cache: 'no-store',
        redirect: 'error',
        signal: abortController.signal
      });
      if (!response?.ok) {
        throw new StructuralResourceError(
          'RESOURCE_REQUEST_FAILED',
          `Structural resource request failed with HTTP ${response?.status ?? 'unknown'}.`,
          {status: response?.status ?? null}
        );
      }
      const contentLength = Number(response.headers?.get?.('content-length'));
      if (Number.isFinite(contentLength) && contentLength > MAX_STRUCTURAL_RESOURCE_CHARACTERS) {
        throw new StructuralResourceError('RESOURCE_TOO_LARGE', `Structural resource exceeds ${MAX_STRUCTURAL_RESOURCE_CHARACTERS} characters.`);
      }
      const text = await response.text();
      if (text.length > MAX_STRUCTURAL_RESOURCE_CHARACTERS) {
        throw new StructuralResourceError('RESOURCE_TOO_LARGE', `Structural resource exceeds ${MAX_STRUCTURAL_RESOURCE_CHARACTERS} characters.`);
      }
      return Object.freeze({
        path: resourcePath,
        mediaType: response.headers?.get?.('content-type') ?? null,
        text
      });
    } catch (error) {
      if (error?.name === 'AbortError' || error instanceof StructuralResourceError) {
        throw error;
      }
      throw new StructuralResourceError('RESOURCE_REQUEST_FAILED', 'The structural resource request failed.');
    } finally {
      signal?.removeEventListener?.('abort', abort);
      this.secondaryAbortControllers.delete(abortController);
    }
  }

  async describePropertyInteraction(member) {
    const {description, descriptor} = await this.#memberDescriptor(member, 'property');
    const mutationType = await this.client.describeMutation();
    const mutationFieldName = mutationFieldNameFor(description, member);
    const mutationField = mutationType ? fieldsByName(mutationType).get(mutationFieldName) ?? null : null;
    const enumValues = descriptor.value?.typeDescription?.enumValues?.map(value => value.name) ?? [];
    return Object.freeze({
      descriptor,
      editable: descriptor.fields.has('set') || Boolean(mutationField),
      validate: descriptor.fields.has('validate'),
      choices: descriptor.fields.has('choices'),
      autoComplete: descriptor.fields.has('autoComplete'),
      mutationFieldName: mutationField ? mutationFieldName : null,
      inputType: propertyInputType(descriptor, mutationField, member),
      enumValues: Object.freeze(enumValues)
    });
  }

  async prepareProperty(member, {signal} = {}) {
    const capabilities = await this.describePropertyInteraction(member);
    if (!capabilities.editable) {
      return interactionResult(InteractionStatus.UNSUPPORTED, {capabilities}, [commandError(`Property '${member}' does not expose an update capability.`)]);
    }
    let choices = capabilities.enumValues;
    if (capabilities.choices) {
      const result = await this.propertyChoices(member, {signal});
      if (result.status === InteractionStatus.SUCCESS) {
        choices = result.data ?? choices;
      }
    }
    return interactionResult(InteractionStatus.SUCCESS, {
      capabilities,
      choices: Object.freeze([...(choices ?? [])])
    });
  }

  async propertyChoices(member, {signal} = {}) {
    return this.#runTransient(`property:${member}:choices`, signal, async commandSignal => {
      const {description, descriptor} = await this.#memberDescriptor(member, 'property');
      const field = descriptor.fields.get('choices');
      if (!field) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Property '${member}' does not expose choices.`)]);
      }
      await ensureResultTypes(this.client, description, field.type, commandSignal);
      const selection = resultSelectionForType(field.type, description.types);
      const result = await this.client.executeObjectInteraction({
        description,
        identity: this.identity,
        selection: {[member]: {choices: selection ?? true}},
        operationName: 'CausewayPropertyChoices',
        signal: commandSignal
      });
      return commandResponse(result, result.data?.[member]?.choices ?? null);
    });
  }

  async autoCompleteProperty(member, search, {signal} = {}) {
    return this.#runTransient(`property:${member}:autocomplete`, signal, async commandSignal => {
      const {description, descriptor} = await this.#memberDescriptor(member, 'property');
      const field = descriptor.fields.get('autoComplete');
      if (!field) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Property '${member}' does not expose autocomplete.`)]);
      }
      const searchArgument = field.args.find(argument => argument.name === 'search')?.name ?? 'search';
      await ensureResultTypes(this.client, description, field.type, commandSignal);
      const selection = resultSelectionForType(field.type, description.types);
      const result = await this.client.executeObjectInteraction({
        description,
        identity: this.identity,
        selection: {[member]: {autoComplete: commandSelection({[searchArgument]: search}, selection ?? true)}},
        operationName: 'CausewayPropertyAutoComplete',
        signal: commandSignal
      });
      return commandResponse(result, result.data?.[member]?.autoComplete ?? null);
    });
  }

  async validateProperty(member, value, {signal} = {}) {
    return this.#runTransient(`property:${member}:validate`, signal, async commandSignal => {
      const {description, descriptor} = await this.#memberDescriptor(member, 'property');
      const field = descriptor.fields.get('validate');
      if (!field) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Property '${member}' does not expose validation.`)]);
      }
      const argumentName = field.args[0]?.name ?? member;
      const result = await this.client.executeObjectInteraction({
        description,
        identity: this.identity,
        selection: {[member]: {validate: commandSelection({[argumentName]: value})}},
        operationName: 'CausewayValidateProperty',
        signal: commandSignal
      });
      return commandResponse(result, result.data?.[member]?.validate ?? null);
    });
  }

  updateProperty(member, value, {signal} = {}) {
    return this.#serializeMutation(async () => {
      const {description, descriptor} = await this.#memberDescriptor(member, 'property');
      const setField = descriptor.fields.get('set');
      let result;
      if (setField) {
        const argumentName = setField.args[0]?.name ?? member;
        const selection = resultSelectionForType(setField.type, description.types) ?? {__typename: true};
        result = await this.client.executeObjectInteraction({
          description,
          identity: this.identity,
          selection: {[member]: {set: commandSelection({[argumentName]: value}, selection)}},
          operationName: 'CausewayUpdateProperty',
          signal
        });
        result = {...result, data: result.data?.[member]?.set ?? null};
      } else {
        const mutationType = await this.client.describeMutation({signal});
        const fieldName = mutationFieldNameFor(description, member);
        const mutationField = mutationType ? fieldsByName(mutationType).get(fieldName) : null;
        if (!mutationField) {
          return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Property '${member}' does not expose an update mutation.`)]);
        }
        const targetArgument = targetArgumentName(mutationField, description.generatedInputTypeName);
        const selection = resultSelectionForType(mutationField.type, description.types) ?? {__typename: true};
        result = await this.client.executeMutationInteraction({
          description,
          mutationType,
          fieldName,
          args: {[targetArgument]: {id: this.identity.id}, [member]: value},
          resultSelection: selection,
          operationName: 'CausewayUpdateProperty',
          signal
        });
      }
      const response = commandResponse(result, result.data);
      if (response.status === InteractionStatus.SUCCESS) {
        this.refresh();
      }
      return response;
    });
  }

  async describeActionInteraction(member) {
    const {description, descriptor} = await this.#memberDescriptor(member, 'action');
    const paramsField = descriptor.fields.get('params');
    const paramsType = paramsField ? description.types.get(namedType(paramsField.type)) ?? null : null;
    const validateField = descriptor.fields.get('validate');
    const parameters = (paramsType?.fields ?? []).map(field => {
      const wrapper = description.types.get(namedType(field.type)) ?? null;
      const actionArgument = validateField?.args.find(argument => argument.name === field.name) ?? null;
      const inputType = actionArgument?.type ?? parameterInputType(wrapper);
      const enumType = inputType ? description.types.get(namedType(inputType)) ?? null : null;
      return Object.freeze({
        id: field.name,
        description: field.description ?? null,
        generatedTypeName: wrapper?.name ?? namedType(field.type),
        fields: fieldsByName(wrapper),
        inputType,
        enumValues: Object.freeze(enumType?.enumValues?.map(value => value.name) ?? [])
      });
    });
    const invocationField = actionInvocationField(descriptor);
    const mutationType = invocationField ? null : await this.client.describeMutation();
    const mutationFieldName = mutationFieldNameFor(description, member);
    const mutationField = mutationType ? fieldsByName(mutationType).get(mutationFieldName) ?? null : null;
    return Object.freeze({
      descriptor,
      parameters: Object.freeze(parameters),
      validate: Boolean(validateField),
      invocationField: invocationField?.name ?? null,
      mutationFieldName: mutationField ? mutationFieldName : null,
      invokable: Boolean(invocationField || mutationField)
    });
  }

  async prepareAction(member, values = {}, {signal} = {}) {
    const capabilities = await this.describeActionInteraction(member);
    if (!capabilities.invokable) {
      return interactionResult(InteractionStatus.UNSUPPORTED, {capabilities}, [commandError(`Action '${member}' does not expose an invocation capability.`)]);
    }
    if (capabilities.parameters.length === 0) {
      return interactionResult(InteractionStatus.SUCCESS, {capabilities, parameters: []});
    }
    return this.#runTransient(`action:${member}:prepare`, signal, async commandSignal => {
      const {description} = await this.#memberDescriptor(member, 'action');
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
      const result = await this.client.executeObjectInteraction({
        description,
        identity: this.identity,
        selection: {[member]: {params: parameterSelection}},
        operationName: 'CausewayPrepareAction',
        signal: commandSignal
      });
      const parameterData = result.data?.[member]?.params ?? null;
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

  async autoCompleteActionParameter(member, parameterId, search, values = {}, {signal} = {}) {
    return this.#runTransient(`action:${member}:${parameterId}:autocomplete`, signal, async commandSignal => {
      const {description} = await this.#memberDescriptor(member, 'action');
      const capabilities = await this.describeActionInteraction(member);
      const parameter = capabilities.parameters.find(candidate => candidate.id === parameterId);
      const field = parameter?.fields.get('autoComplete');
      if (!field) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Action parameter '${member}.${parameterId}' does not expose autocomplete.`)]);
      }
      const args = argumentsFromValues(field, values);
      const searchArgument = field.args.find(argument => argument.name === 'search')?.name ?? field.args.at(-1)?.name;
      if (searchArgument) {
        args[searchArgument] = search;
      }
      await ensureResultTypes(this.client, description, field.type, commandSignal);
      const selection = resultSelectionForType(field.type, description.types);
      const result = await this.client.executeObjectInteraction({
        description,
        identity: this.identity,
        selection: {
          [member]: {
            params: {
              [parameterId]: {
                autoComplete: commandSelection(args, selection ?? true)
              }
            }
          }
        },
        operationName: 'CausewayActionParameterAutoComplete',
        signal: commandSignal
      });
      return commandResponse(result, result.data?.[member]?.params?.[parameterId]?.autoComplete ?? null);
    });
  }

  async validateAction(member, values = {}, {signal} = {}) {
    return this.#runTransient(`action:${member}:validate`, signal, async commandSignal => {
      const {description, descriptor} = await this.#memberDescriptor(member, 'action');
      const field = descriptor.fields.get('validate');
      if (!field) {
        return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Action '${member}' does not expose argument validation.`)]);
      }
      const result = await this.client.executeObjectInteraction({
        description,
        identity: this.identity,
        selection: {[member]: {validate: commandSelection(argumentsFromValues(field, values))}},
        operationName: 'CausewayValidateAction',
        signal: commandSignal
      });
      return commandResponse(result, result.data?.[member]?.validate ?? null);
    });
  }

  invokeAction(member, values = {}, {signal} = {}) {
    return this.#serializeMutation(async () => {
      const {description, descriptor} = await this.#memberDescriptor(member, 'action');
      const invocationField = actionInvocationField(descriptor);
      let result;
      let resultType;
      if (invocationField) {
        const invokeType = description.types.get(namedType(invocationField.type));
        const resultsField = invokeType ? fieldsByName(invokeType).get('results') : null;
        if (resultsField) {
          await ensureResultTypes(this.client, description, resultsField.type, signal);
        }
        const resultsSelection = resultsField ? resultSelectionForType(resultsField.type, description.types) : null;
        result = await this.client.executeObjectInteraction({
          description,
          identity: this.identity,
          selection: {
            [member]: {
              [invocationField.name]: commandSelection(
                argumentsFromValues(invocationField, values),
                resultsField ? {results: resultsSelection ?? true} : {target: true}
              )
            }
          },
          operationName: 'CausewayInvokeAction',
          signal
        });
        resultType = resultsField?.type ?? null;
        result = {...result, data: result.data?.[member]?.[invocationField.name]?.results ?? null};
      } else {
        const mutationType = await this.client.describeMutation({signal});
        const fieldName = mutationFieldNameFor(description, member);
        const mutationField = mutationType ? fieldsByName(mutationType).get(fieldName) : null;
        if (!mutationField) {
          return interactionResult(InteractionStatus.UNSUPPORTED, null, [commandError(`Action '${member}' does not expose an invocation mutation.`)]);
        }
        const targetArgument = targetArgumentName(mutationField, description.generatedInputTypeName);
        const args = argumentsFromValues(mutationField, values);
        args[targetArgument] = {id: this.identity.id};
        resultType = mutationField.type;
        await ensureResultTypes(this.client, description, resultType, signal);
        result = await this.client.executeMutationInteraction({
          description,
          mutationType,
          fieldName,
          args,
          resultSelection: resultSelectionForType(resultType, description.types),
          operationName: 'CausewayInvokeAction',
          signal
        });
      }
      const response = commandResponse(result, normalizeActionResult(result.data, resultType));
      if (response.status === InteractionStatus.SUCCESS) {
        this.refresh();
      }
      return response;
    });
  }

  async loadCollection({
    member,
    columns = [],
    offset = 0,
    size = null,
    requestKey = null,
    force = false,
    signal
  } = {}) {
    if (this.closed) {
      throw new Error('Cannot load a collection from a disconnected object context.');
    }
    const description = this.description ?? await this.client.describeObject(this.identity.logicalTypeName);
    this.description ??= description;
    const descriptor = description.members.get(member);
    const usesWindow = descriptor?.kind === 'collection' && descriptor.window && descriptor.fields.has('window');
    if (!descriptor || descriptor.kind !== 'collection' || (!usesWindow && !descriptor.fields.has('get'))) {
      throw new Error(`Collection '${member}' is not readable on '${description.logicalTypeName}'.`);
    }
    const requestedOffset = usesWindow ? integerAtLeast(offset, 0, 'Collection window offset') : 0;
    const requestedSize = usesWindow
      ? integerAtLeast(size ?? descriptor.window.sizeDefault, 1, 'Collection window size')
      : null;
    const rowSelection = collectionRowSelection(columns);
    const selection = usesWindow
      ? null
      : {[member]: {get: rowSelection}};
    const cacheKey = JSON.stringify({member, rowSelection, offset: requestedOffset, size: requestedSize, usesWindow});
    if (!force && this.secondaryCache.has(cacheKey)) {
      const cached = this.secondaryCache.get(cacheKey);
      if (!cached.abortController.signal.aborted) {
        return cached.promise;
      }
      this.secondaryCache.delete(cacheKey);
    }

    const abortController = new AbortController();
    this.secondaryAbortControllers.add(abortController);
    const abort = () => abortController.abort();
    signal?.addEventListener?.('abort', abort, {once: true});
    if (signal?.aborted) {
      abortController.abort();
    }

    let requestGeneration = null;
    if (requestKey != null) {
      const previous = this.secondaryRequests.get(requestKey);
      previous?.abortController.abort();
      requestGeneration = ++this.secondaryGeneration;
      this.secondaryRequests.set(requestKey, {abortController, generation: requestGeneration});
    }

    const readPromise = usesWindow
      ? this.client.readCollectionWindow({
          description,
          identity: this.identity,
          member,
          rowSelection,
          offset: requestedOffset,
          size: requestedSize,
          signal: abortController.signal
        })
      : this.client.readObject({
          description,
          identity: this.identity,
          selection,
          signal: abortController.signal
        });
    const promise = readPromise.then(async result => {
      if (requestKey != null && requestGeneration !== this.secondaryRequests.get(requestKey)?.generation) {
        throw obsoleteRequestError();
      }
      const data = result.data?.[member] ?? null;
      const candidateRows = usesWindow
        ? data?.window?.rows
        : data?.get;
      const rows = Array.isArray(candidateRows) ? candidateRows : [];
      const firstObjectRow = rows.find?.(row => row?._meta?.logicalTypeName);
      let rowDescription = null;
      let errors = result.errors;
      if (firstObjectRow) {
        try {
          rowDescription = await this.client.describeObject(firstObjectRow._meta.logicalTypeName);
        } catch (error) {
          errors = Object.freeze([...errors, asGraphQLError(error)]);
        }
      }
      if (requestKey != null && requestGeneration !== this.secondaryRequests.get(requestKey)?.generation) {
        throw obsoleteRequestError();
      }
      return Object.freeze({
        descriptor,
        data,
        rows: Object.freeze([...rows]),
        window: usesWindow ? normalizeCollectionWindow(data?.window) : null,
        errors,
        rowDescription,
        rowSelection,
        selection,
        operation: result.operation
      });
    }).finally(() => {
      signal?.removeEventListener?.('abort', abort);
      this.secondaryAbortControllers.delete(abortController);
      if (requestKey != null && requestGeneration === this.secondaryRequests.get(requestKey)?.generation) {
        this.secondaryRequests.delete(requestKey);
      }
    });
    const cacheEntry = {promise, abortController};
    this.secondaryCache.set(cacheKey, cacheEntry);
    promise.catch(() => {
      if (this.secondaryCache.get(cacheKey) === cacheEntry) {
        this.secondaryCache.delete(cacheKey);
      }
    });
    return promise;
  }

  createHydratedRowContext(row, rowSelection = {}) {
    const metadata = row?._meta;
    if (!metadata?.logicalTypeName || !metadata?.id) {
      throw new Error('A hydrated row requires _meta.logicalTypeName and _meta.id.');
    }
    return new ObjectContextController({
      client: this.client,
      logicalTypeName: metadata.logicalTypeName,
      objectId: metadata.id,
      hydration: {data: row, selection: rowSelection},
      fetchImpl: this.fetchImpl
    });
  }

  subscribe(listener) {
    this.stateListeners.add(listener);
    listener(this.state);
    return () => this.stateListeners.delete(listener);
  }

  refresh() {
    this.forceFull = true;
    this.secondaryCache.clear();
    for (const abortController of this.secondaryAbortControllers) {
      abortController.abort();
    }
    this.secondaryRequests.clear();
    this.#scheduleFlush();
  }

  invalidate() {
    this.refresh();
  }

  disconnect() {
    this.closed = true;
    this.abortController?.abort();
    for (const abortController of this.secondaryAbortControllers) {
      abortController.abort();
    }
    this.secondaryAbortControllers.clear();
    this.secondaryRequests.clear();
    for (const abortController of this.commandAbortControllers.values()) {
      abortController.abort();
    }
    this.commandAbortControllers.clear();
    this.commandGenerations.clear();
    this.secondaryCache.clear();
    this.registrations.clear();
    this.stateListeners.clear();
  }

  async #memberDescriptor(member, kind) {
    if (this.closed) {
      throw new Error('Cannot execute a command on a disconnected object context.');
    }
    const description = this.description ?? await this.client.describeObject(this.identity.logicalTypeName);
    this.description ??= description;
    const descriptor = description.members.get(member);
    if (!descriptor || descriptor.kind !== kind) {
      throw new Error(`${kind[0].toUpperCase() + kind.slice(1)} '${member}' is not present on '${description.logicalTypeName}'.`);
    }
    return {description, descriptor};
  }

  async #runTransient(key, signal, execute) {
    const generation = (this.commandGenerations.get(key) ?? 0) + 1;
    this.commandGenerations.set(key, generation);
    this.commandAbortControllers.get(key)?.abort();
    const abortController = new AbortController();
    this.commandAbortControllers.set(key, abortController);
    const abort = () => abortController.abort();
    signal?.addEventListener?.('abort', abort, {once: true});
    try {
      const result = await execute(abortController.signal);
      if (generation !== this.commandGenerations.get(key)) {
        return interactionResult(InteractionStatus.OBSOLETE, null);
      }
      return result;
    } catch (error) {
      if (error?.name === 'AbortError' || generation !== this.commandGenerations.get(key)) {
        return interactionResult(InteractionStatus.OBSOLETE, null);
      }
      return interactionResult(InteractionStatus.FAILED, null, [commandError(error)]);
    } finally {
      signal?.removeEventListener?.('abort', abort);
      if (this.commandAbortControllers.get(key) === abortController) {
        this.commandAbortControllers.delete(key);
      }
    }
  }

  #serializeMutation(execute) {
    const run = this.mutationTail.then(execute, execute);
    this.mutationTail = run.catch(() => {});
    return run.catch(error => interactionResult(InteractionStatus.FAILED, null, [commandError(error)]));
  }

  #scheduleFlush() {
    if (this.closed || this.state.status === ObjectContextStatus.TERMINAL_ERROR && !this.description) {
      return;
    }
    this.revision += 1;
    if (this.scheduled) {
      return;
    }
    this.scheduled = true;
    this.schedule(() => {
      this.scheduled = false;
      void this.#flush(this.revision);
    });
  }

  async #flush(revision) {
    if (this.closed || this.registrations.size === 0) {
      return;
    }
    if (!this.description) {
      this.#setState({status: ObjectContextStatus.SCHEMA_LOADING, error: null, errors: []});
      try {
        const description = await this.client.describeObject(this.identity.logicalTypeName);
        if (this.closed || revision !== this.revision) {
          return;
        }
        this.description = description;
      } catch (error) {
        if (revision === this.revision) {
          this.#setState({
            status: ObjectContextStatus.TERMINAL_ERROR,
            error,
            errors: [asGraphQLError(error)]
          });
          this.#notifyAll();
        }
        return;
      }
    }

    const selections = [];
    for (const registration of this.registrations.values()) {
      try {
        const translated = translateRequirement(registration.requirement, this.description);
        registration.error = null;
        registration.descriptor = translated.descriptor;
        selections.push(translated.selection);
      } catch (error) {
        registration.error = error;
        registration.descriptor = null;
      }
    }
    const activeSelection = mergeSelections(...selections);
    if (isSelectionEmpty(activeSelection)) {
      this.#notifyAll();
      return;
    }

    const cachedSelection = this.snapshot?.selection ?? {};
    const requestedSelection = this.forceFull
      ? activeSelection
      : differenceSelection(activeSelection, cachedSelection);
    if (isSelectionEmpty(requestedSelection)) {
      this.forceFull = false;
      if (this.snapshot) {
        this.#setState({
          status: this.snapshot.errors.length > 0 ? ObjectContextStatus.PARTIAL_ERROR : ObjectContextStatus.READY,
          generation: this.snapshot.generation,
          snapshot: this.snapshot,
          errors: this.snapshot.errors,
          error: null
        });
      }
      this.#notifyAll();
      return;
    }

    this.forceFull = false;
    const generation = ++this.generation;
    this.abortController?.abort();
    this.abortController = new AbortController();
    this.#setState({status: ObjectContextStatus.OBJECT_LOADING, generation, error: null});
    this.#notifyAll();

    try {
      const result = await this.client.readObject({
        description: this.description,
        identity: this.identity,
        selection: requestedSelection,
        signal: this.abortController.signal
      });
      if (this.closed || generation !== this.generation) {
        return;
      }
      if (result.data === null) {
        const error = new Error(result.errors[0]?.message ?? `Object '${this.identity.logicalTypeName}:${this.identity.id}' was not found.`);
        this.#setState({
          status: ObjectContextStatus.TERMINAL_ERROR,
          generation,
          error,
          errors: result.errors.length ? result.errors : [asGraphQLError(error)]
        });
        this.#notifyAll();
        return;
      }
      const previousData = this.snapshot?.data ?? {};
      const previousSelection = this.snapshot?.selection ?? {};
      const errors = mergeErrors(this.snapshot?.errors ?? [], result.errors, requestedSelection);
      this.snapshot = deepFreeze({
        data: deepMerge(previousData, result.data),
        selection: mergeSelections(previousSelection, requestedSelection),
        errors,
        generation
      });
      this.#setState({
        status: errors.length > 0 ? ObjectContextStatus.PARTIAL_ERROR : ObjectContextStatus.READY,
        generation,
        snapshot: this.snapshot,
        errors,
        error: null
      });
      this.#notifyAll();
    } catch (error) {
      if (error?.name === 'AbortError' || generation !== this.generation || this.closed) {
        return;
      }
      const errors = [asGraphQLError(error)];
      if (this.snapshot) {
        this.snapshot = deepFreeze({...this.snapshot, errors, generation});
        this.#setState({
          status: ObjectContextStatus.PARTIAL_ERROR,
          generation,
          snapshot: this.snapshot,
          errors,
          error
        });
      } else {
        this.#setState({
          status: ObjectContextStatus.TERMINAL_ERROR,
          generation,
          errors,
          error
        });
      }
      this.#notifyAll();
    }
  }

  #setState(changes) {
    this.state = freezeState({
      status: changes.status ?? this.state.status,
      generation: changes.generation ?? this.generation,
      snapshot: changes.snapshot === undefined ? this.snapshot : changes.snapshot,
      errors: changes.errors ?? this.state.errors,
      error: changes.error === undefined ? this.state.error : changes.error
    });
    for (const listener of this.stateListeners) {
      listener(this.state);
    }
    this.dispatchEvent(createSemanticEvent(OBJECT_CONTEXT_STATE_EVENT, {state: this.state}, {bubbles: false, composed: false}));
  }

  #notifyAll() {
    for (const registration of this.registrations.values()) {
      this.#notifyRegistration(registration);
    }
  }

  #notifyRegistration(registration) {
    const requirement = registration.requirement;
    if (registration.error) {
      registration.listener(freezeRequirementState({
        status: RequirementStatus.UNSUPPORTED,
        requirement,
        descriptor: null,
        data: null,
        errors: [asGraphQLError(registration.error)],
        generation: this.generation
      }));
      return;
    }
    const mappedStatus = mapRequirementStatus(this.state.status);
    const pathHead = requirement.kind === 'header' || requirement.kind === 'layout'
      ? this.description?.metadata?.id
      : requirement.member;
    const errors = this.snapshot?.errors.filter(error => error.path?.[0] === pathHead) ?? [];
    const data = this.snapshot && pathHead ? this.snapshot.data[pathHead] : null;
    registration.listener(freezeRequirementState({
      status: errors.length > 0 && mappedStatus === RequirementStatus.READY
        ? RequirementStatus.PARTIAL_ERROR
        : mappedStatus,
      requirement,
      descriptor: registration.descriptor,
      data,
      errors,
      generation: this.generation
    }));
  }
}

async function ensureResultTypes(client, description, typeRef, signal) {
  const resultTypeName = namedType(typeRef);
  const resultKind = innermostType(typeRef)?.kind;
  if (!resultTypeName || resultKind !== 'OBJECT' || description.types.has(resultTypeName)) {
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

function mutationFieldNameFor(description, member) {
  return `${description.generatedFieldName}__${member}`;
}

function targetArgumentName(field, generatedInputTypeName) {
  return field.args.find(argument => namedType(argument.type) === generatedInputTypeName)?.name
    ?? field.args.find(argument => argument.name === '_target')?.name
    ?? '_target';
}

function propertyInputType(descriptor, mutationField, member) {
  return descriptor.fields.get('set')?.args.find(argument => argument.name === member)?.type
    ?? descriptor.fields.get('validate')?.args.find(argument => argument.name === member)?.type
    ?? mutationField?.args.find(argument => argument.name === member)?.type
    ?? descriptor.value?.typeRef
    ?? null;
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

function actionInvocationField(descriptor) {
  return ['invoke', 'invokeIdempotent', 'invokeNonIdempotent']
    .map(name => descriptor.fields.get(name))
    .find(Boolean) ?? null;
}

function argumentsFromValues(field, values) {
  const result = {};
  for (const argument of field?.args ?? []) {
    if (Object.prototype.hasOwnProperty.call(values, argument.name)) {
      result[argument.name] = values[argument.name];
    }
  }
  return result;
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

function isSafeStructuralResourcePath(value) {
  return typeof value === 'string'
    && value.startsWith('/')
    && !value.startsWith('//')
    && !value.includes('\\')
    && !/[\r\n]/.test(value);
}

function validateIdentity(identity, client) {
  if (!client) {
    return new Error('A Causeway GraphQL client is required.');
  }
  if (!identity.logicalTypeName) {
    return new Error('The logical-type attribute is required.');
  }
  if (!identity.id) {
    return new Error('The object-id attribute is required.');
  }
  return null;
}

function normalizeRequirement(requirement) {
  if (requirement?.kind === 'header' || requirement?.kind === 'layout') {
    return Object.freeze({kind: requirement.kind});
  }
  if (['property', 'action', 'collection'].includes(requirement?.kind)
      && typeof requirement.member === 'string'
      && requirement.member.length > 0) {
    return Object.freeze({kind: requirement.kind, member: requirement.member});
  }
  throw new Error(`Unsupported semantic read requirement '${JSON.stringify(requirement)}'.`);
}

function translateRequirement(requirement, description) {
  if (requirement.kind === 'header' || requirement.kind === 'layout') {
    const metadata = description.metadata;
    if (!metadata) {
      throw new Error(`Type '${description.generatedTypeName}' does not expose rich object metadata.`);
    }
    const requestedFields = requirement.kind === 'header'
      ? ['id', 'logicalTypeName', 'title', 'version']
      : ['grid', 'layout', 'cssClass'];
    const supportedFields = requestedFields.filter(field => metadata.fields.has(field));
    if (requirement.kind === 'header'
        && (!supportedFields.includes('id') || !supportedFields.includes('logicalTypeName'))) {
      throw new Error(`Metadata type '${metadata.generatedTypeName}' lacks object identity fields.`);
    }
    if (supportedFields.length === 0) {
      throw new Error(`Metadata type '${metadata.generatedTypeName}' lacks ${requirement.kind} fields.`);
    }
    return {
      descriptor: metadata,
      selection: {[metadata.id]: Object.fromEntries(supportedFields.map(field => [field, true]))}
    };
  }
  const member = description.members.get(requirement.member);
  if (!member || member.kind !== requirement.kind) {
    const label = requirement.kind[0].toUpperCase() + requirement.kind.slice(1);
    throw new Error(`${label} '${requirement.member}' is not present on '${description.logicalTypeName}'.`);
  }
  if (requirement.kind === 'property') {
    if (!member.fields.has('get')) {
      throw new Error(`Property '${requirement.member}' does not expose a readable value.`);
    }
    const memberSelection = Object.fromEntries(
      ['hidden', 'disabled', 'datatype']
        .filter(field => member.fields.has(field))
        .map(field => [field, true])
    );
    memberSelection.get = propertyValueSelection(member);
    return {descriptor: member, selection: {[member.id]: memberSelection}};
  }
  if (requirement.kind === 'collection'
      && !member.fields.has('window')
      && !member.fields.has('get')) {
    throw new Error(`Collection '${requirement.member}' does not expose readable contents.`);
  }
  const supportedFields = ['hidden', 'disabled'].filter(field => member.fields.has(field));
  return {
    descriptor: member,
    selection: {[member.id]: Object.fromEntries(supportedFields.map(field => [field, true]))}
  };
}

function propertyValueSelection(member) {
  const value = member.value;
  if (!value || value.typeKind === 'SCALAR' || value.typeKind === 'ENUM') {
    return true;
  }
  const typeFields = value.typeDescription?.fields ?? [];
  const lobFields = typeFields
    .filter(field => ['name', 'mimeType', 'bytes', 'chars'].includes(field.name))
    .map(field => field.name);
  if (lobFields.length > 0) {
    return Object.fromEntries(lobFields.map(field => [field, true]));
  }
  const scalarFields = typeFields
    .filter(field => ['SCALAR', 'ENUM'].includes(innermostType(field.type)?.kind))
    .map(field => field.name);
  if (scalarFields.length > 0) {
    return Object.fromEntries(scalarFields.map(field => [field, true]));
  }
  if (value.typeDescription) {
    return {__typename: true};
  }
  return {_meta: {id: true, logicalTypeName: true, title: true, version: true}};
}

function integerAtLeast(value, minimum, label) {
  if (!Number.isSafeInteger(value) || value < minimum) {
    throw new Error(`${label} must be an integer of at least ${minimum}.`);
  }
  return value;
}

function normalizeCollectionWindow(window) {
  if (!window) {
    return null;
  }
  const offset = window.offset;
  const requestedSize = window.requestedSize;
  const returnedCount = window.returnedCount;
  const hasPrevious = window.hasPrevious === true;
  const hasNext = window.hasNext === true;
  return Object.freeze({
    offset,
    requestedSize,
    returnedCount,
    totalCount: Number.isSafeInteger(window.totalCount) ? window.totalCount : null,
    countAvailable: Number.isSafeInteger(window.totalCount),
    maximumSize: window.maximumSize,
    hasPrevious,
    hasNext,
    previousOffset: hasPrevious ? Math.max(0, offset - requestedSize) : null,
    nextOffset: hasNext ? offset + returnedCount : null,
    rangeStart: returnedCount > 0 ? offset + 1 : null,
    rangeEnd: returnedCount > 0 ? offset + returnedCount : null,
    ordering: window.ordering ?? null
  });
}

function obsoleteRequestError() {
  const error = new Error('Collection window response was superseded by a newer request.');
  error.name = 'AbortError';
  return error;
}

function collectionRowSelection(columns) {
  const selection = {_meta: {id: true, logicalTypeName: true, title: true, version: true}};
  for (const column of columns) {
    const member = typeof column === 'string' ? column : column?.member;
    if (!member) {
      continue;
    }
    selection[member] = {hidden: true, disabled: true, datatype: true, get: true};
  }
  return selection;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
}

function mapRequirementStatus(objectStatus) {
  return {
    [ObjectContextStatus.IDLE]: RequirementStatus.IDLE,
    [ObjectContextStatus.SCHEMA_LOADING]: RequirementStatus.SCHEMA_LOADING,
    [ObjectContextStatus.OBJECT_LOADING]: RequirementStatus.OBJECT_LOADING,
    [ObjectContextStatus.READY]: RequirementStatus.READY,
    [ObjectContextStatus.PARTIAL_ERROR]: RequirementStatus.READY,
    [ObjectContextStatus.TERMINAL_ERROR]: RequirementStatus.TERMINAL_ERROR
  }[objectStatus] ?? RequirementStatus.TERMINAL_ERROR;
}

function mergeErrors(previousErrors, nextErrors, requestedSelection) {
  const replacedHeads = new Set(Object.keys(requestedSelection));
  return Object.freeze([
    ...previousErrors.filter(error => !replacedHeads.has(error.path?.[0])),
    ...nextErrors
  ]);
}

function asGraphQLError(error) {
  return Object.freeze({
    message: error?.message ?? String(error),
    path: [],
    extensions: Object.freeze(error?.code ? {code: error.code} : {})
  });
}

function freezeState(state) {
  return Object.freeze({
    status: state.status,
    generation: state.generation,
    snapshot: state.snapshot,
    errors: Object.freeze([...(state.errors ?? [])]),
    error: state.error ?? null
  });
}

function freezeRequirementState(state) {
  return Object.freeze({...state, errors: Object.freeze([...(state.errors ?? [])])});
}

function deepFreeze(value) {
  if (value && typeof value === 'object' && !Object.isFrozen(value)) {
    Object.freeze(value);
    for (const nested of Object.values(value)) {
      deepFreeze(nested);
    }
  }
  return value;
}
