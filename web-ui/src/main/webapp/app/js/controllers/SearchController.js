(function () {
   'use strict';

ironholdApp.controller('SearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.tabName = 'search';
    $scope.facetAreaShort = true;
    $scope.limitFacets = true;
    $scope.initialState = true;
});



}());