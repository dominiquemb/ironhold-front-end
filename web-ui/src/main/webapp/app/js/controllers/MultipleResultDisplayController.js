(function () {
   'use strict';

ironholdApp.controller('MultipleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.mode = 'text';
    $scope.currentPage = 1;
    $scope.messages = [];
    $scope.matches = 0;
    $scope.showSearchResults = false;
    $scope.currentMessageNumber = -1;
    $scope.showNoResults = false;
    $scope.inputSearch = null;
    $scope.showLoading = false;
    $scope.hidePlaceholderScroller = false;
    $scope.highlightActive = false;
    $scope.messageTypeTable = {
	'bloomberg_message': 'Bloomberg Message',
	'bloomberg_chat': 'Bloomberg Chat',
	'email': 'Email Message'
    };

    $scope.getImportance = function(msg) {
	return (msg.importance) ? 'importance-' + msg.importance : 'importance-none';
    };

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

    $rootScope.$on('selectBelowMessage', function() {
        if ($scope.activeTab === $scope.tabName) {
		if (($scope.currentMessageNumber + 1) >= 0) {
			$scope.currentMessageNumber++;
			$scope.selectMessage($scope.messages[$scope.currentMessageNumber], $scope.currentMessageNumber);
		}
	}
    });

    $rootScope.$on('selectAboveMessage', function() {
        if ($scope.activeTab === $scope.tabName) {
		if (($scope.currentMessageNumber - 1) >= 0) {
			$scope.currentMessageNumber--;
			$scope.selectMessage($scope.messages[$scope.currentMessageNumber], $scope.currentMessageNumber);
		}
	}
    });

    $rootScope.$on('results', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.messages = args.resultEntries;
            $scope.matches = args.matches;
            if ($scope.matches > 0) {
	    	$scope.showNoResults = false;
                $scope.showSearchResults = true;
//		clearTimeout($scope.loadingTimeout);
//	    	$scope.showLoading = false;
                $scope.$emit('showSearchResults', true);
		$scope.showSearchResults = false;
		$scope.hidePlaceholderScroller = true;
		}
	    else {
	    	$scope.showNoResults = true;
	    }
        }
    });

    $rootScope.$on('search', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = args.inputSearch;
	    $scope.loadingTimeout = setTimeout(function() {
		$scope.showLoading = true;
	    }, 200);
        }
    });

    $scope.hasAttachment = function(message) {
        if ($scope.activeTab === $scope.tabName) {
            return message.attachmentWithHighlights;
        }
    };

    $scope.getMessageType = function(message) {
        if ($scope.activeTab === $scope.tabName) {
	    var type = message.formattedIndexedMailMessage.messageType;

            if (!type) {
                type = 'email';
            }
	    else {
		type = type.toLowerCase();
	    }

            return {
		'type': type,
		'title': $scope.messageTypeTable[type]
	    };
        }
    };

    $scope.isImportanceEqualTo = function(message, importance) {
        if ($scope.activeTab === $scope.tabName) {
            return message.formattedIndexedMailMessage.importance === importance;
        }
    };


    $scope.unselectAllMessages = function() {
        if ($scope.activeTab === $scope.tabName) {
            angular.forEach($scope.messages, function(message) {
                message.selected = false;
            });
	    $scope.currentMessageNumber = 0;
        /*
            $scope.$emit('updateSearch', {
                inputSearch: $scope.inputSearch
            });
        */
        }
    };

    $scope.selectMessage = function(message, key) {
        if ($scope.activeTab === $scope.tabName) {
            if ($scope.messages.length > 0) {
                $scope.unselectAllMessages();
                if (!$scope.highlightActive) {
                    message.selected = !message.selected;

		    if (message.selected) {
		    	$scope.currentMessageNumber = key;
		    }	
                }
                else {
                    message.selected = true;
		    $scope.currentMessageNumber = key;
                }
                $scope.unhighlightAllMessages();

                $scope.$emit('selectResultRequest', message, $scope.inputSearch);
            }
        }
    };

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
    };

    $scope.highlightAllMessages = function() {
        if ($scope.activeTab === $scope.tabName) {
            angular.forEach($scope.messages, function(entry) {
                $scope.highlightMessage(entry);
            });
                $scope.$emit('highlightActive', true);
        }
    };

    $scope.highlightMessage = function(evt, message) {
        if ($scope.activeTab === $scope.tabName) {
            evt.stopPropagation();

	    var highlighted = 0;

            message.highlighted = true;

	    angular.forEach($scope.messages, function(entry) {
		if (entry.highlighted) {
			highlighted++;
		}
	    });

	    if (highlighted > 1) {
            	$scope.$emit('highlightActive', true);
	    }

	    if (highlighted === 1 && message.highlighted) {
		message.selected = true;
                $scope.$emit('selectResultRequest', message, $scope.inputSearch);
	    }
        }
    };

    $scope.unhighlightMessage = function(message) {
        if ($scope.activeTab === $scope.tabName) {
            message.highlighted = false;
        }
        };

        $rootScope.$on('reset', function() {
        $scope.reset();
    });

    $scope.reset = function () {
        if ($scope.activeTab === $scope.tabName) {
            $scope.searchMessages = 0;
            $scope.matches = [];
            $scope.messages = [];
	    $scope.currentMessageNumber = -1;
	    $scope.showNoResults = false;
	    $scope.showLoading = false;
            $scope.currentPage = 1;
            $scope.showSearchResults = false;
        }
    };

    $rootScope.$on('pageChange', function(evt, page) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.currentPage = page;
        }
    });
});


}());
