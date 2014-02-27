'use strict';

ironholdApp.controller('MultipleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.mode = 'text';
    $scope.currentPage = 1;
    $scope.messages = [];
    $scope.matches;
    $scope.showSearchResults;
    $scope.inputSearch;

    $rootScope.$on('mode', function(evt, mode) {
	$scope.mode = mode;
    });

    $rootScope.$on('results', function(evt, args) {
	$scope.messages = args.resultEntries;
        $scope.matches = args.matches;
	if ($scope.matches > 0) {
		$scope.showSearchResults = true;
		$scope.$emit('showSearchResults', true);
	}
    });

    $rootScope.$on('search', function(evt, args) {
	$scope.inputSearch = args.inputSearch;
    });

    $scope.hasAttachmentHighlight = function(message) {
        return message.attachmentWithHighlights !== undefined;
    }

    $scope.hasAttachment = function(message) {
        return message.formattedIndexedMailMessage.attachments.length > 0;
    }

    $scope.isMessageTypeEqualTo = function(message, type) {
        return message.formattedIndexedMailMessage.messageType == type;
    }

    $scope.isImportanceEqualTo = function(message, importance) {
        return message.formattedIndexedMailMessage.importance == importance;
    }


    $scope.unselectAllMessages = function() {
        angular.forEach($scope.messages, function(message) {
            message.selected = false;
        });
/*
	$scope.$emit('updateSearch', {
		inputSearch: $scope.inputSearch
	});
*/
    }

    $scope.selectMessage = function(message) {
        $scope.unselectAllMessages();
        message.selected = true;
	
	$scope.$emit('selectResultRequest', message, $scope.inputSearch);
    }

    $rootScope.$on('selectResultData', function(evt, result) {
	    $scope.currentMessage = result.payload.messages[0];
    });

    $scope.unhilightAllMessages = function(message) {
        angular.forEach($scope.messages, function(entry) {
            $scope.unhighlightMessage(entry);
        });
    }

    $scope.highlightAllMessages = function(message) {
        angular.forEach($scope.messages, function(entry) {
            $scope.highlightMessage(entry);
        });
    }

    $scope.highlightMessage = function(message) {
	    message.highlighted = true;
    }

    $scope.unhilightMessage = function(message) {
	    message.highlighted = false;
    }

    $scope.reset = function () {
	$scope.showSearchResults = true;
        $scope.searchMessages = 0;
	$scope.matches = [];
        $scope.messages = [];
        $scope.currentPage = 1;
    }

    $rootScope.$on('pageChange', function(evt, page) {
	$scope.currentPage = page;
    });
});
