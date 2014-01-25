'use strict';

ironholdApp.controller('SingleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
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

    $rootScope.$on('mode', function(evt, mode) {
	$scope.$mode = mode;

	if (mode === 'source') {
                messagesService
                        .one(logInService.getClientKey())
                        .one($scope.currentMessage.formattedIndexedMailMessage.messageId)
                        .one("sources")
                        .get({criteria: $scope.inputSearch})
                        .then(function(result) {
                        	$scope.loadTimestamp = result.payload[0].loadTimestamp;
				$scope.hostname = result.payload[0].hostname;
			  	$scope.imapSource = result.payload[0].imapSource;
				$scope.username = result.payload[0].username;
				$scope.imapPort = result.payload[0].port;
				$scope.protocol = result.payload[0].protocol;
				$scope.folder = result.payload[0].folder;
				$scope.description = result.payload[0].description;
                        });
	}
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
