
juaApp.directive('compareTestsView', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        templateUrl: 'assets/scripts/directives/compareTestsViewTemplate.html',
        scope: true, // Prototypical parent scope inheritance.
        
        link: function(scope, element, attrs) {
        	
        	console.log("executing compareTestSuitesView.link .. ")
        	
        }//link:
	}
}]);