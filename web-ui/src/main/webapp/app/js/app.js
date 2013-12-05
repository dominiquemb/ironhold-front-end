'use strict';
var ironholdApp = angular.module('ironholdApp', ['ngRoute','ngResource','ngSanitize','ui.bootstrap'])
    .config(function ($routeProvider, $locationProvider) {
        $routeProvider.when('/discovery',
            {
                templateUrl:'views/Discovery.html',
                controller: 'DiscoveryController'
            });
        $routeProvider.otherwise({redirectTo: '/discovery'});
        //$locationProvider.html5Mode(true);
     });

String.prototype.endsWith = function(suffix) {
    return this.toLowerCase().indexOf(suffix.toLowerCase(), this.length - suffix.length) !== -1;
};
