/**
 * Angular Controller for Dashboard View. 
 */
var dashboardControllers = angular.module('dashboardControllers', []);

dashboardControllers.controller('DashboardCtrl', ['$scope', 'adhocQuerySocketService',

  function($scope, adhocQuerySocketService) {
	
	/**
	 * Holds the returned row data for consumption by callers (via rowdataHandlerCallback).
	 */
	$scope.rowDataResults = [];
	
	/**
	 * Proto-type to test out d3 and works with testCountsDrective.
	 * Keep it around for reference for time being.
	 */
	$scope.doQuery = function(sql) {
		return adhocQuerySocketService.query(
			sql,
			
			function() {
				/* no-op */
			},
			
			function() {
				/* no-op */
			},
			
			function(metadata) {
				$scope.metadata = metadata;
			},
				
			function(rowData) {
				$scope.rowDataResults.push({name: rowData[0], score: rowData[1]});
			}
		);
	};
	
	/**
	 * Runs the query to feed the main dashboard summary graph.
	 * Enhances the standard response rowdata to make the data
	 * easier to work witj.  
	 */
	$scope.doSummaryQuery = function() {
		return adhocQuerySocketService.query(
			"SELECT TOP 50 * FROM TESTSUITE ORDER BY TIMESTAMP DESC",
			
			/*
			 * Socket Opened Handler
			 */
			function() {
				$scope.isLoadingData = true;
			},
			/*
			 * Socket closed Handler
			 */
			function() {
				$scope.isLoadingData = false;
			},
			/*
			 * Metadata Handler.
			 */
			function(metadata) {
				$scope.metadata = metadata;
			},
			/*
			 * Rowdata Handler.
			 */
			function(rowData) {
				/*
            	 * Date Format eg: 1986-12-26 09:29:29.848
            	 */
            	var parseDate = d3.time.format.utc("%Y-%m-%d %H:%M:%S.%L").parse;
            	
            	/*
            	 * Extract data from array and set as named fields. 
            	 * Makes it easier to work with client side.
            	 * TODO: Could turn this into a smarter generic parser. Is currently a 
            	 * bit wasteful as data is duplicated in array and field.
            	 */
            	
            	rowData.id 			= +(rowData[0]);
            	rowData.uuid 		= rowData[1];
            	rowData.className 	= rowData[2];
            	rowData.time	 	= rowData[3];
            	rowData.folder 		= rowData[4];
            	rowData.file 		= rowData[5];
            	
            	rowData.testsRun 	= +(rowData[6]);
            	rowData.errors 		= +(rowData[7]);
            	rowData.failures 	= +(rowData[8]);
            	rowData.skipped 	= +(rowData[9]);
            	rowData.timestamp 	= parseDate(rowData[10]);
            	
            	rowData.passing 	= (rowData.testsRun - (rowData.errors + rowData.failures + rowData.skipped))
            	
//				rowData[10] = parseDate(rowData[10]);
//				rowData[6] = +(rowData[6]);
				
				$scope.$apply( new function() {
					$scope.rowDataResults.push(rowData);
				});
			}
		);
	};
	
  }]);
