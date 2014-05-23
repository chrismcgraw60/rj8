/**
 * Angular Controller for Test Run View. Handles all of the data requirements when rendering a given
 * Test Run.  
 */
var testRunViewControllers = angular.module('testRunViewControllers', []);

testRunViewControllers.controller('TestRunViewCtrl', ['$scope', '$routeParams', 'adhocQuerySocketService',

  function($scope, $routeParams, adhocQuerySocketService) {
	
	$scope.currentTestRunId = $routeParams.id;
	
	/**
	 * Runs the query to find Errors / Fails and Skipped Tests for a given suite ID.  
	 */
	$scope.getNonPassesForSuite = function(dataRowHandler) {		
    	
		return adhocQuerySocketService.query({
			sql: "SELECT * FROM TESTENTRY WHERE STATUS != 'PASS' AND SUITE_ID = " + $scope.currentTestRunId,
			
			onRowData: function(testResultData) {
            	testResultData.id 			= +(testResultData[0]);
            	testResultData.uuid 		= testResultData[1];
            	testResultData.className 	= testResultData[2];
            	testResultData.methodName 	= testResultData[3];
            	testResultData.time	 		= testResultData[4];
            	testResultData.status 		= testResultData[5];
            	testResultData.exception 	= testResultData[6];
            	testResultData.message 		= testResultData[7];
            	testResultData.detail	 	= testResultData[8];
            	
				$scope.$apply( new function() {
					dataRowHandler(testResultData);
				});
			}
		});
	};
	
	/**
	 * Fetch a single test suite.  
	 */
	$scope.getTestRun = function(testRunLoadedHandler) {
		
    	/*
    	 * Calculates percentage pass rate base on fail / error count.
    	 */
    	var calculatePassRate = function(testRun) {
    		var nonPass = (testRun.errors + testRun.failures);
			var percentageNonPassed = (nonPass/testRun.testsRun) * 100; 
			var percentageNonPassedRounded = Math.round(percentageNonPassed * 100) / 100;
			var percentagePassed = (100 - percentageNonPassedRounded);
			return percentagePassed;
    	};
    	
    	/*
    	 * Date Format eg: 1986-12-26 09:29:29.848
    	 */
    	var parseDate = d3.time.format.utc("%Y-%m-%d %H:%M:%S.%L").parse;
    	
		return adhocQuerySocketService.query({
			sql: "SELECT * FROM TESTSUITE WHERE ID = " + $scope.currentTestRunId,
			
			onRowData: function(testRunData) {
				/*
				 * Read of the 1st row (should only be one) and
				 * set the data on the scope ($scope.currentTestRun).
				 * TODO: duplication. Need to consolidate this w Dashboard Ctrl.
				 */
				testRunData.id 			= +(testRunData[0]);
            	testRunData.uuid 		= testRunData[1];
            	testRunData.packageName = testRunData[2]
            	testRunData.className 	= testRunData[3];
            	testRunData.time	 	= testRunData[4];
            	testRunData.folder 		= testRunData[5];
            	testRunData.file 		= testRunData[6];
            	
            	testRunData.testsRun 	= +(testRunData[7]);
            	testRunData.failures 	= +(testRunData[8]);
            	testRunData.errors 		= +(testRunData[9]);
            	testRunData.skipped 	= +(testRunData[10]);
            	testRunData.timestamp 	= parseDate(testRunData[11]);
            	
            	testRunData.passing 	= (testRunData.testsRun - (testRunData.errors + testRunData.failures + testRunData.skipped));
            	
            	testRunData.rate = calculatePassRate(testRunData);
				
				$scope.$apply( new function() {
					testRunLoadedHandler(testRunData);
				});
			}
		});
	};
	
  }]);
