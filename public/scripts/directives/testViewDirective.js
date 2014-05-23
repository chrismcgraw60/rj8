/**
 * Rendering a Test.
 */
juaApp.directive('testView', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        templateUrl: 'assets/scripts/directives/testViewTemplate.html',
        scope: true, // Prototypical parent scope inheritance.
        
        link: function(scope, element, attrs) {
        	scope.getSelectedTest(function(testData) {
	    		scope.testInfo = testData;
	    		scope.testInfo.fqn = testData.className + "." + testData.methodName;		
	    		
	    		scope.getTestHistory(testData.className, testData.methodName, 
	    			function(hItem) {
	    				
	    				$("#historyView").append("<div>" + hItem.id + "</div>");
	    				$("#historyView").append("<div>" + hItem.status + "</div>");
	    				$("#historyView").append("<div>" + hItem.suiteId + "</div>");
	    				$("#historyView").append("<div>" + hItem.timeStamp + "</div>");
	    				$("#historyView").append("<hr/>");
	    				
			    	});//scope.getTestHistory
	    		
	    	});//scope.getSelectedTest
        }//link:
	}
}]);