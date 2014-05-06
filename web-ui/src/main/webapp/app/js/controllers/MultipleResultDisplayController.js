(function () {
   'use strict';

ironholdApp.controller('MultipleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.mode = 'text';
    $scope.currentPage = 1;
    $scope.entries = {};
    $scope.matches = 0;
    $scope.currentEntryNumber = {};
    $scope.initialState = true;
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
    $scope.removeInstructions = false;

    $rootScope.$on('removeInstructions', function() {
	$scope.removeInstructions = true;
    });

    $scope.$emit('initCustomScrollbars');

    $scope.entryNumMatchesKey = function(key) {
        if ($scope.activeTab === $scope.tabName) {
		return $scope.currentEntryNumber[$scope.tabName] === key;
	}
    };

    $scope.getEntries = function() {
        if ($scope.activeTab === $scope.tabName) {
		return $scope.entries[$scope.tabName];
	}
    };

    $scope.noEntries = function() {
        if ($scope.activeTab === $scope.tabName) {
		if ($scope.entries[$scope.tabName]) {
			return $scope.entries[$scope.tabName].length === 0;
		}
		else {
			return true;
		}
	}
    };

    $scope.getImportance = function(msg) {
        if ($scope.activeTab === $scope.tabName) {
		return (msg.importance) ? 'importance-' + msg.importance : 'importance-none';
	}
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

    $scope.getCurrentEntryNumber = function() {
        if ($scope.activeTab === $scope.tabName) {
		return $scope.currentEntryNumber[$scope.tabName] || 0;
	}
    };

    $rootScope.$on('selectBelowMessage', function() {
        if ($scope.activeTab === $scope.tabName) {
		if (($scope.currentEntryNumber[$scope.tabName] + 1) >= 0) {
			$scope.currentEntryNumber[$scope.tabName]++;
			$scope.selectMessage($scope.entries[$scope.tabName][$scope.getCurrentEntryNumber()], $scope.getCurrentEntryNumber());
		}
	}
    });

    $rootScope.$on('selectAboveMessage', function() {
        if ($scope.activeTab === $scope.tabName) {
		if (($scope.currentEntryNumber[$scope.tabName] - 1) >= 0) {
			$scope.currentEntryNumber[$scope.tabName]--;
			$scope.selectMessage($scope.entries[$scope.tabName][$scope.getCurrentEntryNumber()], $scope.getCurrentEntryNumber());
		}
	}
    });

    $scope.resetNoResults = function() {
        if ($scope.activeTab === $scope.tabName) {
		$scope.showNoResults = true;
		$scope.$emit('resetSingleResultPanel');
		$scope.currentMessageNumber = -1;
	}
    };

    $rootScope.$on('results', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
	    $scope.initialState = false;
            $scope.entries[$scope.tabName] = args.resultEntries;
	
	    if ($scope.tabName === 'search') {
		    $scope.matches = args.matches;
		    if ($scope.matches > 0) {
			$scope.currentEntryNumber[$scope.tabName] = -1;
			$scope.showNoResults = false;
			$scope.showSearchResults = true;
			$scope.$emit('showSearchResults', true);
			$scope.showSearchResults = false;
			$scope.hidePlaceholderScroller = true;
			}
		    else {
			$scope.resetNoResults();
		    }
	    }
	    else {
		if ($scope.entries[$scope.tabName].length > 0) {
			$scope.currentEntryNumber[$scope.tabName] = -1;
			$scope.showNoResults = false;
			$scope.showSearchResults = true;
			$scope.$emit('showSearchResults', true);
			$scope.showSearchResults = false;
			$scope.hidePlaceholderScroller = true;
		}
		else {
			$scope.resetNoResults();
		}
	    }
            $scope.$emit('reinitScrollbars');
        }
    });

    $rootScope.$on('search', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = args.inputSearch;
	    $scope.initialState = false;
	    $scope.loadingTimeout = $timeout(function() {
		$scope.showLoading = true;
	    }, 2000);
        }
    });

    $scope.hasAttachment = function(message) {
        if ($scope.activeTab === $scope.tabName) {
            return message.attachmentWithHighlights;
        }
    };

    $scope.getMessageType = function(message) {
        if ($scope.activeTab === 'search') {
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
	if ($scope.activeTab === 'search') {
            return message.formattedIndexedMailMessage.importance === importance;
        }
    };


    $scope.unselectAllMessages = function() {
        if ($scope.activeTab === $scope.tabName) {
            angular.forEach($scope.entries[$scope.tabName], function(message) {
                message.selected = false;
            });
	    $scope.currentMessageNumber[$scope.tabName] = 0;
        /*
            $scope.$emit('updateSearch', {
                inputSearch: $scope.inputSearch
            });
        */
        }
    };

    $scope.selectEntry = function(entry, key) {
        if ($scope.activeTab === $scope.tabName) {
            if ($scope.entries[$scope.tabName].length > 0) {
                $scope.unselectAllMessages();
                if (!$scope.highlightActive) {
                    entry.selected = !entry.selected;

		    if (entry.selected) {
		    	$scope.currentEntryNumber[$scope.tabName] = key;
		    }	
                }
                else {
                    entry.selected = true;
		    $scope.currentEntryNumber[$scope.tabName] = key;
                }
                $scope.unhighlightAllMessages();
		entry.highlighted = true;
	    }
	}
    };

    $scope.selectMessage = function(entry, key) {
        if ($scope.activeTab === $scope.tabName) {
		$scope.selectEntry(entry, key);
	        $scope.$emit('selectResultRequest', entry, $scope.inputSearch);
        }
    };

    $scope.selectUser = function(entry, key) {
        if ($scope.activeTab === $scope.tabName) {
		$scope.selectEntry(entry, key);
	        $scope.$emit('selectResultRequest', entry);
	}
    };

    $rootScope.$on('selectResultData', function(evt, result) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.currentMessage = result;
        }
    });

    $scope.unhighlightAllMessages = function() {
        if ($scope.activeTab === $scope.tabName) {
            angular.forEach($scope.entries[$scope.tabName], function(entry) {
                $scope.unhighlightMessage(entry);
            });
                $scope.$emit('highlightActive', false);
        }
    };

    $scope.highlightAllMessages = function() {
        if ($scope.activeTab === $scope.tabName) {
            angular.forEach($scope.entries[$scope.tabName], function(entry) {
                $scope.highlightMessage(entry);
            });
                $scope.$emit('highlightActive', true);
        }
    };

    $scope.toggleHighlight = function(message, evt) {
        if ($scope.activeTab === $scope.tabName) {
	    if (evt) {
		evt.stopPropagation();
	    }

	    var highlighted = 0;

	    angular.forEach($scope.entries[$scope.tabName], function(entry) {
		if (entry.highlighted) {
			highlighted++;
		}
	    });

	    if (message.highlighted) {
		    if (highlighted > 1) {
			message.highlighted = false;
			highlighted--;
		    }
	    }
	    else {
		message.highlighted = true;
		highlighted++;
	    }

	    if (highlighted > 1) {
		$scope.$emit('highlightActive', true);
	    }
	    else {
		$scope.$emit('highlightActive', false);
	    }

	    if (highlighted === 1 && message.highlighted) {
		message.selected = true;
		$scope.$emit('selectResultRequest', message, $scope.inputSearch);
	    }

	    $scope.selectRemainingMessage();
	}
    };

    $scope.selectRemainingMessage = function() {
        if ($scope.activeTab === $scope.tabName) {
	    var highlighted = [];

	    angular.forEach($scope.entries[$scope.tabName], function(entry, key) {
		if (entry.highlighted) {
			highlighted.push({entry: entry, key: key});
		}
	    });

	    if (highlighted.length === 1) {
		$scope.selectEntry(highlighted[0].entry, highlighted[0].key);
	        $scope.$emit('selectResultRequest', highlighted[0].entry, $scope.inputSearch);
	    }
	}
    };

    $scope.highlightMessage = function(message) {
        if ($scope.activeTab === $scope.tabName) {
	    message.highlighted = true;
	}
    };

    $scope.unhighlightMessage = function(message) {
        if ($scope.activeTab === $scope.tabName) {
		message.highlighted = false;
		message.selected = false;
        }
    };

    $rootScope.$on('reset', function() {
        $scope.reset();
    });

    $scope.reset = function () {
        if ($scope.activeTab === $scope.tabName) {
            $scope.searchMessages = 0;
            $scope.matches = [];
            $scope.entries[$scope.tabName] = [];
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
