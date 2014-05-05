
var analyseControllers = angular.module('analyseControllers', []);

analyseControllers.controller('AnalyseCtrl', ['$scope',

  function($scope) {

	$scope.query='SELECT * FROM TESTENTRY';
	
	$scope.analysisData = [];
	
	$scope.ws = new WebSocket("ws://localhost:9000/query");
	
	$scope.buffer = [];
	$scope.messageCount = 0;
	
	$scope.ws.onopen = function() {
		console.log("ws connection open.");
//		$scope.analysisData = [];
//		$scope.analysisData.push("test");
		
		$scope.ws.onclose = function() {
			console.log("ws connection closed.");
		}
		
		$scope.ws.onerror = function(error) {
			console.log("WS ERRROR: " + error);
		}

		$scope.ws.onmessage = function(message) {
//			console.log(message.data);
			var data = angular.fromJson(message.data);
			console.log(data);
			$scope.$emit("RESULT_ADDED", data);
//			$scope.analysisData.push(data);
//			$scope.$apply();			
		};
	};
	
	$scope.runQuery = function(){
		$scope.analysisData = [];
		$scope.analysisData.push("test");
//		
//		console.log("Running Query..." + $scope.query);
//		
//		$scope.ws = new WebSocket("ws://localhost:9000/query");
//		
//		$scope.ws.onopen = function() {
//			console.log("ws connection open.");
//			$scope.analysisData = [];
//			$scope.analysisData.push("test");
//			
//			$scope.ws.onclose = function() {
//				console.log("ws connection closed.");
//			}
//			
//			$scope.ws.onerror = function(error) {
//				console.log("WS ERRROR: " + error);
//			}
//
//			$scope.ws.onmessage = function(message) {
//				$scope.analysisData.push(message.data);
////				console.log(message.data);
//			};
			$scope.ws.send($scope.query);
//	}
		
		
	};
	
  }]);