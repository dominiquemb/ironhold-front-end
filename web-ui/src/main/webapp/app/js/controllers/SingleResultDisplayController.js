'use strict';

ironholdApp.controller('SingleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);
	
    $scope.showContainer = false;
    $scope.currentMessage = false;
    $scope.mode = 'text';
    $scope.loadTimestamp;
    $scope.hostname;
    $scope.imapSource;
    $scope.username;
    $scope.imapPort;
    $scope.protocol;
    $scope.folder;
    $scope.description;

    $scope.isModeActive = function(mode) {
	return ($state.current.url.indexOf(mode + "-mode") !== -1) ? true : false;
    }

    $rootScope.$on('modeData', function(evt, results) {
	var resultEntry;
	for (resultEntry in results) {
		$scope[resultEntry] = results[resultEntry];
	}
    });

    $rootScope.$on('mode', function(evt, mode) {
	$scope.$mode = mode;

	$scope.$emit('modeRequest', {
		mode: mode,
		messageId: $scope.currentMessage.formattedIndexedMailMessage.messageId,
		inputSearch: $scope.inputSearch
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
