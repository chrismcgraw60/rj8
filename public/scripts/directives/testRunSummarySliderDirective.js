/**
 * Encapsulates presentation for a slider control that lets the user control how much historical
 * test run is loaded in a range from 0-500 test runs.
 * 
 * There is currently no sliding window, the range always starts of the most recent and goes back 
 * in time.
 */
juaApp.directive('testRunSummarySlider', ['$window', '$timeout', '$location', function($window, $timeout, $location) {
	return {
        restrict: 'EA',
        transclude: true,
        template: '<div id="rangeSlider" style="height:240px; width:16px"></div>',
        
        link: function(scope, element, attrs) {
        	
        	/*
        	 * Set up the slider tool-tip via its serialization object.
        	 */
        	var sliderTooltipInfo = {
    			target: '-tooltip-<div class="slider-tooltip"></div>',
    			method: function (value) {
    				$(this).html('<span><b>' + value + '</b> most recent tests.</span>');
    			}
		    };
        	
        	/*
        	 * Set up a slider to control the date range.
        	 * The slider will step in increments of 5 over the range 
        	 * where data points are most interesting and it allocates 
        	 * most of the slider (75%) to this range.
        	 * The remaining range (25%) is used to allow the user to 
        	 * request historical data up to a max of 500 most recent 
        	 * test runs.
        	 */
        	$("#rangeSlider").noUiSlider({
        		start: 100,
        		orientation: "vertical",
        		step: 5,
        		range: {
        			"min": 5,
        			"75%": 75,
        			"max": 500
        		},
        		serialization: {
        			format: { decimals: 0 },
        			lower: [ $.Link(sliderTooltipInfo) ]
        		}
        	});
        	
        	/*
             * When the slider is moved, reset the Test Result data and 
             * do a fresh load, using the slider value as the range.
             */
        	$("#rangeSlider").on('set', function() {
        		scope.rowDataResults = [];
    			scope.$apply(function() {
    				scope.doSummaryQuery($("#rangeSlider").val());
    			});
        	});
        	
        	/*
        	 * Initialize the slider value and trigger the initial data load.
        	 */
        	$("#rangeSlider").val(25);
        	scope.doSummaryQuery(25);
            
        }//link
	}
}]);