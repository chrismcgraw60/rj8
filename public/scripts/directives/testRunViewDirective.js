/**
 * Rendering a Test Result.
 */
juaApp.directive('testRunView', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        templateUrl: 'assets/scripts/directives/testRunViewTemplate.html',
        scope: true, // Prototypical parent scope inheritance.
        
        link: function(scope, element, attrs) {
	    	
	    	scope.getTestRun(function(testRun) {
    			/*
    			 * Style the build ID and Error / Failure counts depending on whether there are errors / failures
    			 * in the results.
    			 */
    			var linkClass = ((testRun.errors + testRun.failures) === 0) ? "pass" : "fail";
    			var errClass = (testRun.errors === 0) ? "" : "fail";
    			var failClass = (testRun.failures === 0) ? "" : "fail";
    			
    			/*
    			 * If any of our counts are zero, use a '-' instead of printing the zero.
    			 * It reads a little better.
    			 */
    			var formatCount = function(c) { return (c != 0) ? c : "---"; }
    			
    			/*
    			 * Build up a table row for adding to the directive UI.
    			 */
    			var tr = document.createElement("tr");
    			$(tr).append("<td><a class='" + linkClass + "' href='#testResults/" + testRun.id + "'>" + testRun.file + "</a></td>");
    			$(tr).append("<td>" + testRun.timestamp.toISOString() + "</td>");
    			$(tr).append("<td>" + formatCount(testRun.testsRun) + "</td>");
    			$(tr).append("<td class='" + errClass + "'>" + formatCount(testRun.errors) + "</td>");
    			$(tr).append("<td class='" + failClass + "'>" + formatCount(testRun.failures) + "</td>");
    			$(tr).append("<td>" + formatCount(testRun.skipped) + "</td>");
    			$(tr).append("<td>" + testRun.rate + "</td>");
    			$(tr).append("<td>" + Math.round(testRun.time * 100) / 100 + "</td>");
    			
				$("#testRunGridBody").append(tr);
	    	});
	    	
	    	/*
	    	 * Map status code to the grid DOM nodes where the result entry
	    	 * should be placed.
	    	 */
        	var statusGridMap = {
            		"ERROR" 	: {grid: $("#errorsGrid"), body: $("#errorsGridBody")},
            		"FAIL" 		: {grid: $("#failsGrid"), body: $("#failsGridBody")},
            		"SKIPPED" 	: {grid: $("#skippedGrid"), body: $("#skippedGridBody")}
            	};
	    	
	    	scope.getNonPassesForSuite(function(dataRow) {
	    		var tr = document.createElement("tr");
	    		
	    		var fqn = dataRow.className + "." + dataRow.methodName;
    			$(tr).append("<td><a href='#tests/" + dataRow.id + "'>" + fqn + "</a></td>");
    			
    			if (dataRow.status == "ERROR" || dataRow.status == "FAIL") {
    				$(tr).append("<td>" + dataRow.exception + "</td>");
    				$(tr).append("<td>" + dataRow.message + "</td>");
    				$(tr).append("<td>" + Math.round(dataRow.time * 100) / 100 + "</td>");
    			}
    			
    			/*
    			 * All grids are hidden by default when the directive initializes.
    			 * If the grid is hidden for the data type (error / fail / skipped), show it now.
    			 */
    			var grid = statusGridMap[dataRow.status].grid;
    			if (!grid.is(":visible")) {
    				grid.css("display", "");
    			}
    			/*
    			 * Add the row to the grid (which should no wbe visible).
    			 */
    			statusGridMap[dataRow.status].body.append(tr);
	    	});//scope.getNonPassesForSuite
	    	
        }//link:
	}
}]);