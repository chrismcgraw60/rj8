
juaApp.directive('manageFolders', function(){
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: 'assets/scripts/directives/manageFoldersTemplate.html',
		link: function(scope, element, attrs) {
			
			var folderInfoCache = {};
			
			var rowId = function (folderId) { return 'f_' + folderId; };
			var statusCellId = function (folderId) { return 's_' + rowId(folderId); };
			var updatedCellId = function (folderId) { return 'u_' + rowId(folderId); };
			
			
			var openFolderEventStream = function () {
				scope.getFolderData(function(folderDataRow) {
					
					if (!scope.rootFolder) {
						scope.rootFolder = folderDataRow;
					}
					else {
						if (folderInfoCache[folderDataRow.id]) {
							var tr = folderInfoCache[folderDataRow.id];
							$("#" + statusCellId(folderDataRow.id)).html(folderDataRow.status);
							$("#" + updatedCellId(folderDataRow.id)).html(folderDataRow.updated);
						}
						else {
				    		var tr = document.createElement("tr");
				    		$(tr).attr('id', rowId(folderDataRow.id));
				    		$("#folderGridBody").append(tr);
				    		$(tr).append("<td>" + folderDataRow.path.replace(scope.rootFolder.path, "-") + "</td>");
				    		$(tr).append("<td id ='"  + statusCellId(folderDataRow.id) + "'>" + folderDataRow.status + "</td>");
				    		$(tr).append("<td>" + folderDataRow.created + "</td>");
				    		$(tr).append("<td id ='"  + updatedCellId(folderDataRow.id) + "'>" + folderDataRow.updated + "</td>");
				    		folderInfoCache[folderDataRow.id] = tr;
						}
					}
		    		
		    	});//scope.getFolderData
			};
			
			openFolderEventStream();
			
		}//link
	}
});