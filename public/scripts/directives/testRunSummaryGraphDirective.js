
juaApp.directive('testRunSummaryGraph', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        
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
        	var svg = d3.select(element[0])
        		.append("svg")
        		.attr("width", width + margin.left + margin.right)
        		.attr("height", height + margin.top + margin.bottom)
        		.append("g")
        		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        	
            /*
        	 * Set up a div to be used as the Tooltip.
        	 */
        	var toolTipDiv = d3.select(element[0])
        		.append("div")
        		.attr("class", "tooltip")
        		.style("opacity", 0);
        	        	
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
        	 * Update the graph using the Test Result data on the scope. This data may still be
        	 * in the process of being populated and so this function periodically invokes 
        	 * $timeout until such time that it can determine no more data will arriving. 
        	 */
        	var update = function() {
        		
            	resultCountOnLastUpdate = scope.rowDataResults.length;
            	
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
        		x.domain(d3.extent(scope.rowDataResults, function(d) { return d.timestamp; }));
        		var yMax = d3.max(scope.rowDataResults, function(d) { return d.testsRun; });
        		var yMin = d3.min(scope.rowDataResults, function(d) { return d.passing; });
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
        		
        		console.log(scope.rowDataResults);
        		
        		/*
        		 * Plot line of #tests run by date.
        		 */
        		svg.append("path")
        			.attr("d", valueline(scope.rowDataResults));
        		
        		/*
        		 * Plot scatter of #tests run by date.
        		 * These will follow the #tests line drawn above.
        		 * Each point shows a tooltip on mouse-over.
        		 */
        		if (scope.rowDataResults.length <= 50) {
            		svg.selectAll("dot")
            			.data(scope.rowDataResults)
            		.enter().append("circle")
            			.attr("r", 3.5)
            			.attr("cx", function(d) { return x(d.timestamp); })
            			.attr("cy", function(d) { return y(d.testsRun); })
            			.style("fill", function(d) { return (d.errors > 0 || d.failures > 0) ? "DarkRed": "Green"; })
                		.on("mouseover", function(d) {
                			/*
                			 * When we  mouse over a scatter plot circle, we expand it out and show the tooltip.
                			 */
                			d3.select(this)
            					.transition()
            					.duration(200)
            					.attr("r", 10);
                			toolTipDiv.transition()
                				.duration(200)
                				.style("opacity", .91);
                			toolTipDiv
                				.html(renderTooltip(d))
                				.style("left", (d3.event.pageX) + "px")
                				.style("top", (d3.event.pageY - 28) + "px");
                		})
                		.on("mouseout", function(d) {
                			/*
                			 * Shrink the scatter plot circle to its original size 
                			 * and hide the tool-tip.
                			 */
                			d3.select(this)
                				.transition()
                				.duration(500)
                				.attr("r", 3.5);
                			toolTipDiv.transition()
                				.duration(500)
                				.style("opacity", 0);
                		})
                		.on("click", function(d) {
                			/*
                			 * Navigate to the results page for the clicked test result.
                			 */
                			scope.$apply(function() {
                				$location.path("/testResults/" + d.id);
                			});
                		});
        		}
        	
        		/*
        		 * Plot line #test passes on given date.
        		 * If there are any fails / errors / skipped then this will
        		 * bump along under the #tests line (which should only change
        		 * gradually).
        		 */
        		svg.append("path")
        			.attr("class", "pass-path")
        			.attr("d", passingLine(scope.rowDataResults));
        		
        		/*
        		 * Reset the timer for next UI update.
        		 * See comment for var resultCountOnLastUpdate.
        		 */
        		if (scope.isLoadingData || resultCountOnLastUpdate < scope.rowDataResults.length) {
        			$timeout(update, 50);
        		}	
            		                		
            };
            
        	/*
        	 * Browser on-resize event.
        	 */ 
            window.onresize = function() { scope.$apply(); };
            /*
             * Request the Test Result data.
             */
            scope.doSummaryQuery();
            /*
             * Schedule the 1st UI update.
             */
            $timeout(update, 50);
            
            /**************************************************************************************
             * TOOL-TIP RENDERING
             * TODO: Encapsulate in a dedicated directive.
             */
            
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
        }
	}
}]);