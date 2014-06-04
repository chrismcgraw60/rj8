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
		return adhocQuerySocketService.query({
			sql: sql,
			
			onMetadata: function(metadata) {
				$scope.metadata = metadata;
			},
				
			onRowData: function(rowData) {
				$scope.rowDataResults.push({name: rowData[0], score: rowData[1]});
			}
		});
	};
	
	/**
	 * Runs the query to feed the main dashboard summary graph.
	 * Enhances the standard response rowdata to make the data
	 * easier to work with.  
	 */
	$scope.doSummaryQuery = function(range) {
		return adhocQuerySocketService.query({
			sql: "SELECT TOP " + range + " * FROM TESTSUITE ORDER BY TIMESTAMP DESC",
						
			onRowData: function(rowData) {
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
            	rowData.packageName = rowData[2]
            	rowData.className 	= rowData[3];
            	rowData.time	 	= rowData[4];
            	rowData.folder 		= rowData[5];
            	rowData.file 		= rowData[6];
            	
            	rowData.testsRun 	= +(rowData[7]);
            	rowData.failures 	= +(rowData[8]);
            	rowData.errors 		= +(rowData[9]);
            	rowData.skipped 	= +(rowData[10]);
            	rowData.timestamp 	= parseDate(rowData[11]);
            	
            	rowData.passing 	= (rowData.testsRun - (rowData.errors + rowData.failures + rowData.skipped));
				
            	$scope.rowDataResults.push(rowData);
            	if ($scope.rowDataResults.length == range) {
            		/*
            		 * The controlled buffers the results and does not call the handler
            		 * until they are fully loaded. We experimented with batch updates 
            		 * but the UX is jarring. If we had smoother transitions in the UX
            		 * then a batch update strategy could be re-implemented.
            		 * (basically a modulo operation).
            		 */
					$scope.$apply( new function() {
						$scope.summaryUpdateHandler($scope.rowDataResults);
					});
            	}
			}
		});//adhocQuerySocketService.query
	};//$scope.doSummaryQuery
  }]);
