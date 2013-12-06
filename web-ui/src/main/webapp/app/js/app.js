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

ironholdApp.factory('searchResultsService', function ($rootScope) {
    var sharedService = { };

    sharedService.searchMatches = 0;
    sharedService.searchTime = 0;

    sharedService.prepForBroadcast = function(searchMatches, searchTime) {
        this.searchMatches = searchMatches;
        this.searchTime = searchTime;
        this.broadcastItem();
    }

    sharedService.broadcastItem = function() {
        $rootScope.$broadcast('handleSearchResultsChange');
    }

    return sharedService;
});

String.prototype.endsWith = function(suffix) {
    return this.toLowerCase().indexOf(suffix.toLowerCase(), this.length - suffix.length) !== -1;
};
