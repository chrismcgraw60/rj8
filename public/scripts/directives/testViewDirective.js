/**
 * Renders a specific Test Result at the pass / fail level. There are 3 components to this view:
 * 1. Test Details : Shows all of the detailed info regarding the fail (eg stack trace).
 * 2. Test History : Shows a Calendar based view of the recent results for the test.
 * 3. Test Compare : Shows a Launcher widget that will activate when a user Ctrl+Clicks a given result in the History 
 * View. When a user has selected 2 results, then the compare button will activate to allow the user to compare the 2
 * selected test results.
 */
juaApp.directive('testView', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        templateUrl: 'assets/scripts/directives/testViewTemplate.html',
        scope: true, // Prototypical parent scope inheritance.
        
        link: function(scope, element, attrs) {
        	
        	/*
        	 * Initialize the start date for the History View. The History will show the last
        	 * 100 days (roughly).
        	 */
        	var calStart = new Date();
        	calStart.setDate(calStart.getDate() - 100);
        	
        	/*
        	 * scope.timestampMap stores each test result against its timestamp. 
        	 * We do this because as we load the data into the Calendar, we lose
        	 * everything except the timestamp. When we subsequently handle a click 
        	 * event for a given result on the Calendar the event gives us the timestamp 
        	 * but we need the full data to be able to give a good rich hover or seed 
        	 * the Compare Launcher.
        	 * Because we track the timestamp against the Test Result in this map, we
        	 * can look up the original test data against the timestamp supplied by
        	 * the event callback and get to the data we need.    
        	 */
        	scope.timestampMap = {};
        	
        	/*
        	 * Set up a click handler that will be registered on the Calendar's click event.
        	 * We want to detect a ctrl+click and then populate the Compare launcher with the 
        	 * clicked result. 
        	 */
        	var calendarClickHandler = function(date, nb) {
        		if (d3.event.ctrlKey) {
        			var testData = scope.timestampMap[date.getTime()];
        			
        			scope.$apply(function() {
            			if (!scope.ctrlSelect1) { scope.ctrlSelect1 = testData; }
            			else if (scope.ctrlSelect1 && scope.ctrlSelect1 == testData) { scope.ctrlSelect1 = null; }
            			else if (!scope.ctrlSelect2) { scope.ctrlSelect2 = testData; }
            			else if (scope.ctrlSelect2 && scope.ctrlSelect2 == testData) { scope.ctrlSelect2 = null; }
        			});
        		}
        	};
    		
        	/*
        	 * Initialize the Calendar. This is a slightly modified version of the official code 
        	 * that customizes mouse-over behavior (actually currently cancels it).  
        	 */
    		scope.cal = new CalHeatMap();
        	scope.cal.init({
        		domain: "month",
        		subDomain: "day",
        		rowLimit: 1,
        		verticalOrientation: true,
        		subDomainTextFormat: "%d",
        		cellSize: 20,
        		cellRadius: 5,
        		label: {
        			position: "left",
        			offset: {x: 20, y: 12},
        			width: 110
        		},
        		displayLegend: false,
        		start: calStart,
        		range: 4,
        		legend: [2, 3, 4],
        		onClick: calendarClickHandler
        	});
        	
        	/*
        	 * Kick off the request to load the test details.
        	 */
        	scope.getSelectedTest(function(testData) {
	    		scope.testInfo = testData;
	    		scope.testInfo.fqn = testData.className + "." + testData.methodName;		
	    			    			    		
	    		/*
	    		 * Once we have loaded the test details, initiate the history request.
	    		 */
	    		scope.getTestHistory(testData.className, testData.methodName, 
	    			function(hItem) {
	    			
	    				/*
	    				 * Normalise the timestamp to 0 hours, mins, secs, ms.
	    				 * We need to consider multiple tests on the same day but this will
	    				 * do for 1st cut.
	    				 */
		    			var date = new Date(hItem.dt);
		    			date.setHours(0);
		    			date.setMinutes(0);
		    			date.setSeconds(0);
		    			date.setMilliseconds(0);
		            	var tStamp = new Date(date).getTime()/1000;
		            	tStamp = Math.floor(tStamp);
		            	
		            	/*
		            	 * We use arbitrary amounts to signify the various Test Status values
		            	 * above (fail, error, skipped).
		            	 */
		            	var s;
		            	if (hItem.status == "PASS") { s = 4; } 
		            	else if (hItem.status == "SKIPPED") { s = 3; } 
		            	else if (hItem.status == "FAIL" || hItem.status == "ERROR") { s = 1; }
		            	
		            	/*
		            	 * This is the format that the heat-map understands.
		            	 * <timestamp> : amount
		            	 */
		            	var data = {};	
		            	data[tStamp] = +s;
		            			            	
		            	/*
		            	 * Remember the timestamp we fed into the Calendar. This is
		            	 * the timestamp will be given back to us when the user clicks 
		            	 * on a test result and we'll use it to look up the original 
		            	 * data item. Uses case is compare.
		            	 */
		            	scope.timestampMap[date.getTime()] = hItem;
		            			            	
		            	scope.$apply(function() {
		            		scope.cal.update(data, false, scope.cal.APPEND_ON_UPDATE);
            			});
	    			}
	    		);//scope.getTestHistory
	    	});//scope.getSelectedTest
        }//link:
	}
}]);