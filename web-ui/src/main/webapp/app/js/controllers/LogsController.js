'use strict';

ironholdApp.controller('LogsController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.tabName = 'logs';
});
