'use strict';

ironholdApp.controller('ResultsDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.mode = 'text';
    $scope.showSearchResults = false;
    $scope.showMessage = false;
    $scope.currentPage = 1;
    $scope.pageSize = 10;
    $scope.showMessage = false;
    $scope.messages = [];
    $scope.matches;

    $rootScope.$on('results', function(evt, args) {
	$scope.messages = args.resultEntries;
        $scope.matches = args.matches;
	if ($scope.matches > 0) {
		$scope.showSearchResults = true;
	}
    });

    $scope.switchMode = function(newMode) {
        $scope.mode = newMode;
    }

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

        $scope.updateSearch();
    }

    $scope.selectMessage = function(message) {
        $scope.unselectAllMessages();
        message.selected = true;
        messagesService.one("demo").one(message.formattedIndexedMailMessage.messageId).get({criteria: $scope.inputSearch}).then(function(result) {
            $scope.currentMessage = result.payload.messages[0];
            $scope.showMessage = true;
            $scope.mode = 'text';
        });
    }

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
        $scope.showSearchResults = false;
        $scope.showMessage = false;
        $scope.searchMessages = 0;
        $scope.searchTime = 0;
	$scope.matches = [];
        $scope.messages = [];
        $scope.suggestions = [];
        $scope.currentPage = 1;
    }

    $scope.goTo = function(page) {
        if (page > 0) {
            $scope.currentPage = page;
            $scope.updateSearch();
        }
    }
});
