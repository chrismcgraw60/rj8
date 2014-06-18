/**
 * Encapsulates presentation logic for Most Recent Test History.
 * 
 * This directive is essentially a wrapper around an interactive D3 based scatter plot graph.
 * The graph plots (1)#tests in a given run and (2) non-passing tests in a given run. The disparity
 * between the 2 lines gives a visual indication of test growth and stability over time.
 * 
 * When there are few enough data points to be able to show each individual data-point, then the
 * data point will be represented a solid circle that the user can interact with to get rich hover 
 * info and also ctrl+click to trigger compare data points. 
 */
juaApp.directive('testRunSummaryGraph', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        templateUrl: 'assets/scripts/directives/testRunSummaryGraphTemplate.html',
        
        link: function(scope, element, attrs) {
        	
        	/*
        	 * Initialize dimensions and margins
        	 * TODO: % based width
        	 */
        	var margin 	= {top: 30, right: 20, bottom: 30, left: 50},
        	width 	= 750 - margin.left - margin.right,
			height 	= 300 - margin.top - margin.bottom;

        	/*
        	 * Initialize X/Y ranges.
        	 */
        	var x = d3.time.scale().range([0, width]);
        	var y = d3.scale.linear().range([height, 0]); 
        	
        	/*
        	 * Initialize X/Y axis.
        	 */
        	var xAxis = d3.svg.axis().scale(x).orient("bottom").ticks(5);
        	var yAxis = d3.svg.axis().scale(y).orient("left").ticks(5);
        	
        	/*
        	 * Databind the line that will describe the #Tests Run path.
        	 */
        	var valueline = d3.svg.line()
        		.x(function(d) { return x(d.timestamp); })
        		.y(function(d) { return y(d.testsRun); });
        	
        	/*
        	 * Databind the line that will describe the #Tests Passed path.
        	 */
        	var passingLine = d3.svg.line()
    			.x(function(d) { return x(d.timestamp); })
    			.y(function(d) { return y(d.passing); });
        	
        	/*
        	 * Initialize SVG Canvas.
        	 */
        	var svg = d3.select("#scatter-plot")
        		.append("svg")
        		.attr("width", width + margin.left + margin.right)
        		.attr("height", height + margin.top + margin.bottom)
        		.append("g")
        		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        	        	
        	/*
        	 * We remember the size of the data from the last time we bound to it.
        	 * On each UI update, we check some conditions to decide whether to schedule
        	 * the next UI update. These conditions are:
        	 *  - Results are still being loaded.
        	 *  - The # of results has increased since last time we checked.
        	 * While either of these conditions hold, we continue to schedule UI updates. 
        	 */
        	var resultCountOnLastUpdate = 0;
        	
        	/*
        	 * Define a function that will update the plot graph with a new set of test run
        	 * data points. We will attach this function to the scope so it can be called
        	 * by any code that intends to load data.
        	 */
        	scope.summaryUpdateHandler = function(testRunDataPoints) {
        		
            	resultCountOnLastUpdate = testRunDataPoints.length;
            	
        		/*
        		 * Ensure a clean canvas.
        		 */
        		svg.selectAll('*').remove();
        		
        		/*
        		 * Set X/Y the domains. We show:
        		 * - Full extent of test date range along X.
        		 * - Full extent of test count along Y with some padding 
        		 *   so min val doesn't start from 0 (unless its 0).
        		 */
        		x.domain(d3.extent(testRunDataPoints, function(d) { return d.timestamp; }));
        		var yMax = d3.max(testRunDataPoints, function(d) { return d.testsRun; });
        		var yMin = d3.min(testRunDataPoints, function(d) { return d.passing; });
        		yMin = (yMin-50 < 0) ? 0 : yMin-50;
        		y.domain([yMin, yMax]);
        		
        		/*
        		 * Render axis
        		 */
        		svg.append("g")
        			.attr("class", "x axis")
        			.attr("transform", "translate(0," + height + ")")
        			.call(xAxis);
        		svg.append("g")
        			.attr("class", "y axis")
        			.call(yAxis);
        		svg.append("text")
        			.attr("x", width/2)
        			.attr("y", height + margin.bottom)
        			.style("text-anchor", "middle")
        			.text("Date");
        		svg.append("text")
            		.attr("transform", "rotate(-90)")
            		.attr("y", 0 - margin.left)
            		.attr("x",0 - (height / 2))
            		.attr("dy", "1em")
            		.style("text-anchor", "middle")
            		.text("#Tests");
        		/*
        		 * Render Grid lines
        		 */
        		svg.append("g")
        			.attr("class", "grid")
        			.attr("transform", "translate(0," + height + ")")
        			.call(d3.svg.axis()
                    		.scale(x)
                    		.orient("bottom")
                    		.ticks(5)
        					.tickSize(-height, 0, 0)
        					.tickFormat(""));
        		svg.append("g")
        			.attr("class", "grid")
        			.call(d3.svg.axis()
                    		.scale(y)
                    		.orient("left")
                    		.ticks(5)
        					.tickSize(-width, 0, 0)
        					.tickFormat(""));
        		        		
        		/*
        		 * Plot line of #tests run by date.
        		 */
        		svg.append("path")
        			.attr("d", valueline(testRunDataPoints));
        		
        		/*
        		 * Plot scatter of test run data points by date.
        		 * These should follow the #tests line drawn above.
        		 * 
        		 * When the # data points falls below a certain threshold (75), the graph will display selectable points. 
        		 * As the # data points continues to drop then the points become larger and easier to select.
        		 * 
        		 * Each point expands and shows a tooltip on mouse-over. Each point detects ctrl + right-click and registers
        		 * the selected test run data point as either a compare target, or if it is already registered, it is
        		 * unregistered.
        		 */
        		if (testRunDataPoints.length <= 75) {
            		svg.selectAll("dot")
            		.data(testRunDataPoints)
            		.enter().append("circle")
            			.attr("r", computeNormalRadius(testRunDataPoints))
            			.attr("cx", function(d) { return x(d.timestamp); })
            			.attr("cy", function(d) { return y(d.testsRun); })
            			.style("fill", function(d) { return computeFill(this, d, testRunDataPoints); })
                		.on("mouseover", function(d) {
                			expandPlot(this, testRunDataPoints);
                			toolTipIn(d);
                		})
                		.on("mouseout", function(d) {
                			if (!isSelectedforCompare(d)) {
                				shrinkPlot(this, testRunDataPoints);
                			}
                			toolTipOut();
                		})
                		.on("click", function(d) {
            				if (d3.event.ctrlKey) { 
            					handleCtrlClick(this, d, testRunDataPoints); 
            				}
            				else { 
            					scope.$apply(function() { 
            						$location.path("/testResults/" + d.id); 
            					}); 
            				}
                		})
                		.on("contextmenu", function(d) {
                			scope.$apply(function() { 
                				showCompareContext(d);
                				toolTipOut();
                			});
                		});
        		}
        	
        		/*
        		 * Plot line #test passes on given date. If there are any fails / errors / skipped then this will
        		 * bump along under the #tests line (which should only change gradually).
        		 */
        		svg.append("path")
        			.attr("class", "pass-path")
        			.attr("d", passingLine(scope.rowDataResults));           		                		
            };
                       
        	/*
        	 * Browser on-resize event.
        	 */ 
            window.onresize = function() { scope.$apply(); };
            
            /**************************************************************************************
             * SCATTER PLOTS - Manipulation
             * Change appearance of plotted test runs.
             */
            
            var computeFill = function(element, d, dp) {
            	if (isSelectedforCompare(d)) {
            		return renderPlotAsSelected(element, d, dp);
            	} 
            	else {
            		return (d.errors > 0 || d.failures > 0) ? "DarkRed": "Green";
            	}
            };
            
    		var computeNormalRadius = function(dp) {
    			if (dp.length < 10) { return 12; }
    			else if (dp.length < 15) { return 10; }
    			else if (dp.length < 20) { return 8; }
				else if (dp.length < 25) { return 7; }
				else if (dp.length < 30) { return 6; }
				else if (dp.length < 35) { return 5; }
				else if (dp.length < 40) { return 4; }
				else if (dp.length < 50) { return 3.5; }
				else if (dp.length < 60) { return 2.5; }
				else if (dp.length < 70) { return 2; }
				else if (dp.length < 80) { return 1.5; }
    		};
    		
    		var computeExpandedRadiusOnMouseOver = function(dp) {
    			return computeNormalRadius(dp) + 5;
    		};
    		
            var expandPlot = function(element, dp) {
            	d3.select(element)
					.transition()
					.duration(200)
					.attr("r", computeExpandedRadiusOnMouseOver(dp));
            };
            
            var shrinkPlot = function(element, dp) {
            	d3.select(element)
					.transition()
					.duration(500)
					.attr("r", computeNormalRadius(dp));
            };
            
            
            /**************************************************************************************
             * CTRL+CLICK HANDLING
             * Capture selected Test Runs for compare.
             */
            
        	scope.compareInfo = {
        		compareUrl: null,
        		left: null,
        		right: null,
        	};
        	
            var renderPlotAsSelected = function(element, d, dp) {
            	d3.select(element)
            		.style("fill", "Black")
            		.attr("r", computeExpandedRadiusOnMouseOver(dp));
//					.transition()
//					.duration(200)
//					.attr("r", computeExpandedRadiusOnMouseOver(dp));
            };
            
            var renderPlotAsUnselected = function(element, d, dp) {
            	d3.select(element)
            		.style("fill", function(d) { return (d.errors > 0 || d.failures > 0) ? "DarkRed": "Green"; })
					.transition()
					.duration(200)
					.attr("r", computeNormalRadius(dp));
            };
            
            var isSelectedforCompare = function(d) {
            	return (scope.compareInfo.left != null && scope.compareInfo.left.data.id == d.id ||
            			scope.compareInfo.right != null && scope.compareInfo.right.data.id == d.id);
            };
        	
            var handleCtrlClick = function(element, d, dp) {
            	/*
            	 * Register with Compare
            	 */
    			if (!scope.compareInfo.left) { 
    				scope.compareInfo.left = {};
    				scope.compareInfo.left.data = d;
    				scope.compareInfo.left.element = element;
    				renderPlotAsSelected(element, d, dp);
    			}
    			else if (scope.compareInfo.left && scope.compareInfo.left.data.id == d.id) { 
    				renderPlotAsUnselected(element, d, dp);
    				scope.compareInfo.left = null; 
    			}
    			else if (!scope.compareInfo.right) { 
    				scope.compareInfo.right = {};
    				scope.compareInfo.right.data  = d; 
    				scope.compareInfo.right.element = element;
    				renderPlotAsSelected(element, d, dp);
    			}
    			else if (scope.compareInfo.right && scope.compareInfo.right.data.id == d.id) { 
    				renderPlotAsUnselected(element, d, dp);    				
    				scope.compareInfo.right = null; 
    			}
            };
            
            /**************************************************************************************
             * TOOL-TIP Transition & RENDERING
             * TODO: Encapsulate in a dedicated directive.
             */
            /*
        	 * Set up a div to be used as the Tooltip.
        	 */
        	var toolTipDiv = d3.select(element[0])
        		.append("div")
        		.attr("class", "tooltip")
        		.style("opacity", 0);
        	
            var toolTipIn = function(d) {
    			toolTipDiv.transition()
					.duration(200)
					.style("opacity", .91);
    			
    			toolTipDiv
					.html(renderTooltip(d))
					.style("left", (d3.event.pageX + 10) + "px")
					.style("top", (d3.event.pageY - 28) + "px");
            }
            
            var toolTipOut = function() {
            	toolTipDiv
            		.transition() 
            		.duration(500) 
            		.style("opacity", 0);
            };
            
        	/*
        	 * Date Time format for Tooltip.
        	 */
            var toolTipFormatTime = d3.time.format("%e %B");
            /*
             * Builds HTML for Tooltip.
             */
            var renderTooltip = function(d) {
            	var runStyle = (d.errors == 0 && d.failures == 0) ? "style='color:Green'" : "";
            	var html = 	"<b>" + toolTipFormatTime(d.timestamp) + "</b><br/>" + d[5] + "<br/>" +
            				"<table>" + 
            				"	<tr " + runStyle + "><td><b># Tests Run: </b></td><td>" + d.testsRun +"</td></tr>";
            	if (d.skipped > 0) {
            		html = html + 
            				"	<tr><td><b># Skipped: </b></td><td>" + d.skipped +"</td></tr>"; 
            	}
            	if (d.errors > 0) {
            		html = html + 
            				"	<tr style='color:DarkRed'><td><b># Errors: </b></td><td>" + d.errors +"</td></tr>"; 
            	}
            	if (d.failures > 0) {
            		html = html + 
            				"	<tr style='color:DarkRed'><td><b># Failures: </b></td><td>" + d.failures +"</td></tr>"; 
            	}
            				
            	var html = html + "<table>"; 
            				
            	return html;
            };
            
            /**************************************************************************
            * COMPARE CONTEXT
            */
            
            var showCompareContext = function(d) {
                d3.event.preventDefault();
                $(".custom-menu")
            	.toggle(500)
            	.css({
            		top: d3.event.pageY  - 18 + "px",
            		left: d3.event.pageX + 15 + "px"
            	});
            };
            
            var hideCompareContext = function(event) {
            	$(".custom-menu").hide(100);
            };

            // Hide the tool-tip if we click somewhere else.
            $("#scatter-plot").on("mousedown", hideCompareContext);
            
        }//link
	}
}]);