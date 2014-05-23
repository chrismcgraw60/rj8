/**
 * Rendering a Test Result.
 */
juaApp.directive('latestResults', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        templateUrl: 'assets/scripts/directives/latestResultsGridTemplate.html',
        scope: true, // Prototypical parent scope inheritance.
        
        link: function(scope, element, attrs) {
        	
        	var retryCount = 0;
        	
        	var getLatestResult = function() {
        		/*
        		 * Timeout loop. We retry every 50ms until either data has appeared or we've reached
        		 * the retry limit (200).  
        		 */
        		if (retryCount >= 200) { 
        			/*
        			 * Will be roughly 10s. Too long for this application. 
        			 */
        			console.error("Reached retry limit : " + retryCount);
        			return; 
        		}
        		
        		if (!scope.rowDataResults || scope.rowDataResults.length == 0) {
        			$timeout(getLatestResult, 50);
        			retryCount ++;
        			return;
        		}
        		
        		/*
        		 * We have data, set the latest results on the scope.
        		 */
        		scope.recentResults = scope.rowDataResults.slice(0, 5);
        		
        		for(var i=0; i<5; i++) {
        			var nextLatestResult = scope.rowDataResults[i];
        			
        			nextLatestResult.rate = calculatePassRate(nextLatestResult);
        				
        			/*
        			 * Style the build ID and Error / Failure counts depending on whether there are errors / failures
        			 * in the results.
        			 */
        			var linkClass = ((nextLatestResult.errors + nextLatestResult.failures) === 0) ? "pass" : "fail";
        			var errClass = (nextLatestResult.errors === 0) ? "" : "fail";
        			var failClass = (nextLatestResult.failures === 0) ? "" : "fail";
        			
        			/*
        			 * Build up a table row for adding to the directive UI.
        			 */
        			var tr = document.createElement("tr");
        			$(tr).append("<td><a class='" + linkClass + "' href='#testResults/" + nextLatestResult.id + "'>" + nextLatestResult.file + "</a></td>");
        			$(tr).append("<td>" + nextLatestResult.timestamp.toISOString() + "</td>");
        			$(tr).append("<td>" + nextLatestResult.testsRun + "</td>");
        			$(tr).append("<td class='" + errClass + "'>" + nextLatestResult.errors + "</td>");
        			$(tr).append("<td class='" + failClass + "'>" + nextLatestResult.failures + "</td>");
        			$(tr).append("<td>" + nextLatestResult.skipped + "</td>");
        			$(tr).append("<td>" + nextLatestResult.rate + "</td>");
        			$(tr).append("<td>" + Math.round(nextLatestResult.time * 100) / 100 + "</td>");
        			
					$("#gridBody").append(tr);
        		}
        	};
        	
        	/*
        	 * Calculates percentage pass rate base on fail / error count.
        	 */
        	var calculatePassRate = function(nextLatestResult) {
        		var nonPass = (nextLatestResult.errors + nextLatestResult.failures);
    			var percentageNonPassed = (nonPass/nextLatestResult.testsRun) * 100; 
    			var percentageNonPassedRounded = Math.round(percentageNonPassed * 100) / 100;
    			var percentagePassed = (100 - percentageNonPassedRounded);
    			return percentagePassed;
        	};
        	
        	getLatestResult();
        	
        }//link:
	}
}]);