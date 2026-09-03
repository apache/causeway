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

import type {InjectionKey, Plugin} from 'vue';
import type {Router} from 'vue-router';
import type {CausewayPageRegistration, CausewayPageRegistryInput} from './registry';

export interface CausewayObjectTarget {
  readonly logicalTypeName?: string;
  readonly id?: string;
  readonly objectId?: string;
  readonly title?: string;
}

export interface CausewayLocalResourceTarget {
  readonly path: string;
  readonly openUrlStrategy: 'SAME_WINDOW' | 'NEW_WINDOW';
}

export interface CausewaySemanticResult {
  readonly kind: 'object' | 'scalar' | 'collection' | 'local-resource' | 'unsupported' | 'void' | string;
  readonly value?: unknown;
  readonly reason?: string;
}

export interface CausewayActionRequest {
  readonly actionId: string;
  readonly serviceLogicalTypeName?: string;
  readonly identity?: CausewayObjectTarget | null;
  readonly target?: object | null;
  readonly context?: object;
  readonly presentation?: Record<string, unknown>;
}

export interface CausewayEventClaim {
  readonly claimed: boolean;
  claim(): boolean;
}

export interface CausewayPolicyContext {
  readonly router: Router;
  readonly basePath: string;
  readonly shell: HTMLElement | null;
  readonly routeGeneration: number;
}

export interface CausewayViewerPolicies {
  readonly menuActionLabel?: (detail: CausewayActionRequest, context: CausewayPolicyContext) => string | void;
  readonly action?: (detail: CausewayActionRequest, claim: CausewayEventClaim, context: CausewayPolicyContext) => boolean | void | Promise<boolean | void>;
  readonly navigate?: (target: CausewayObjectTarget, claim: CausewayEventClaim, context: CausewayPolicyContext) => boolean | void | Promise<boolean | void>;
  readonly home?: (entry: unknown, claim: CausewayEventClaim, context: CausewayPolicyContext) => boolean | void | Promise<boolean | void>;
  readonly result?: (detail: unknown, claim: CausewayEventClaim, context: CausewayPolicyContext) => boolean | void | Promise<boolean | void>;
  readonly error?: (error: unknown, context: CausewayPolicyContext) => void;
}

export interface CausewayViewerOptions {
  readonly router: Router;
  readonly endpoint: string;
  readonly basePath?: string;
  readonly applicationResourceBase?: string;
  readonly pages?: CausewayPageRegistryInput;
  readonly policies?: CausewayViewerPolicies;
  readonly developmentDiagnostics?: boolean;
}

export interface CausewayViewerRuntime {
  readonly plugin: Plugin;
  readonly router: Router;
  readonly endpoint: string;
  readonly basePath: string;
  readonly applicationResourceBase: string;
  readonly pages: ReadonlyMap<string, CausewayPageRegistration>;
  readonly policies: CausewayViewerPolicies;
  readonly developmentDiagnostics: boolean;
  readonly state: {
    shell: HTMLElement | null;
    routeGeneration: number;
  };
}

export const CAUSEWAY_VIEWER_KEY: InjectionKey<CausewayViewerRuntime> = Symbol('causeway-vue-viewer');

export interface CausewayRoutePageProps {
  readonly logicalTypeName: string;
  readonly objectId: string;
  readonly routeKey: string;
}
