
var manageControllers = angular.module('manageControllers', []);

manageControllers.controller('ManageCtrl', ['$scope', 'folderManagerService',

  function($scope, folderManagerService) {

	/*
	 * Wires up a submitted data handler callback to the folder feed provided by
	 * the folderManagerService (see folderServices.js).
	 */
	$scope.getFolderData = function(dataRowHandler) {
	
		return folderManagerService.folderFeed({
			
			onFolderEventData: function(folderData) {
				/*
				 * Type the ID as a number.
				 */
				folderData.id = +(folderData.id);
            	
				$scope.$apply( new function() {
					dataRowHandler(folderData);
				});
			}
		});
	};
	
  }]);
