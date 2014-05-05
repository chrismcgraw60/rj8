
juaApp.directive('resultList', function(){
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: 'assets/scripts/directives/resultListTemplate.html',
		link: function(scope, element, attrs) {
			scope.$on('RESULT_ADDED', 
				function(event, data) { 
					/*
					 * 
0: "13921"
1: "60ad8f03-ff85-435b-a107-6d59cfd61317"
2: "com.ibm.rdm.client.api.tests.LicenseAvailabilityTest"
3: "testLicenseAvailability"
4: "0.374"
5: "10"
					 */
					if (data.rows) {
						console.log("HANDLED: " + data.rows[0]); 
						var tag = '<div>' + data.rows[0] + ' | ' + data.rows[2] + ' | ' + data.rows[3] + '</div>';
					    element.append(tag);
					}
				});
		}
	}
});