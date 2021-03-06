/* Root Module for JUnit Analyser UI */

var juaApp = angular.module('juaApp', [
  'ngRoute',
  'querySocketServices',
  'folderServices',
  'dashboardControllers',
  'analyseControllers',
  'manageControllers',
  'testRunViewControllers',
  'testViewControllers'
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
      when('/testResults/:id', {
    	templateUrl: 'assets/partials/testRunView.html',
        controller: 'TestRunViewCtrl'
      }).
      when('/tests/:id', {
    	  templateUrl: 'assets/partials/testView.html',
          controller: 'TestViewCtrl'
        }).
      when('/compareTests', {
    	  templateUrl: 'assets/partials/compareTestsView.html',
          controller: 'TestViewCtrl'
        }).
      when('/compareSuites', {
    	  templateUrl: 'assets/partials/compareTestSuitesView.html',
          controller: 'DashboardCtrl'
        }).
      otherwise({
        redirectTo: '/dashboard'
      });
  }]);