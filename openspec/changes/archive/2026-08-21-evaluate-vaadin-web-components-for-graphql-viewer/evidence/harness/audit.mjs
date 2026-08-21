/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {spawnSync} from 'node:child_process';
import {mkdirSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const audit = spawnSync('npm', ['audit', '--omit=dev', '--json'], {cwd: directory, encoding: 'utf8', maxBuffer: 50_000_000});
const report = JSON.parse(audit.stdout || '{}');
const result = {generatedAt: new Date().toISOString(), command: 'npm audit --omit=dev --json', exitCode: audit.status, metadata: report.metadata, vulnerabilities: report.vulnerabilities ?? {}};
mkdirSync(resolve(directory, '..', 'results'), {recursive: true});
writeFileSync(resolve(directory, '..', 'results', 'security-audit.json'), `${JSON.stringify(result, null, 2)}\n`);
console.log(JSON.stringify({exitCode: audit.status, vulnerabilities: report.metadata?.vulnerabilities, dependencies: report.metadata?.dependencies}, null, 2));
if (audit.status && !report.metadata) process.exitCode = audit.status;
