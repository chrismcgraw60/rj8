/* Root Module for JUnit Analyser UI */

var juaApp = angular.module('juaApp', [
  'ngRoute',
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
      otherwise({
        redirectTo: '/analyse'
      });
  }]);