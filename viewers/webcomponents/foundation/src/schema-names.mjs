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

const RICH_PREFIX = 'rich__';

const GENERATED_SUFFIXES = Object.freeze([
  ['__gqlv_action_parameter', 'parameter'],
  ['__gqlv_action_invoke', 'action-invoke'],
  ['__gqlv_action_params', 'action-params'],
  ['__gqlv_action_args', 'action-args'],
  ['__gqlv_property_lob', 'property-lob'],
  ['__gqlv_property', 'property'],
  ['__gqlv_collection', 'collection'],
  ['__gqlv_action', 'action'],
  ['__gqlv_member', 'member'],
  ['__gqlv_meta', 'metadata'],
  ['__gqlv_input', 'input'],
  ['__gqlv_enum', 'enum']
]);

export class RichSchemaNameError extends Error {
  constructor(code, message, details = {}) {
    super(message);
    this.name = 'RichSchemaNameError';
    this.code = code;
    this.details = Object.freeze({...details});
  }
}

export function sanitizeGraphQLName(name) {
  if (typeof name !== 'string' || name.length === 0) {
    throw new RichSchemaNameError('INVALID_LOGICAL_NAME', 'A non-empty logical name is required.', {name});
  }
  const replaced = name.replaceAll('.', '_').replaceAll('#', '__').replaceAll('()', '');
  const hyphenStart = replaced.indexOf('-');
  if (hyphenStart <= 0) {
    return replaced;
  }
  const [first, ...rest] = replaced.split('-');
  return first + rest.map(capitalize).join('');
}

function capitalize(word) {
  return word.length === 0 ? '' : word.charAt(0).toUpperCase() + word.slice(1);
}

export class RichSchemaNames {
  constructor({
    richRootField = 'rich',
    lookupArgumentName = 'object',
    objectFieldPrefix = '',
    objectFieldSuffix = ''
  } = {}) {
    this.richRootField = richRootField === '' ? null : richRootField;
    this.lookupArgumentName = assertGraphQLName(lookupArgumentName, 'lookup argument');
    this.objectFieldPrefix = objectFieldPrefix;
    this.objectFieldSuffix = objectFieldSuffix;
    Object.freeze(this);
  }

  objectType(logicalTypeName) {
    return `${RICH_PREFIX}${sanitizeGraphQLName(logicalTypeName)}`;
  }

  objectInputType(logicalTypeName) {
    return `${this.objectType(logicalTypeName)}__gqlv_input`;
  }

  objectField(logicalTypeName) {
    return assertGraphQLName(
      `${this.objectFieldPrefix}${sanitizeGraphQLName(logicalTypeName)}${this.objectFieldSuffix}`,
      'object lookup field'
    );
  }

  metadataType(logicalTypeName) {
    return `${this.objectType(logicalTypeName)}__gqlv_meta`;
  }

  memberType(logicalTypeName, memberId, kind) {
    const suffix = {
      property: 'property',
      collection: 'collection',
      action: 'action'
    }[kind];
    if (!suffix) {
      throw new RichSchemaNameError('UNSUPPORTED_MEMBER_KIND', `Unsupported member kind '${kind}'.`, {kind});
    }
    return `${this.objectType(logicalTypeName)}__${assertGraphQLName(memberId, 'member')}__gqlv_${suffix}`;
  }

  classify(typeName, ownerTypeName = null) {
    if (typeof typeName !== 'string' || !typeName.startsWith(RICH_PREFIX)) {
      throw new RichSchemaNameError(
        'UNRECOGNIZED_GENERATED_NAME',
        `Type '${typeName}' is not a rich-schema generated name.`,
        {typeName}
      );
    }
    const suffixEntry = GENERATED_SUFFIXES.find(([suffix]) => typeName.endsWith(suffix));
    if (!suffixEntry) {
      return Object.freeze({kind: 'object', typeName, ownerTypeName: null, localName: typeName.slice(RICH_PREFIX.length)});
    }
    const [suffix, kind] = suffixEntry;
    if (ownerTypeName && typeName !== `${ownerTypeName}${suffix}` && !typeName.startsWith(`${ownerTypeName}__`)) {
      throw new RichSchemaNameError(
        'WRONG_GENERATED_OWNER',
        `Type '${typeName}' does not belong to '${ownerTypeName}'.`,
        {typeName, ownerTypeName}
      );
    }
    const ownerPrefix = ownerTypeName ? `${ownerTypeName}__` : RICH_PREFIX;
    const localName = typeName.slice(ownerPrefix.length, typeName.length - suffix.length);
    return Object.freeze({kind, typeName, ownerTypeName, localName});
  }

  isReachableSupportType(ownerTypeName, typeName) {
    return typeof typeName === 'string'
      && typeName !== ownerTypeName
      && typeName.startsWith(`${ownerTypeName}__`)
      && typeName.includes('__gqlv_');
  }
}

export function assertGraphQLName(name, role = 'name') {
  if (typeof name !== 'string' || !/^[_A-Za-z][_0-9A-Za-z]*$/.test(name)) {
    throw new RichSchemaNameError('INVALID_GRAPHQL_NAME', `Invalid GraphQL ${role} '${name}'.`, {name, role});
  }
  return name;
}
