(function () {
   'use strict';

ironholdApp.controller('UsersController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.tabName = 'users';
    $scope.initialState = true;
});


}());