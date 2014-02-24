'use strict';

ironholdApp.controller('SingleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);
	
    $scope.showContainer = false;
    $scope.currentMessage = false;
    $scope.mode = 'text';
    $scope.modeData = {};

    $scope.isModeActive = function(mode) {
	return ($state.current.url.indexOf(mode + "-mode") !== -1) ? true : false;
    }

    $rootScope.$on('modeData', function(evt, results) {
	$scope.modeData[results.mode] = results.payload[0];
    });

    $rootScope.$on('mode', function(evt, mode) {
	$scope.$mode = mode;

	if (mode === 'text') {
		mode = '';
	}

	$scope.$emit('modeRequest', {
		mode: mode,
		messageId: $scope.currentMessage.formattedIndexedMailMessage.messageId,
		criteria: {'criteria': $scope.inputSearch}
	});
    });

    $scope.switchMode = function(newMode) {
        $scope.$emit('mode', newMode);
    }

    $rootScope.$on('selectMessage', function(evt, message) {
	$scope.currentMessage = message;
    });

    $rootScope.$on('search', function() {
	$scope.showContainer = true;
    });

});
