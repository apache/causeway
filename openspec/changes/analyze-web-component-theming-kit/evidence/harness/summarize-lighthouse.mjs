/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {mkdirSync, readFileSync, readdirSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const sourceDirectory = resolve(directory, '../lighthouse');
const resultDirectory = resolve(directory, '../results');
const reports = {};

for (const reportName of readdirSync(sourceDirectory).sort()) {
  const report = JSON.parse(readFileSync(resolve(sourceDirectory, reportName, 'report.json'), 'utf8'));
  reports[reportName] = {
    url: report.finalDisplayedUrl,
    fetchTime: report.fetchTime,
    lighthouseVersion: report.lighthouseVersion,
    userAgent: report.userAgent,
    categories: Object.fromEntries(Object.entries(report.categories).map(([name, category]) => [name, Math.round(category.score * 100)])),
    failedAudits: Object.values(report.audits)
      .filter(audit => audit.score !== null && audit.score < 1 && audit.scoreDisplayMode !== 'notApplicable')
      .map(audit => ({
        id: audit.id,
        title: audit.title,
        score: audit.score,
        displayValue: audit.displayValue ?? null,
        items: (audit.details?.items ?? []).map(item => ({
          selector: item.node?.selector ?? null,
          snippet: item.node?.snippet ?? null,
          explanation: item.node?.explanation ?? null
        }))
      }))
  };
}

const result = {generatedAt: new Date().toISOString(), reports};
mkdirSync(resultDirectory, {recursive: true});
writeFileSync(resolve(resultDirectory, 'lighthouse-summary.json'), `${JSON.stringify(result, null, 2)}\n`);
writeFileSync(resolve(resultDirectory, 'lighthouse-summary.md'), renderMarkdown(result));
console.log(`Summarized ${Object.keys(reports).length} Lighthouse reports.`);

function renderMarkdown(summary) {
  const lines = ['# Lighthouse summary', '', `Generated: ${summary.generatedAt}`, ''];
  for (const [name, report] of Object.entries(summary.reports)) {
    lines.push(`## ${name}`, '');
    lines.push(`- Scores: ${Object.entries(report.categories).map(([category, score]) => `${category} ${score}`).join(', ')}.`);
    lines.push(`- Failed audits: ${report.failedAudits.length}.`);
    for (const audit of report.failedAudits) {
      lines.push(`- ${audit.id}: ${audit.title}; affected items ${audit.items.length}.`);
      for (const item of audit.items.slice(0, 12)) {
        lines.push(`  - ${item.selector ?? 'document'}: ${(item.explanation ?? item.snippet ?? '').replaceAll('\n', ' ').slice(0, 240)}`);
      }
    }
    lines.push('');
  }
  return `${lines.join('\n')}\n`;
}
