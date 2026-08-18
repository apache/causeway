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

/**
 * @typedef {Object} GraphQLRequest
 * @property {string} document GraphQL operation document.
 * @property {Record<string, unknown>} [variables] Operation variables.
 * @property {string} [operationName] Operation name.
 * @property {AbortSignal} [signal] Cancellation signal.
 */

/**
 * @typedef {Object} GraphQLErrorData
 * @property {string} message Error message.
 * @property {Array<string|number>} [path] GraphQL response path.
 * @property {Record<string, unknown>} [extensions] GraphQL extensions.
 */

/**
 * @typedef {Object} GraphQLResponse
 * @property {Record<string, unknown>|null} [data] Response data.
 * @property {GraphQLErrorData[]} [errors] Response errors.
 */

/**
 * A replaceable GraphQL transport.
 *
 * @callback GraphQLExecutor
 * @param {GraphQLRequest} request
 * @returns {Promise<GraphQLResponse>}
 */

/**
 * @typedef {'property'|'collection'|'action'|'metadata'} MemberKind
 */

/**
 * @typedef {Object} GraphQLTypeRef
 * @property {string} kind
 * @property {string|null} name
 * @property {GraphQLTypeRef|null} [ofType]
 */

/**
 * @typedef {Object} GraphQLArgumentDescription
 * @property {string} name
 * @property {string|null} description
 * @property {GraphQLTypeRef} type
 * @property {string|null} defaultValue
 */

/**
 * @typedef {Object} GraphQLFieldDescription
 * @property {string} name
 * @property {string|null} description
 * @property {GraphQLArgumentDescription[]} args
 * @property {GraphQLTypeRef} type
 */

/**
 * @typedef {Object} GraphQLTypeDescription
 * @property {string} name
 * @property {string|null} description
 * @property {GraphQLFieldDescription[]} fields
 * @property {GraphQLInputFieldDescription[]} inputFields
 * @property {GraphQLEnumValueDescription[]} enumValues
 * @property {string} kind
 */

/**
 * @typedef {Object} GraphQLInputFieldDescription
 * @property {string} name
 * @property {string|null} description
 * @property {GraphQLTypeRef} type
 * @property {string|null} defaultValue
 */

/**
 * @typedef {Object} GraphQLEnumValueDescription
 * @property {string} name
 * @property {string|null} description
 */

/**
 * @typedef {Object} SemanticMemberDescription
 * @property {string} id
 * @property {MemberKind} kind
 * @property {string|null} description
 * @property {string} generatedTypeName
 * @property {ReadonlyMap<string, GraphQLFieldDescription>} fields
 * @property {SemanticValueDescription|null} value
 */

/**
 * @typedef {Object} SemanticValueDescription
 * @property {GraphQLTypeRef|null} typeRef
 * @property {string|null} namedTypeName
 * @property {string|null} typeKind
 * @property {GraphQLTypeDescription|null} typeDescription
 * @property {GraphQLTypeRef|null} elementTypeRef
 */

/**
 * @typedef {Object} SemanticObjectDescription
 * @property {string} logicalTypeName
 * @property {string} generatedTypeName
 * @property {string} generatedInputTypeName
 * @property {string} generatedFieldName
 * @property {string|null} description
 * @property {ReadonlyMap<string, SemanticMemberDescription>} members
 * @property {SemanticMemberDescription|null} metadata
 * @property {ReadonlyMap<string, GraphQLTypeDescription>} types
 */

/**
 * @typedef {Object} ObjectIdentity
 * @property {string} logicalTypeName
 * @property {string} id
 */

/**
 * @typedef {Object} HeaderRequirement
 * @property {'header'} kind
 */

/**
 * @typedef {Object} PropertyRequirement
 * @property {'property'} kind
 * @property {string} member
 */

/**
 * @typedef {Object} ActionRequirement
 * @property {'action'} kind
 * @property {string} member
 */

/**
 * Semantic metadata for one bounded collection response.
 * Presentation components may derive labels and controls from this state but do not own its consistency rules.
 *
 * @typedef {Object} CollectionWindowState
 * @property {number} offset
 * @property {number} requestedSize
 * @property {number} returnedCount
 * @property {number|null} totalCount
 * @property {boolean} countAvailable
 * @property {number} maximumSize
 * @property {boolean} hasPrevious
 * @property {boolean} hasNext
 * @property {number|null} previousOffset
 * @property {number|null} nextOffset
 * @property {number|null} rangeStart
 * @property {number|null} rangeEnd
 * @property {'CONFIGURED'|'ENCOUNTER'|null} ordering
 */

/**
 * @typedef {Object} CollectionRequirement
 * @property {'collection'} kind
 * @property {string} member
 */

/**
 * @typedef {HeaderRequirement|PropertyRequirement|ActionRequirement|CollectionRequirement} SemanticReadRequirement
 */

/**
 * @typedef {Object} RequirementState
 * @property {'idle'|'schema-loading'|'object-loading'|'ready'|'partial-error'|'terminal-error'|'unsupported'} status
 * @property {SemanticReadRequirement} requirement
 * @property {unknown} [data]
 * @property {SemanticMemberDescription|null} [descriptor]
 * @property {GraphQLErrorData[]} errors
 * @property {number} generation
 */

/**
 * @typedef {Object} ObjectSnapshot
 * @property {Record<string, unknown>} data
 * @property {Record<string, unknown>} selection
 * @property {GraphQLErrorData[]} errors
 * @property {number} generation
 */

/**
 * @typedef {Object} ObjectContextState
 * @property {'idle'|'schema-loading'|'object-loading'|'ready'|'partial-error'|'terminal-error'} status
 * @property {number} generation
 * @property {ObjectSnapshot|null} snapshot
 * @property {GraphQLErrorData[]} errors
 * @property {Error|null} error
 */

export const ObjectContextStatus = Object.freeze({
  IDLE: 'idle',
  SCHEMA_LOADING: 'schema-loading',
  OBJECT_LOADING: 'object-loading',
  READY: 'ready',
  PARTIAL_ERROR: 'partial-error',
  TERMINAL_ERROR: 'terminal-error'
});

export const InteractionStatus = Object.freeze({
  IDLE: 'idle',
  PREPARING: 'preparing',
  EDITING: 'editing',
  VALIDATING: 'validating',
  SAVING: 'saving',
  INVOKING: 'invoking',
  SUCCESS: 'success',
  FAILED: 'failed',
  CANCELLED: 'cancelled',
  UNSUPPORTED: 'unsupported',
  OBSOLETE: 'obsolete'
});

export const InteractionResultKind = Object.freeze({
  OBJECT: 'object',
  COLLECTION: 'collection',
  SCALAR: 'scalar',
  VOID: 'void'
});

export const RequirementStatus = Object.freeze({
  IDLE: 'idle',
  SCHEMA_LOADING: 'schema-loading',
  OBJECT_LOADING: 'object-loading',
  READY: 'ready',
  PARTIAL_ERROR: 'partial-error',
  TERMINAL_ERROR: 'terminal-error',
  UNSUPPORTED: 'unsupported'
});
