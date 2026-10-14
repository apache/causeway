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

            var container = $('#' + config.canvasId);
            var width = config.initialWidth || 800;
            var height = config.initialHeight || 600;
            var enableDragging = config.enableDragging !== false;
            var enableZoom = config.enableZoom !== false;

            container.empty();
            var svg = container.append('<svg width="100%" height="100%"></svg>')
                .find('svg');
            svg.attr('width', width).attr('height', height);

            // Parse the graph data (expected as JSON)
            var nodes, links;
            try {
                var data = JSON.parse(config.graphData);
                nodes = data.nodes || [];
                links = data.links || [];
            } catch (e) {
                console.error('D3 graph data parse error:', e);
                return;
            }

            // Create the force simulation
            var simulation = d3.forceSimulation(nodes)
                .force('link', d3.forceLink(links)
                    .id(function (d) { return d.id; })
                    .distance(100))
                .force('charge', d3.forceManyBody().strength(-300))
                .force('center', d3.forceCenter(width / 2, height / 2));

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

            // Links
            var link = g.append('g')
                .attr('class', 'd3-links')
                .selectAll('line')
                .data(links)
                .join('line')
                .attr('stroke', '#999')
                .attr('stroke-opacity', 0.6)
                .attr('stroke-width', function (d) { return Math.sqrt(d.value || 1); });

            // Nodes
            var node = g.append('g')
                .attr('class', 'd3-nodes')
                .selectAll('circle')
                .data(nodes)
                .join('circle')
                .attr('r', 8)
                .attr('fill', '#69b3a2');

            // Node labels
            g.append('g')
                .attr('class', 'd3-labels')
                .selectAll('text')
                .data(nodes)
                .join('text')
                .text(function (d) { return d.name || d.id; })
                .attr('font-size', 12)
                .attr('dx', 12)
                .attr('dy', 4);

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

            // Simulation tick
            simulation.on('tick', function () {
                link
                    .attr('x1', function (d) { return d.source.x; })
                    .attr('y1', function (d) { return d.source.y; })
                    .attr('x2', function (d) { return d.target.x; })
                    .attr('y2', function (d) { return d.target.y; });

                node
                    .attr('cx', function (d) { return d.x; })
                    .attr('cy', function (d) { return d.y; });

                svg.selectAll('.d3-labels text')
                    .attr('x', function (d) { return d.x; })
                    .attr('y', function (d) { return d.y; });
            });

            // Store the simulation for cleanup
            container.data('d3-simulation', simulation);
        },

        destroy: function (canvasId) {
            var container = $('#' + canvasId);
            var simulation = container.data('d3-simulation');
            if (simulation) {
                simulation.stop();
                container.removeData('d3-simulation');
            }
            container.empty();
        }

    };

})(jQuery);
