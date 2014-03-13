'use strict';

ironholdApp.controller('SettingsController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.tabName = 'settings';
    $scope.initialState = true;
});
