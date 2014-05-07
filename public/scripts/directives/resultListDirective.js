
juaApp.directive('resultList', function(){
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: 'assets/scripts/directives/resultListTemplate.html',
		link: function(scope, element, attrs) {
			scope.$on('RESULT_METADATA_ADDED', 
				function(event, metadata) {
					if (metadata) {
						var headerRow = document.createElement("tr");
						var columns = metadata.columns;
						for(var i=0; i<columns.length; i++) {
							var col = columns[i];
							$(headerRow).append("<td>" + col.name + "</td>");
						}
						$("#resultsGrid").append(headerRow);
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
						$("#resultsGrid").append(tr);
					}
				}
			);
			
			scope.$on('RESULT_INITIALISED', 
				function(event, data) {
					$("#resultsGrid").empty();
				}
			);
		}
	}
});