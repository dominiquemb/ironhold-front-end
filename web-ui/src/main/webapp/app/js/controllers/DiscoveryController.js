(function () {
   'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.tabName = 'discovery';
    $scope.showPreviewToolbar = false;
    $scope.modes[$scope.tabName] = 'text';
    $scope.initialState = true;
});

}());
