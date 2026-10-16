/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
;(function ($, undefined) {

    'use strict';

    if (!d3) {
        console.error("Missing D3 prerequisites.");
        return;
    }

    if (typeof(WicketStuff) !== 'object') {
        window.WicketStuff = {};
    }

    if (typeof(WicketStuff.D3) === 'object') {
        return;
    }

    var PLACEHOLDER = "<PLACEHOLDER>";

    WicketStuff.D3 = {

        init: function (config) {

            var containerEl = document.getElementById(config.canvasId);
            if (!containerEl) {
                console.error('D3 canvas element not found:', config.canvasId);
                return;
            }
            // Measure available space from the container; fall back to config values
            var width = containerEl.clientWidth || config.initialWidth || 800;
            var height = containerEl.clientHeight || config.initialHeight || Math.round(width * 0.6);
            var enableDragging = config.enableDragging !== false;
            var enableZoom = config.enableZoom !== false;

            // Use D3 selections throughout — never mix jQuery with D3 selection methods
            var svg = d3.select(containerEl)
                .append('svg')
                .attr('width', '100%')
                .attr('height', height)
                .attr('viewBox', '0 0 ' + width + ' ' + height)
                .attr('preserveAspectRatio', 'xMidYMid meet');

            // Parse the graph data (expected as JSON);
            // if graphData is null, undefined, or empty, use an empty graph
            var nodes, links;
            try {
                if (config.graphData) {
                    var data = JSON.parse(config.graphData);
                    nodes = data.nodes || [];
                    links = data.links || [];
                } else {
                    nodes = [];
                    links = [];
                }
            } catch (e) {
                console.error('D3 graph data parse error:', e);
                return;
            }

            // If there's no data, render an empty graph placeholder
            if (nodes.length === 0 && links.length === 0) {
                svg.append('text')
                    .attr('x', width / 2)
                    .attr('y', height / 2)
                    .attr('text-anchor', 'middle')
                    .attr('font-family', 'sans-serif')
                    .attr('font-size', '14px')
                    .attr('fill', '#999')
                    .text('No graph data available');
                return;
            }

            // Force parameters from config (with sensible defaults)
            var linkDistance = config.linkDistance != null ? config.linkDistance : 120;
            var repulsionStrength = config.repulsionStrength != null ? config.repulsionStrength : -400;
            var collisionRadius = config.collisionRadius != null ? config.collisionRadius : 60;
            var distStrength = config.distributionStrength != null ? config.distributionStrength : 0.08;

            // Create the force simulation
            // Use forceX/forceY (weaker than forceCenter) to spread nodes across the canvas,
            // combined with collision detection to prevent overlap.
            var simulation = d3.forceSimulation(nodes)
                .force('link', d3.forceLink(links)
                    .id(function (d) { return d.id; })
                    .distance(linkDistance))
                .force('charge', d3.forceManyBody()
                    .strength(repulsionStrength))
                .force('collide', d3.forceCollide()
                    .radius(function (d) {
                        return (d.r || 8) + collisionRadius;
                    }))
                .force('x', d3.forceX(width / 2).strength(distStrength))
                .force('y', d3.forceY(height / 2).strength(distStrength));

            // Re-center/distribute on window resize so the force layout stays proportioned to the container
            window.addEventListener('resize', function () {
                var w = containerEl.clientWidth;
                var h = containerEl.clientHeight;
                if (w && (w !== width || h !== height)) {
                    width = w;
                    height = h || height;
                    svg.attr('viewBox', '0 0 ' + width + ' ' + height);
                    simulation.force('x', d3.forceX(width / 2).strength(distStrength));
                    simulation.force('y', d3.forceY(height / 2).strength(distStrength));
                    simulation.alpha(0.5).restart();
                }
            });

            // Zoom behaviour (applied to the container)
            var g = svg.append('g');
            if (enableZoom) {
                var zoom = d3.zoom()
                    .scaleExtent([0.1, 10])
                    .on('zoom', function (event) {
                        g.attr('transform', event.transform);
                    });
                svg.call(zoom);
            }

            // ---- Extract legend from data ----
            var legend = data.legend || [];

            // ---- Links (lines) ----
            var link = g.append('g')
                .attr('class', 'd3-links')
                .selectAll('line')
                .data(links)
                .join('line')
                .attr('stroke', '#999')
                .attr('stroke-opacity', 0.6)
                .attr('stroke-width', function (d) { return Math.sqrt(d.value || 1); });

            // ---- Edge labels ----
            var linkLabel = g.append('g')
                .attr('class', 'd3-link-labels')
                .selectAll('text')
                .data(links)
                .join('text')
                .text(function (d) { return d.label || ''; })
                .attr('font-size', 9)
                .attr('fill', '#555')
                .attr('text-anchor', 'middle')
                .attr('dominant-baseline', 'central');

            // ---- Helper functions for node rendering ----
            function nodeScale(d) {
                return (d.r || 8) / 8;
            }
            function clampFont(px) {
                return Math.max(6, Math.min(18, Math.round(px)));
            }

            // ---- Nodes (rounded rectangles + 3-line labels) ----
            var node = g.append('g')
                .attr('class', 'd3-nodes')
                .selectAll('g')
                .data(nodes)
                .join('g');

            // Rounded rectangle behind the label (fitted after measuring)
            node.append('rect')
                .attr('rx', 9)
                .attr('ry', 9)
                .attr('fill', function (d) { return d.color || '#69b3a2'; })
                .attr('stroke', '#333')
                .attr('stroke-width', function (d) {
                    return d.approved ? 4 : 2;
                })
                .attr('stroke-dasharray', function (d) {
                    return d.synonym ? '6,4' : null;
                });

            // 3-line label: italic subdomain / name / <<POS>>
            var nodeText = node.append('text')
                .attr('text-anchor', 'middle')
                .attr('dominant-baseline', 'central')
                .attr('fill', '#fff')
                .attr('pointer-events', 'none');

            // Line 1: subdomain in italics
            nodeText.append('tspan')
                .attr('x', 0)
                .attr('dy', '-0.9em')
                .attr('font-style', 'italic')
                .attr('font-size', function (d) {
                    return clampFont(7 * nodeScale(d));
                })
                .text(function (d) { return d.subdomain || ''; });

            // Line 2: candidate name (bold)
            nodeText.append('tspan')
                .attr('x', 0)
                .attr('dy', '1.15em')
                .attr('font-weight', 'bold')
                .attr('font-size', function (d) {
                    return clampFont(11 * nodeScale(d));
                })
                .text(function (d) { return d.name || d.id; });

            // Line 3: POS in double brackets
            nodeText.append('tspan')
                .attr('x', 0)
                .attr('dy', '1.15em')
                .attr('font-size', function (d) {
                    return clampFont(7 * nodeScale(d));
                })
                .text(function (d) { return '<<' + (d.pos || '') + '>>'; });

            // Fit the rectangle around the measured label
            node.each(function (d) {
                var labelEl = d3.select(this).select('text').node();
                if (!labelEl) return;
                var bbox = labelEl.getBBox();
                var sf = nodeScale(d) || 1;
                var pad = 7 * sf;
                var w = bbox.width + pad * 2;
                var h = bbox.height + pad * 2;
                d3.select(this).select('rect')
                    .attr('width', w)
                    .attr('height', h)
                    .attr('x', -w / 2)
                    .attr('y', -h / 2);
            });

            // Node click handler (if a callback URL is configured)
            if (config.clickCallbackUrl) {
                node.style('cursor', 'pointer')
                    .on('click', function (event, d) {
                        var url = config.clickCallbackUrl.replace(PLACEHOLDER, '')
                            + '&nodeId=' + encodeURIComponent(d.id);
                        Wicket.Ajax.get({'u': url});
                    });
            }

            // Drag behaviour
            if (enableDragging) {
                var drag = d3.drag()
                    .on('start', function (event, d) {
                        if (!event.active) simulation.alphaTarget(0.3).restart();
                        d.fx = d.x;
                        d.fy = d.y;
                    })
                    .on('drag', function (event, d) {
                        d.fx = event.x;
                        d.fy = event.y;
                    })
                    .on('end', function (event, d) {
                        if (!event.active) simulation.alphaTarget(0);
                        d.fx = null;
                        d.fy = null;
                    });
                node.call(drag);
            }

            // ---- Legend ----
            if (legend.length > 0) {
                var legendGroup = svg.append('g')
                    .attr('class', 'd3-legend')
                    .attr('transform', function () {
                        return 'translate(20, ' + (height - 40 - legend.length * 28) + ')';
                    });
                legendGroup.append('rect')
                    .attr('width', 240)
                    .attr('height', legend.length * 28 + 10)
                    .attr('fill', 'rgba(255, 255, 255, 0.92)')
                    .attr('stroke', '#ccc')
                    .attr('rx', 4);
                legendGroup.append('text')
                    .attr('x', 8).attr('y', 18)
                    .attr('font-size', 11).attr('font-weight', 'bold')
                    .attr('fill', '#333')
                    .text('Node Type Legend');
                legend.forEach(function (item, i) {
                    var rowY = 35 + i * 28;
                    var swatch = legendGroup.append('g');
                    swatch.append('rect')
                        .attr('x', 8).attr('y', rowY)
                        .attr('width', 14).attr('height', 14)
                        .attr('fill', item.color).attr('rx', 2);
                    swatch.append('text')
                        .attr('x', 28).attr('y', rowY + 11)
                        .attr('font-size', 10).attr('fill', '#444')
                        .text((item.label || '') + ' \u2014 ' + (item.description || ''));
                });
            }

            // ---- Simulation tick ----
            simulation.on('tick', function () {
                link
                    .attr('x1', function (d) { return d.source.x; })
                    .attr('y1', function (d) { return d.source.y; })
                    .attr('x2', function (d) { return d.target.x; })
                    .attr('y2', function (d) { return d.target.y; });

                linkLabel
                    .attr('x', function (d) { return (d.source.x + d.target.x) / 2; })
                    .attr('y', function (d) { return (d.source.y + d.target.y) / 2; });

                node.attr('transform', function (d) {
                    return 'translate(' + d.x + ',' + d.y + ')';
                });
            });

            // Store the simulation for cleanup (on the DOM element)
            containerEl._d3Simulation = simulation;
        },

        destroy: function (canvasId) {
            var containerEl = document.getElementById(canvasId);
            if (!containerEl) return;
            var simulation = containerEl._d3Simulation;
            if (simulation) {
                simulation.stop();
                delete containerEl._d3Simulation;
            }
            // Clear all children (SVG and any other content)
            while (containerEl.firstChild) {
                containerEl.removeChild(containerEl.firstChild);
            }
        }

    };

})(jQuery);
