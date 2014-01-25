'use strict';

ironholdApp.controller('SingleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);
	
    $scope.showContainer = false;
    $scope.currentMessage = false;
    $scope.mode = 'text';

    $scope.isModeActive = function(mode) {
	return ($state.current.url.indexOf(mode + "-mode") !== -1) ? true : false;
    }

    $rootScope.$on('mode', function(evt, mode) {
	$scope.$mode = mode;
    });

    $scope.switchMode = function(newMode) {
        $scope.mode = newMode;
        $scope.$emit('mode', newMode);
    }

    $rootScope.$on('selectMessage', function(evt, message) {
	$scope.currentMessage = message;
    });

    $rootScope.$on('search', function() {
	$scope.showContainer = true;
    });

});
