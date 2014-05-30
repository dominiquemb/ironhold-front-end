(function () {
   'use strict';

ironholdApp.controller('FoldersController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.tabName = 'folders';
    $scope.modes[$scope.tabName] = 'text';
    $scope.showPreviewToolbar = false;
    $scope.initialState = true;
});


}());
