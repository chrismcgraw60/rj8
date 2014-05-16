/**
 * Rendering a Test Result.
 */
juaApp.directive('testResult', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        templateUrl: 'assets/scripts/directives/testResultTemplate.html',
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
        		 * We have data, set the latest result on the scope.
        		 */
        		scope.latestResult = scope.rowDataResults[0];
        	};
        	
        	getLatestResult();
        	
        }//link:
	}
}]);