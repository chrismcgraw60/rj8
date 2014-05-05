
var manageControllers = angular.module('manageControllers', []);

manageControllers.controller('ManageCtrl', ['$scope',

  function($scope) {

	$scope.manageData = {reports: ["r1", "r2", "r3"] };
	
  }]);
