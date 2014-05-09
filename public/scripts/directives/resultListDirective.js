
juaApp.directive('resultList', function(){
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: 'assets/scripts/directives/resultListTemplate.html',
		link: function(scope, element, attrs) {
			scope.$on('RESULT_METADATA_ADDED', 
				function(event, metadata) {
					if (metadata) {
						var tr = document.createElement("tr");
						var columns = metadata.columns;
						for(var i=0; i<columns.length; i++) {
							var col = columns[i];
							$(tr).append("<th><h4>" + col.name + "</h4></th>");
						}
						$("#gridHeader").append(tr);
					}
				}
			);
			
			scope.$on('RESULT_ADDED', 
				function(event, rowData) { 
					if (rowData) {
						var tr = document.createElement("tr");
						for(var i=0; i<rowData.length; i++) {
							$(tr).append("<td>" + rowData[i] + "</td>");
						}
						$("#gridBody").append(tr);
					}
				}
			);
			
			scope.$on('RESULT_INITIALISED', 
				function(event, data) {
					$("#gridHeader").empty();
					$("#gridBody").empty();
				}
			);
		}
	}
});