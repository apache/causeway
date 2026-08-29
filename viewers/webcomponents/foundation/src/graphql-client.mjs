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

import {buildApplicationEntryReadOperation} from './application-operations.mjs';
import {normalizeExecutor} from './graphql-executor.mjs';
import {buildDescribeTypesOperation, fieldsByName, namedType, normalizeTypeDescription} from './introspection.mjs';
import {RichSchemaNameError, RichSchemaNames} from './schema-names.mjs';
import {buildCollectionWindowReadOperation, buildObjectReadOperation} from './selection.mjs';
import {
  buildDescribeOperationRootsOperation,
  buildMutationInteractionOperation,
  buildObjectInteractionOperation,
  buildServiceInteractionOperation
} from './interaction-operations.mjs';

export class CausewaySchemaError extends Error {
  constructor(code, message, details = {}) {
    super(message);
    this.name = 'CausewaySchemaError';
    this.code = code;
    this.details = Object.freeze({...details});
  }
}

export class CausewayGraphQLClient {
  constructor({executor, schemaNames = new RichSchemaNames()} = {}) {
    this.executor = normalizeExecutor(executor);
    this.schemaNames = schemaNames;
    this.typeCache = new Map();
    this.objectDescriptionCache = new Map();
    this.serviceDescriptionCache = new Map();
    this.operationRootsPromise = null;
    this.mutationDescriptionPromise = null;
    this.applicationDescriptionPromise = null;
    this.schemaDiscoveryTail = Promise.resolve();
  }

  describeObject(logicalTypeName) {
    if (!this.objectDescriptionCache.has(logicalTypeName)) {
      const promise = this.#describeObject(logicalTypeName)
        .catch(error => {
          this.objectDescriptionCache.delete(logicalTypeName);
          throw error;
        });
      this.objectDescriptionCache.set(logicalTypeName, promise);
    }
    return this.objectDescriptionCache.get(logicalTypeName);
  }

  async describeTypes(typeNames, {signal} = {}) {
    const uniqueNames = [...new Set(typeNames)];
    const uncached = uniqueNames.filter(typeName => !this.typeCache.has(typeName));
    if (uncached.length > 0) {
      const batchPromise = this.#fetchTypes(uncached, signal);
      for (const typeName of uncached) {
        this.typeCache.set(typeName, batchPromise.then(types => types.get(typeName) ?? null));
      }
      batchPromise.catch(() => {
        for (const typeName of uncached) {
          this.typeCache.delete(typeName);
        }
      });
    }
    const descriptions = await Promise.all(uniqueNames.map(typeName => this.typeCache.get(typeName)));
    return new Map(uniqueNames.map((typeName, index) => [typeName, descriptions[index]]));
  }

  async describeOperationRoots({signal} = {}) {
    if (!this.operationRootsPromise) {
      const operation = buildDescribeOperationRootsOperation();
      this.operationRootsPromise = this.#executeSchemaDiscovery({
        document: operation.document,
        variables: operation.variables,
        operationName: operation.operationName,
        signal
      }).then(response => {
        if (response.errors?.length && !response.data) {
          throw new CausewaySchemaError(
            'OPERATION_ROOT_INTROSPECTION_FAILED',
            response.errors.map(error => error.message).join('; '),
            {errors: response.errors}
          );
        }
        return Object.freeze({
          queryTypeName: response.data?.__schema?.queryType?.name ?? null,
          mutationTypeName: response.data?.__schema?.mutationType?.name ?? null
        });
      }).catch(error => {
        this.operationRootsPromise = null;
        throw error;
      });
    }
    return this.operationRootsPromise;
  }

  async describeMutation({signal} = {}) {
    if (!this.mutationDescriptionPromise) {
      this.mutationDescriptionPromise = this.describeOperationRoots({signal}).then(async roots => {
        if (!roots.mutationTypeName) {
          return null;
        }
        const types = await this.describeTypes([roots.mutationTypeName], {signal});
        return types.get(roots.mutationTypeName) ?? null;
      }).catch(error => {
        this.mutationDescriptionPromise = null;
        throw error;
      });
    }
    return this.mutationDescriptionPromise;
  }

  describeService(logicalTypeName) {
    if (!this.serviceDescriptionCache.has(logicalTypeName)) {
      const promise = this.#describeObject(logicalTypeName)
        .then(description => Object.freeze({...description, kind: 'service'}))
        .catch(error => {
          this.serviceDescriptionCache.delete(logicalTypeName);
          throw error;
        });
      this.serviceDescriptionCache.set(logicalTypeName, promise);
    }
    return this.serviceDescriptionCache.get(logicalTypeName);
  }

  describeApplicationEntry() {
    if (!this.applicationDescriptionPromise) {
      this.applicationDescriptionPromise = this.#describeApplicationEntry().catch(error => {
        this.applicationDescriptionPromise = null;
        throw error;
      });
    }
    return this.applicationDescriptionPromise;
  }

  async readApplicationEntry({description, signal} = {}) {
    const operation = buildApplicationEntryReadOperation({description, schemaNames: this.schemaNames});
    const response = await this.executor({
      document: operation.document,
      variables: operation.variables,
      operationName: operation.operationName,
      signal
    });
    return Object.freeze({
      data: valueAtPath(response.data, operation.applicationPath) ?? null,
      errors: normalizeErrors(response.errors, operation.applicationPath),
      operation
    });
  }

  async readObject({description, identity, selection, signal}) {
    const operation = buildObjectReadOperation({
      description,
      identity,
      selection,
      schemaNames: this.schemaNames
    });
    const response = await this.executor({
      document: operation.document,
      variables: operation.variables,
      operationName: operation.operationName,
      signal
    });
    const objectData = valueAtPath(response.data, operation.objectPath);
    const errors = normalizeErrors(response.errors, operation.objectPath);
    return Object.freeze({
      data: objectData ?? null,
      errors,
      operation
    });
  }

  async readCollectionWindow({description, identity, member, rowSelection, offset, size, signal}) {
    const operation = buildCollectionWindowReadOperation({
      description,
      identity,
      member,
      rowSelection,
      offset,
      size,
      schemaNames: this.schemaNames
    });
    const response = await this.executor({
      document: operation.document,
      variables: operation.variables,
      operationName: operation.operationName,
      signal
    });
    const objectData = valueAtPath(response.data, operation.objectPath);
    return Object.freeze({
      data: objectData ?? null,
      errors: normalizeErrors(response.errors, operation.objectPath),
      operation
    });
  }

  async executeObjectInteraction({description, identity, selection, operationName, signal}) {
    const operation = buildObjectInteractionOperation({
      description,
      identity,
      selection,
      schemaNames: this.schemaNames,
      operationName
    });
    const response = await this.executor({
      document: operation.document,
      variables: operation.variables,
      operationName: operation.operationName,
      signal
    });
    return Object.freeze({
      data: valueAtPath(response.data, operation.objectPath) ?? null,
      errors: normalizeErrors(response.errors, operation.objectPath),
      operation
    });
  }

  async executeServiceInteraction({description, selection, operationName, signal}) {
    const operation = buildServiceInteractionOperation({
      description,
      selection,
      schemaNames: this.schemaNames,
      operationName
    });
    const response = await this.executor({
      document: operation.document,
      variables: operation.variables,
      operationName: operation.operationName,
      signal
    });
    return Object.freeze({
      data: valueAtPath(response.data, operation.servicePath) ?? null,
      errors: normalizeErrors(response.errors, operation.servicePath),
      operation
    });
  }

  async executeMutationInteraction({
    description,
    mutationType,
    fieldName,
    args,
    resultSelection,
    operationName,
    signal
  }) {
    const types = new Map(description.types);
    types.set(mutationType.name, mutationType);
    const operation = buildMutationInteractionOperation({
      mutationType,
      fieldName,
      args,
      resultSelection,
      types,
      operationName
    });
    const response = await this.executor({
      document: operation.document,
      variables: operation.variables,
      operationName: operation.operationName,
      signal
    });
    return Object.freeze({
      data: valueAtPath(response.data, operation.resultPath) ?? null,
      errors: normalizeErrors(response.errors, operation.resultPath),
      operation
    });
  }

  clearSchemaCache() {
    this.typeCache.clear();
    this.objectDescriptionCache.clear();
    this.serviceDescriptionCache.clear();
    this.operationRootsPromise = null;
    this.mutationDescriptionPromise = null;
    this.applicationDescriptionPromise = null;
  }

  async #describeApplicationEntry(signal) {
    const roots = await this.describeOperationRoots({signal});
    if (!roots.queryTypeName) {
      return unsupportedApplicationDescription('QUERY_ROOT_UNAVAILABLE');
    }
    const queryTypes = await this.describeTypes([roots.queryTypeName], {signal});
    const queryType = queryTypes.get(roots.queryTypeName) ?? null;
    if (!queryType) {
      return unsupportedApplicationDescription('QUERY_ROOT_UNAVAILABLE');
    }
    let richType = queryType;
    if (this.schemaNames.richRootField) {
      const richField = fieldsByName(queryType).get(this.schemaNames.richRootField) ?? null;
      const richTypeName = richField ? namedType(richField.type) : null;
      if (!richTypeName) {
        return unsupportedApplicationDescription('RICH_ROOT_UNAVAILABLE');
      }
      const richTypes = await this.describeTypes([richTypeName], {signal});
      richType = richTypes.get(richTypeName) ?? null;
      if (!richType) {
        return unsupportedApplicationDescription('RICH_ROOT_UNAVAILABLE');
      }
    }
    const applicationField = fieldsByName(richType).get('application') ?? null;
    const applicationTypeName = applicationField ? namedType(applicationField.type) : null;
    if (!applicationTypeName) {
      return unsupportedApplicationDescription('APPLICATION_UNAVAILABLE');
    }
    const applicationTypes = await this.describeTypes([applicationTypeName], {signal});
    const applicationType = applicationTypes.get(applicationTypeName) ?? null;
    if (!applicationType) {
      return unsupportedApplicationDescription('APPLICATION_UNAVAILABLE');
    }
    const applicationFields = fieldsByName(applicationType);
    const menuBarsField = applicationFields.get('menuBars') ?? null;
    const menuBarsTypeName = menuBarsField ? namedType(menuBarsField.type) : null;
    if (!menuBarsTypeName) {
      return unsupportedApplicationDescription('MENU_BARS_UNAVAILABLE', {applicationField, applicationType});
    }
    const issuesField = applicationFields.get('issues') ?? null;
    const issueTypeName = issuesField ? namedType(issuesField.type) : null;
    const homeField = applicationFields.get('home') ?? null;
    const homeTypeName = homeField ? namedType(homeField.type) : null;
    const requestedTypes = [menuBarsTypeName, issueTypeName, homeTypeName].filter(Boolean);
    const supportTypes = await this.describeTypes(requestedTypes, {signal});
    const menuBarsType = supportTypes.get(menuBarsTypeName) ?? null;
    if (!menuBarsType) {
      return unsupportedApplicationDescription('MENU_BARS_UNAVAILABLE', {applicationField, applicationType});
    }
    const homeType = homeTypeName ? supportTypes.get(homeTypeName) ?? null : null;
    const homeObjectField = homeType ? fieldsByName(homeType).get('object') ?? null : null;
    const homeObjectTypeName = homeObjectField ? namedType(homeObjectField.type) : null;
    const homeObjectUnion = homeObjectTypeName
      ? (await this.describeTypes([homeObjectTypeName], {signal})).get(homeObjectTypeName) ?? null
      : null;
    return Object.freeze({
      supported: true,
      reason: null,
      applicationField,
      applicationType,
      menuBarsField,
      menuBarsType,
      issuesField,
      issueType: issueTypeName ? supportTypes.get(issueTypeName) ?? null : null,
      homeField,
      homeType,
      homeObjectField,
      homeObjectUnion
    });
  }

  async #fetchTypes(typeNames, signal) {
    const entries = await Promise.all(typeNames.map(async typeName => {
      const operation = buildDescribeTypesOperation([typeName]);
      const response = await this.#executeSchemaDiscovery({
        document: operation.document,
        variables: operation.variables,
        operationName: operation.operationName,
        signal
      });
      if (response.errors?.length && !response.data) {
        throw new CausewaySchemaError(
          'INTROSPECTION_FAILED',
          response.errors.map(error => error.message).join('; '),
          {errors: response.errors}
        );
      }
      const alias = operation.aliases.get(typeName);
      return [typeName, normalizeTypeDescription(response.data?.[alias])];
    }));
    return new Map(entries);
  }

  #executeSchemaDiscovery(request) {
    const execute = () => this.executor(request);
    const result = this.schemaDiscoveryTail.then(execute, execute);
    this.schemaDiscoveryTail = result.catch(() => undefined);
    return result;
  }

  async #describeObject(logicalTypeName) {
    const generatedTypeName = this.schemaNames.objectType(logicalTypeName);
    const initialTypes = await this.describeTypes([generatedTypeName]);
    const objectType = initialTypes.get(generatedTypeName);
    if (!objectType) {
      throw new CausewaySchemaError(
        'OBJECT_TYPE_NOT_FOUND',
        `Rich GraphQL object type '${generatedTypeName}' was not found.`,
        {logicalTypeName, generatedTypeName}
      );
    }

    const describedTypes = new Map([[generatedTypeName, objectType]]);
    let pending = directSupportTypeNames(objectType, generatedTypeName, this.schemaNames);
    while (pending.length > 0) {
      const batch = await this.describeTypes(pending);
      const next = new Set();
      for (const [typeName, typeDescription] of batch) {
        if (!typeDescription) {
          throw new CausewaySchemaError(
            'SUPPORT_TYPE_NOT_FOUND',
            `Rich GraphQL support type '${typeName}' was not found.`,
            {logicalTypeName, generatedTypeName, typeName}
          );
        }
        describedTypes.set(typeName, typeDescription);
        for (const nestedTypeName of referencedInteractionTypeNames(typeDescription, generatedTypeName, this.schemaNames)) {
          if (!describedTypes.has(nestedTypeName)) {
            next.add(nestedTypeName);
          }
        }
      }
      pending = [...next];
    }

    const members = new Map();
    let metadata = null;
    for (const field of objectType.fields) {
      const generatedMemberTypeName = namedType(field.type);
      if (!generatedMemberTypeName || !this.schemaNames.isReachableSupportType(generatedTypeName, generatedMemberTypeName)) {
        continue;
      }
      let classification;
      try {
        classification = this.schemaNames.classify(generatedMemberTypeName, generatedTypeName);
      } catch (cause) {
        if (cause instanceof RichSchemaNameError) {
          throw new CausewaySchemaError(
            'UNRECOGNIZED_MEMBER_TYPE',
            `Cannot classify field '${field.name}' with generated type '${generatedMemberTypeName}'.`,
            {field: field.name, generatedMemberTypeName, cause}
          );
        }
        throw cause;
      }
      const semanticKind = semanticMemberKind(classification.kind);
      if (!semanticKind) {
        throw new CausewaySchemaError(
          'UNRECOGNIZED_MEMBER_TYPE',
          `Field '${field.name}' uses unsupported generated type '${generatedMemberTypeName}'.`,
          {field: field.name, generatedMemberTypeName, classification}
        );
      }
      const supportType = describedTypes.get(generatedMemberTypeName);
      const member = Object.freeze({
        id: field.name,
        kind: semanticKind,
        description: field.description ?? null,
        generatedTypeName: generatedMemberTypeName,
        fields: fieldsByName(supportType),
        metadata: semanticMemberMetadataDescription(supportType, describedTypes),
        value: semanticValueDescription(semanticKind, supportType, describedTypes),
        window: semanticCollectionWindowDescription(semanticKind, supportType, describedTypes)
      });
      if (semanticKind === 'metadata') {
        metadata = member;
      } else {
        members.set(field.name, member);
      }
    }

    return Object.freeze({
      logicalTypeName,
      generatedTypeName,
      generatedInputTypeName: this.schemaNames.objectInputType(logicalTypeName),
      generatedFieldName: this.schemaNames.objectField(logicalTypeName),
      description: objectType.description ?? null,
      members,
      metadata,
      types: describedTypes
    });
  }
}

function semanticMemberMetadataDescription(supportType, describedTypes) {
  const field = supportType?.fields.find(candidate => candidate.name === 'metadata') ?? null;
  const generatedTypeName = namedType(field?.type);
  const typeDescription = describedTypes.get(generatedTypeName) ?? null;
  return field && typeDescription
    ? Object.freeze({field, generatedTypeName, typeDescription, fields: fieldsByName(typeDescription)})
    : null;
}

function semanticCollectionWindowDescription(kind, supportType, describedTypes) {
  if (kind !== 'collection') {
    return null;
  }
  const field = supportType?.fields.find(candidate => candidate.name === 'window') ?? null;
  if (!field) {
    return null;
  }
  const generatedTypeName = namedType(field.type);
  const typeDescription = describedTypes.get(generatedTypeName) ?? null;
  if (!typeDescription) {
    return null;
  }
  const args = new Map(field.args.map(argument => [argument.name, argument]));
  return Object.freeze({
    generatedTypeName,
    typeDescription,
    fields: fieldsByName(typeDescription),
    offsetDefault: integerDefault(args.get('offset')?.defaultValue, 0),
    sizeDefault: integerDefault(args.get('size')?.defaultValue, null)
  });
}

function integerDefault(value, fallback) {
  if (value == null) {
    return fallback;
  }
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) ? parsed : fallback;
}

function semanticValueDescription(kind, supportType, describedTypes) {
  if (kind !== 'property' && kind !== 'collection') {
    return null;
  }
  const typeRef = supportType?.fields.find(field => field.name === 'get')?.type ?? null;
  const elementTypeRef = kind === 'collection' ? listElementType(typeRef) : null;
  const effectiveTypeRef = elementTypeRef ?? typeRef;
  const namedTypeName = namedType(effectiveTypeRef);
  return Object.freeze({
    typeRef,
    namedTypeName,
    typeKind: innermostType(effectiveTypeRef)?.kind ?? null,
    typeDescription: describedTypes.get(namedTypeName) ?? null,
    elementTypeRef
  });
}

function listElementType(typeRef) {
  let current = typeRef;
  while (current?.kind === 'NON_NULL') {
    current = current.ofType;
  }
  if (current?.kind !== 'LIST') {
    return null;
  }
  return current.ofType ?? null;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
}

function directSupportTypeNames(objectType, generatedTypeName, schemaNames) {
  const result = new Set();
  for (const field of objectType.fields) {
    const typeName = namedType(field.type);
    if (schemaNames.isReachableSupportType(generatedTypeName, typeName)) {
      result.add(typeName);
    }
  }
  return [...result];
}

function referencedInteractionTypeNames(typeDescription, generatedTypeName, schemaNames) {
  const result = new Set();
  const consider = (typeRef, fieldName = '') => {
    const typeName = namedType(typeRef);
    const kind = innermostType(typeRef)?.kind;
    const choiceValue = ['choices', 'default', 'autoComplete', 'autoCompleteWindow'].includes(fieldName)
      && ['OBJECT', 'INTERFACE', 'UNION'].includes(kind);
    const objectValue = ['get', 'rows', 'items'].includes(fieldName)
      && ['OBJECT', 'INTERFACE', 'UNION'].includes(kind);
    const objectMetadata = fieldName === '_meta' && kind === 'OBJECT';
    const memberMetadata = fieldName === 'metadata' && kind === 'OBJECT';
    if (typeName && (schemaNames.isReachableSupportType(generatedTypeName, typeName)
        || kind === 'ENUM' && typeName.startsWith('rich__')
        || choiceValue
        || objectValue
        || objectMetadata
        || memberMetadata)) {
      result.add(typeName);
    }
  };
  for (const field of typeDescription.fields) {
    consider(field.type, field.name);
    for (const argument of field.args) {
      consider(argument.type);
    }
  }
  return [...result];
}

function semanticMemberKind(generatedKind) {
  return {
    property: 'property',
    collection: 'collection',
    action: 'action',
    metadata: 'metadata'
  }[generatedKind] ?? null;
}

function unsupportedApplicationDescription(reason, details = {}) {
  return Object.freeze({
    supported: false,
    reason,
    applicationField: details.applicationField ?? null,
    applicationType: details.applicationType ?? null,
    menuBarsField: null,
    menuBarsType: null,
    issuesField: null,
    issueType: null
  });
}

function valueAtPath(root, path) {
  return path.reduce((value, segment) => value?.[segment], root);
}

function normalizeErrors(errors = [], objectPath) {
  return Object.freeze(errors.map(error => {
    const originalPath = Array.isArray(error.path) ? error.path : [];
    const startsAtObject = objectPath.every((segment, index) => originalPath[index] === segment);
    return Object.freeze({
      message: error.message ?? 'GraphQL error',
      path: startsAtObject ? originalPath.slice(objectPath.length) : originalPath,
      extensions: Object.freeze({...error.extensions})
    });
  }));
}
