/**
 * Angular Controller for Test View. Handles all of the data requests for rendering a single Test.
 * In addition to the test details (status, exception stack etc), we also render the historical
 * status data for the test. 
 */
var testViewControllers = angular.module('testViewControllers', []);

testViewControllers.controller('TestViewCtrl', ['$scope', '$routeParams', 'adhocQuerySocketService',
    
  function($scope, $routeParams, adhocQuerySocketService) {
	
	$scope.currentTestRunId = $routeParams.id;
	
	/**
	 * Fetch a single Test.
	 */
	$scope.getSelectedTest = function(dataRowHandler) {		
    	
		return adhocQuerySocketService.query({
			sql: "SELECT * FROM TESTENTRY WHERE ID = " + $scope.currentTestRunId,
			
			onRowData: function(testData) {
            	testData.id 		= +(testData[0]);
            	testData.uuid 		= testData[1];
            	testData.className 	= testData[2];
            	testData.methodName = testData[3];
            	testData.time	 	= testData[4];
            	testData.status 	= testData[5];
            	testData.exception 	= testData[6];
            	testData.message 	= testData[7];
            	testData.detail	 	= testData[8];
            	
				$scope.$apply( new function() {
					dataRowHandler(testData);
				});
			}
		});
	};
	
	/**
	 * Fetch historical status data for a given test class and method name. 
	 */
	$scope.getTestHistory = function(className, methodName, testRunLoadedHandler) {	   	
	   	/*
	   	 * Date Format eg: 1986-12-26 09:29:29.848
	   	 */
	   	var parseDate = d3.time.format.utc("%Y-%m-%d %H:%M:%S.%L").parse;
   	
		return adhocQuerySocketService.query({
			sql: "SELECT TOP 100 TESTENTRY.ID, TESTENTRY.STATUS, TESTENTRY.SUITE_ID, TESTSUITE.TIMESTAMP " +  
				 "FROM TESTENTRY INNER JOIN TESTSUITE ON TESTENTRY.SUITE_ID = TESTSUITE.ID " +  
				 "WHERE TESTENTRY.CLASSNAME = '" + className + "' AND TESTENTRY.METHODNAME = '" + methodName + "' " + 
				 "ORDER BY TIMESTAMP DESC",
			
			onRowData: function(testRunData) {
				
				testRunData.id 			= +(testRunData[0]);
            	testRunData.status 		= testRunData[1];
            	testRunData.suiteId   	= testRunData[2]
            	testRunData.timeStamp	= parseDate(testRunData[3]);
            	testRunData.dt			= testRunData[3];
			
				$scope.$apply( new function() {
					testRunLoadedHandler(testRunData);
				});
			}
		});
	};
	
  }]);
