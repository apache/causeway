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

import {shallowRef} from 'vue';

export interface AuthenticationContext {
  readonly username: string;
  readonly csrfHeaderName: string;
  readonly csrfParameterName: string;
  readonly csrfToken: string;
  readonly loginPath: string;
  readonly logoutPath: string;
}

interface GraphQLRequest {
  readonly document: string;
  readonly variables?: Record<string, unknown>;
  readonly operationName?: string;
  readonly signal?: AbortSignal;
}

export type GraphQLExecutor = (request: GraphQLRequest) => Promise<unknown>;

export const authenticationContext = shallowRef<AuthenticationContext | null>(null);
export const graphQlExecutor = shallowRef<GraphQLExecutor | undefined>(undefined);

export async function bootstrapAuthentication(endpoint = '/graphql'): Promise<void> {
  const contextEndpoint = document.querySelector<HTMLMetaElement>(
    'meta[name="causeway-authentication-context"]'
  )?.content.trim();
  if (!contextEndpoint) return;

  const response = await fetch(contextEndpoint, {
    credentials: 'same-origin',
    headers: {accept: 'application/json'},
    cache: 'no-store'
  });
  if (response.status === 401) {
    location.assign('/vue/login');
    return;
  }
  if (!response.ok) throw new Error(`Authentication context failed with HTTP ${response.status}.`);
  const context = await response.json() as AuthenticationContext;
  authenticationContext.value = Object.freeze(context);
  graphQlExecutor.value = createSecuredExecutor(endpoint, context);
}

function createSecuredExecutor(endpoint: string, context: AuthenticationContext): GraphQLExecutor {
  return async ({document, variables = {}, operationName, signal}: GraphQLRequest) => {
    const response = await fetch(endpoint, {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        accept: 'application/graphql-response+json, application/json',
        'content-type': 'application/json',
        [context.csrfHeaderName]: context.csrfToken
      },
      body: JSON.stringify({query: document, variables, operationName}),
      signal
    });
    if (response.status === 401) {
      const continuation = safeVueContinuation();
      const separator = context.loginPath.includes('?') ? '&' : '?';
      location.assign(context.loginPath + (continuation
        ? `${separator}continue=${encodeURIComponent(continuation)}`
        : ''));
      throw new Error('The authenticated session has expired.');
    }
    const text = await response.text();
    let body: unknown;
    try {
      body = text ? JSON.parse(text) : {};
    } catch (cause) {
      throw new Error(`GraphQL response was not valid JSON (HTTP ${response.status}).`, {cause});
    }
    if (!response.ok) {
      const error = new Error(`GraphQL request failed with HTTP ${response.status}.`) as Error & {status: number; responseBody: unknown};
      error.status = response.status;
      error.responseBody = body;
      throw error;
    }
    return body;
  };
}

function safeVueContinuation(): string | null {
  const candidate = `${location.pathname}${location.search}`;
  if (candidate === '/vue' || candidate === '/vue/' || candidate.startsWith('/vue/object/')) return candidate;
  return null;
}
