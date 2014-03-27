'use strict';

ironholdApp.controller('MultipleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.mode = 'text';
    $scope.currentPage = 1;
    $scope.messages = [];
    $scope.matches;
    $scope.showSearchResults;
    $scope.inputSearch;
    $scope.highlightActive = false;

    $rootScope.$on('highlightActive', function(evt, offOrOn) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.highlightActive = offOrOn;
	}
    });
 
    $rootScope.$on('mode', function(evt, mode) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.mode = mode;
	}
    });

    $rootScope.$on('results', function(evt, args) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.messages = args.resultEntries;

		// First message selected by default
		$scope.selectMessage($scope.messages[0]);

		$scope.matches = args.matches;
		if ($scope.matches > 0) {
			$scope.showSearchResults = true;
			$scope.$emit('showSearchResults', true);
		}
	}
    });

    $rootScope.$on('search', function(evt, args) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.inputSearch = args.inputSearch;
	}
    });

    $scope.hasAttachment = function(message) {
	if ($scope.activeTab === $scope.tabName) {
		return message.attachmentWithHighlights;
	}
    }

    $scope.isMessageTypeEqualTo = function(message, type) {
	if ($scope.activeTab === $scope.tabName) {
		if (!msgType) {
			message.formattedIndexedMailMessage.messageType = 'EMAIL';
		}
		return message.formattedIndexedMailMessage.messageType == type;
	}
    }

    $scope.isImportanceEqualTo = function(message, importance) {
	if ($scope.activeTab === $scope.tabName) {
		return message.formattedIndexedMailMessage.importance == importance;
	}
    }


    $scope.unselectAllMessages = function() {
	if ($scope.activeTab === $scope.tabName) {
		angular.forEach($scope.messages, function(message) {
		    message.selected = false;
		});
	/*
		$scope.$emit('updateSearch', {
			inputSearch: $scope.inputSearch
		});
	*/
	}
    }

    $scope.selectMessage = function(message) {
	if ($scope.activeTab === $scope.tabName) {
		if ($scope.messages.length > 0) {
			$scope.unselectAllMessages();
			if (!$scope.highlightActive) {
				message.selected = !message.selected;
			}
			else {
				message.selected = true;
			}
			$scope.unhighlightAllMessages();
			
			$scope.$emit('selectResultRequest', message, $scope.inputSearch);
		}
	}
    }

    $rootScope.$on('selectResultData', function(evt, result) {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.currentMessage = result.payload.messages[0];
	}
    });

    $scope.unhighlightAllMessages = function() {
	if ($scope.activeTab === $scope.tabName) {
		angular.forEach($scope.messages, function(entry) {
		    $scope.unhighlightMessage(entry);
		});
	    	$scope.$emit('highlightActive', false);
	}
    }

    $scope.highlightAllMessages = function(message) {
	if ($scope.activeTab === $scope.tabName) {
		angular.forEach($scope.messages, function(entry) {
		    $scope.highlightMessage(entry);
		});
	    	$scope.$emit('highlightActive', true);
	}
    }

    $scope.highlightMessage = function(evt, message) {
	if ($scope.activeTab === $scope.tabName) {
	    evt.stopPropagation();
	    message.highlighted = true;
	    $scope.$emit('highlightActive', true);
	}
    }

    $scope.unhighlightMessage = function(message) {
	if ($scope.activeTab === $scope.tabName) {
	    message.highlighted = false;
	}
    }

    $rootScope.$on('reset', function() {
	$scope.reset();
    });

    $scope.reset = function () {
	if ($scope.activeTab === $scope.tabName) {
		$scope.searchMessages = 0;
		$scope.matches = [];
		$scope.messages = [];
		$scope.currentPage = 1;
		$scope.showSearchResults = false;
	}
    }

    $rootScope.$on('pageChange', function(evt, page) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.currentPage = page;
	}
    });
});
