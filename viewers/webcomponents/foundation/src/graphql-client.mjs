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

import {normalizeExecutor} from './graphql-executor.mjs';
import {buildDescribeTypesOperation, fieldsByName, namedType, normalizeTypeDescription} from './introspection.mjs';
import {RichSchemaNameError, RichSchemaNames} from './schema-names.mjs';
import {buildObjectReadOperation} from './selection.mjs';

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

  clearSchemaCache() {
    this.typeCache.clear();
    this.objectDescriptionCache.clear();
  }

  async #fetchTypes(typeNames, signal) {
    const entries = await Promise.all(typeNames.map(async typeName => {
      const operation = buildDescribeTypesOperation([typeName]);
      const response = await this.executor({
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
        for (const field of typeDescription.fields) {
          const nestedTypeName = namedType(field.type);
          if (this.schemaNames.isReachableSupportType(generatedTypeName, nestedTypeName)
              && !describedTypes.has(nestedTypeName)) {
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
        value: semanticValueDescription(semanticKind, supportType, describedTypes)
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

function semanticMemberKind(generatedKind) {
  return {
    property: 'property',
    collection: 'collection',
    action: 'action',
    metadata: 'metadata'
  }[generatedKind] ?? null;
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
