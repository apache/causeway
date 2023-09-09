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

/* -- DEFAULTS
var ropts = {
		scale : 1.0,
		node_radius: 5,
		arrow_length: 8,
		arrow_breadth: 5,
		enable_nodelabels: true,
		enable_edgelabels: true,
		enable_edgearrows: true,
		enable_stickyNodeDrag: true
};*/

var frameBox = {
		_width: Math.round(1800/ropts.scale), 
		_height: Math.round(1200/ropts.scale), 
		border_padding: 2,
		node_radius: ropts.node_radius,
		width: function () {
			return this._width;
		},
		height: function () {
			return this._height;
		},
		minX: function () {
			return 0;
		},
		minY: function () {
			return 0;
		},
		maxX: function () {
			return this.width();
		},
		maxY: function () {
			return this.height();
		},
		getViewBoxLiteral: function () {
			return ' ' + (this.minX()-this.border_padding) 
				+ ' ' + (this.minY()-this.border_padding) 
				+ ' ' + (this.maxX()+this.border_padding) 
				+ ' ' + (this.maxY()+this.border_padding);
		},
		forceBoxBounded: function(x, y) {
			var nodes;

			if (x == null) x = this.width() / 2;
			if (y == null) y = this.height() / 2;

			const minX=this.minX()+this.node_radius,
				minY=this.minY()+this.node_radius,
				maxX=this.maxX()-this.node_radius,
				maxY=this.maxY()-this.node_radius;

			function force() {
				var i,
				n = nodes.length,
				node;

				for (i = 0; i < n; ++i) {
					node = nodes[i];
					if(node.x>maxX){ node.x=maxX; }
					if(node.y>maxY){ node.y=maxY; }
					if(node.x<minX){ node.x=minX; }
					if(node.y<minY){ node.y=minY; }
				}
			}

			force.initialize = function(_) {
				nodes = _;
			};

			force.x = function(_) {
				return arguments.length ? (x = +_, force) : x;
			};

			force.y = function(_) {
				return arguments.length ? (y = +_, force) : y;
			};

			return force;
		},
		bind: function(target){
			target.attr("preserveAspectRatio", "xMinYMin meet");
			target.attr("viewBox", this.getViewBoxLiteral());	

			target.append("rect")
			.attr("class", "frame-box")
			.attr("x", this.minX())
			.attr("y", this.minY())
			.attr("width", this.width())
			.attr("height", this.height());
		}
};

// render-model
var rmodel = {
		nodes	: null,
		nodelabels  : null,
		edgelabels  : null,
		edgelines  : null,
		rerenderFunction : null,
		highlightedNodeIds : new Set(),
		honorHighlightedNodes: function() {
			let edgelines = this.edgelines;
			let edgelabels = this.edgelabels;
			let highlightedNodeIds = this.highlightedNodeIds;
			
			// if nothing is highlighted, render default opacities
			if(highlightedNodeIds.size == 0) {
				if(edgelines) { edgelines.attr("stroke-opacity", 0.6); }
				if(edgelabels) { edgelabels.attr("opacity", 1); }
				return;
			}
			
			// update edge lines' opacity
			if(edgelines){
				edgelines.attr("stroke-opacity", function(d) {
					//console.log("edgeTo " + d.target.id + "->" + highlightedNodeIds.has(d.target.id));
					return highlightedNodeIds.has(d.target.id) ? ".6" : ".1"; 
				});
			}
			// update edge labels' opacity
			if(edgelabels){
				edgelabels.attr("opacity", function(d) {
					return highlightedNodeIds.has(d.target.id) ? "1" : ".2"; 
				});
			}
		}
	};

function renderForceDirectedGraph(data, noteText) {

	var svg = d3.select(".force-directed-graph");

	frameBox.bind(svg);

	var color = d3.scaleOrdinal(d3.schemeCategory20);
	
	{ // options

		let inset = 5;
		let textHeight = 24;

		svg.append("text")
			.attr("x", inset)
			.attr("y", inset+textHeight*0.5)
			.text(noteText);
	
		var checkBox1 = new svgCheckBox("Node Labels").x(inset).y(inset+textHeight*1).rx(5).ry(5)
			.checked(ropts.enable_nodelabels);
		var checkBox2 = new svgCheckBox("Edge Labels").x(inset).y(inset+textHeight*2).rx(5).ry(5)
			.checked(ropts.enable_edgelabels);
		var checkBox3 = new svgCheckBox("Edge Arrows").x(inset).y(inset+textHeight*3).rx(5).ry(5)
			.checked(ropts.enable_edgearrows);
		
		var updateCB = function () {
		
			var checkBox1changed = ropts.enable_nodelabels != checkBox1.checked();
			var checkBox2changed = ropts.enable_edgelabels != checkBox2.checked();
			var checkBox3changed = ropts.enable_edgearrows != checkBox3.checked();
		
			ropts.enable_nodelabels = checkBox1.checked();
			ropts.enable_edgelabels = checkBox2.checked();
			ropts.enable_edgearrows = checkBox3.checked();
			
			function invalidateAll(){
				svg.selectAll(".nodes").remove();
				rmodel.nodes = null;
				svg.selectAll(".links").remove();
				rmodel.edgelines = null;
				svg.selectAll(".nodelabels").remove();
				rmodel.nodelabels = null;
				svg.selectAll(".edgelabel").remove();
				rmodel.edgelabels = null;
			}
			
			if(!ropts.enable_nodelabels) {
				svg.selectAll(".nodelabels").remove();
				rmodel.nodelabels = null;
			} 
			
			if(!ropts.enable_edgelabels) {
				svg.selectAll(".edgelabel").remove();
				rmodel.edgelabels = null;
			}
			
			if(!ropts.enable_edgearrows) {
				invalidateAll();
			} else {
				// invalide on re-enable as well
				if(checkBox3changed) {
					invalidateAll();
				}
			}
			
			if(rmodel.rerenderFunction!=null) {
				rmodel.rerenderFunction(); // trigger re-render
			}
			
		};

		//Setting up each check box
		checkBox1.clickEvent(updateCB);
		checkBox2.clickEvent(updateCB);
		checkBox3.clickEvent(updateCB);

		svg.call(checkBox1);
		svg.call(checkBox2);
		svg.call(checkBox3);
		
		updateCB();
	
	}

	var simulation = d3.forceSimulation()
	.force("link", d3.forceLink().id((d) => d.id ))
	//.force("charge", d3.forceManyBody().strength(-100))
	.force("collide", d3.forceCollide((d) => 25))
	.force("center", d3.forceCenter(frameBox.width()/2, frameBox.height()/2))
	.force("boxBounded", frameBox.forceBoxBounded())
	;

	function renderGraph(graph) {
	
		// factories
	
		function createEdges() {

			var link = svg.append("g")
			.attr("class", "links")
			.selectAll("line")
			.data(graph.links)
			.enter().append("line")
			.style("pointer-events", "none")
			.attr("stroke-width", function(d) { return Math.sqrt(d.weight); })
			;
			
			if(ropts.enable_edgearrows){
			
				var a = ropts.arrow_length,
					b = ropts.arrow_breadth,
					r = ropts.node_radius,
					b2 = b*0.5;
			
				svg.append('defs').append('marker')
				.attr('id','arrowhead')
				.attr('viewBox','-0 -'+b2+' '+a+' '+b)
				.attr('refX', a+r)
				.attr('refY', 0)
				.attr('orient', 'auto')
				.attr('markerWidth', a)
				.attr('markerHeight', b)
				.attr('xoverflow', 'visible')
				
				.append('svg:path')
					.attr('d', 'M 0,-'+b2+' L '+a+',0 L 0,'+b2)
					.attr('fill', '#ccc')
					.attr('stroke','#ccc')
						
				;
				
				link.attr('marker-end','url(#arrowhead)');
			}
			
			return link;
		}
		
		function createNodes() {

			var node = svg.append("g")
			.attr("class", "nodes")
			.selectAll("circle")
			.data(graph.nodes)
			.enter().append("circle")
			.attr("r", ropts.node_radius)
			.attr("fill", function(d) { return color(d.group); })
			.call(d3.drag()
					.on("start", dragstarted)
					.on("drag", dragged)
					.on("end", dragended))
			.on("dblclick", releaseNode)
			.on("click", toggleNodeHighlight)
			;
			
			node.append("title")
				.text(function(d) { return d.description; });
			
			return node;
		}

		function createNodeLabels() {
			return svg.append("g")
				.attr("class", "nodelabels")
				.selectAll(".nodelabel") 
				.data(graph.nodes)
				.enter()
				.append("text")
				.style("pointer-events", "none")
				.attr("class", "nodelabel")
				.text(function(d){ return d.label; });
		}
		
		function createEdgeLabels() {
			return svg.append("g")
				.attr("class", "edgelabels")
				.selectAll(".edgelabel")
				.data(graph.links)
				.enter()
				.append('text')
				.style("pointer-events", "none")
				.style("text-anchor", "middle")
				.attr("class", "edgelabel")
				.text(function(d){return d.label;});
		}
		
		// getters
		
		function getNodes() {
			if(rmodel.nodes != null) {
				return rmodel.nodes;
			}
			rmodel.nodes = createNodes();
			return rmodel.nodes;
		}
		
		function getEdges() {
			if(rmodel.edgelines != null) {
				return rmodel.edgelines;
			}
			rmodel.edgelines = createEdges();
			rmodel.honorHighlightedNodes();
			return rmodel.edgelines;
		}
		
		function getNodeLabels() {
			if(!ropts.enable_nodelabels) {
				return null;
			}
			if(rmodel.nodelabels != null) {
				return rmodel.nodelabels;
			}
			rmodel.nodelabels = createNodeLabels();
			return rmodel.nodelabels;
		}
		
		function getEdgeLabels() {
			if(!ropts.enable_edgelabels) {
				return null;
			}
			if(rmodel.edgelabels != null) {
				return rmodel.edgelabels;
			}
			rmodel.edgelabels = createEdgeLabels();
			rmodel.honorHighlightedNodes();
			return rmodel.edgelabels;
		}
	  

		simulation
		.nodes(graph.nodes)
		.on("tick", ticked);

		simulation.force("link")
		.links(graph.links);

		function ticked() {
		
		
			getEdges()
			.attr("x1", function(d) { return d.source.x; })
			.attr("y1", function(d) { return d.source.y; })
			.attr("x2", function(d) { return d.target.x; })
			.attr("y2", function(d) { return d.target.y; })
			;

			getNodes()
			.attr("cx", function(d) { return d.x; })
			.attr("cy", function(d) { return d.y; })
			;

			if(ropts.enable_nodelabels) {
				getNodeLabels()
				.attr("x", function(d) { return d.x+ropts.node_radius; })
				.attr("y", function(d) { return d.y; })
				;
			} 
				
			if(ropts.enable_edgelabels)
				getEdgeLabels()
				.attr("x", function(d) { return (d.source.x + d.target.x)*0.5; })
				.attr("y", function(d) { return (d.source.y + d.target.y)*0.5; })
				.attr('transform', function(d){
					var cx = (d.source.x + d.target.x)*0.5;
					var cy = (d.source.y + d.target.y)*0.5;
					var dx = d.target.x - d.source.x;
					var dy = d.target.y - d.source.y;
					var degree = Math.atan2(dy, dx)*57.2958; //180./3.141592654; 
					if (d.target.x<d.source.x)
						degree+=180;
					return 'rotate('+degree+' '+cx+' '+cy+')';
				})
				;
		}
		
		rmodel.rerenderFunction = ticked;
		
	}

	renderGraph(data);

	function dragstarted(d) {
		if (!d3.event.active) simulation.alphaTarget(0.3).restart();
		d.fx = d.x;
		d.fy = d.y;
	}

	function dragged(d) {
		d.fx = d3.event.x;
		d.fy = d3.event.y;
	}

	function dragended(d) {
		if (!d3.event.active) simulation.alphaTarget(0);
		if(!ropts.enable_stickyNodeDrag){
			releaseNode(d);
		}
	}
	
	function releaseNode(d) {
		d.fx = null;
		d.fy = null;
	}
	
	function toggleNodeHighlight(node) {
		let nodeId = node.id;
		if(rmodel.highlightedNodeIds.has(nodeId)) {
			rmodel.highlightedNodeIds.delete(nodeId);
			//console.log("del " + nodeId);
		} else {
			rmodel.highlightedNodeIds.add(nodeId);
			//console.log("add " + nodeId);
		}
		rmodel.honorHighlightedNodes();
	}

}