
var analyseControllers = angular.module('analyseControllers', []);

analyseControllers.controller('AnalyseCtrl', ['$scope', '$location',

  function($scope, $location) {

	$scope.query='SELECT * FROM TESTENTRY';
	
	$scope.analysisData = [];
	
	$scope.ws = new WebSocket("ws://" + $location.host() + ":" + $location.port() + "/query");
	
	$scope.ws.onopen = function() {
		console.log("ws connection open.");
		
		$scope.ws.onclose = function() {
			console.log("ws connection closed.");
		}
		
		$scope.ws.onerror = function(error) {
			console.log("WS ERRROR: " + error);
		}

		$scope.ws.onmessage = function(message) {
			var data = angular.fromJson(message.data);
			
			if (data.metadata) {
				$scope.$emit("RESULT_METADATA_ADDED", data.metadata);
			}
			else if (data.row) {
				$scope.$emit("RESULT_ADDED", data.row);
			}
			else {
				throw "Unexpected Server Data from Query Result :" + data;
			}
		};
	};
	
	$scope.runQuery = function(){
		$scope.$emit("RESULT_INITIALISED", null);	
		$scope.ws.send($scope.query);
	};
	
  }]);