
var analyseControllers = angular.module('analyseControllers', []);

analyseControllers.controller('AnalyseCtrl', ['$scope',

  function($scope) {

	$scope.analysisData = {rows: ["row1", "row2", "row3"] };
	
  }]);