/* Root Module for JUnit Analyser UI */

var juaApp = angular.module('juaApp', [
  'ngRoute',
  'querySocketServices',
  'dashboardControllers',
  'analyseControllers',
  'manageControllers'
]);

juaApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/analyse', {
        templateUrl: 'assets/partials/analyse.html',
        controller: 'AnalyseCtrl'
      }).
      when('/manage', {
        templateUrl: 'assets/partials/manage.html',
        controller: 'ManageCtrl'
      }).
      when('/dashboard', {
    	  templateUrl: 'assets/partials/dashboard.html',
          controller: 'DashboardCtrl'
        }).
      when('/testResults', {
    	templateUrl: 'assets/partials/testSuiteResults.html',
        controller: 'DashboardCtrl'
      }).
      otherwise({
        redirectTo: '/dashboard'
      });
  }]);