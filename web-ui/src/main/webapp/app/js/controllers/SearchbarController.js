(function () {
   'use strict';

ironholdApp.controller('SearchbarController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    var typingTimer;

    $scope.showSearchPreviewResults = false;
    $scope.searchMatches = null;
    $scope.showSuggestions = false;
    $scope.searchFieldHilite = false;
    $scope.searchTime = null;
    $scope.suggestons = null;
    $scope.showSortingPanel = null;
    $scope.inputSearch = '';
    $scope.searchProgressShow = false;
    $scope.searchProgressTimer = false;
    $scope.searchProgressText = 'Searching...';
    $scope.searchProgressCount = 0;

    searchResultsService.prepForBroadcast("-", "- ");

    $scope.toggleSortingPanel = function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.showSortingPanel = !$scope.showSortingPanel;
	}
    };

    $rootScope.$on('searchHistoryData', function(evt, result) {
	if ($scope.activeTab === $scope.tabName) {
       		$scope.searchHistory = result.payload;
	}
    });

    $rootScope.$on('triggerSearch', function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.updateSearch();
	}
    });

    $scope.search = function() {
        if ($scope.activeTab === $scope.tabName) {
	    var disableFacets = false;

            $scope.currentlySearching(true);
            $scope.$emit('reset');

	    if ($scope.searchMatches > 20000) {
		disableFacets = true;
	    }

            $scope.$emit('search', {
                inputSearch: $scope.inputSearch,
		disableFacets: disableFacets
            });
        }
    };

    $scope.currentlySearching = function(showhide) {
        if (showhide && !$scope.searchProgressTimer) {
            $scope.searchProgressTimer = setInterval(function() {
                $('.search-progress-text').html(
                    $('.search-progress-text').html() + ' . . .'
                );
                $scope.searchProgressCount++;
                if ($scope.searchProgressCount > 2) {
                    $('.search-progress-text').html('Searching . . .');
                    $scope.searchProgressCount = 0;
                }
                $scope.$apply();
            }, 1000);
        } else if (!showhide) {
            clearInterval($scope.searchProgressTimer);
            $scope.searchProgressTimer = false;
            $('.search-progress-text').html('Searching . . .');
            $scope.showSortingPanel = true;
        }
        $scope.searchProgressShow = showhide;
    };

    $rootScope.$on('totalResultsChange', function(evt, result) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.searchMatches = result.payload.matches;
        }
    });

    $scope.updateSearch = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.$emit('updateSearch', {
                inputSearch: $scope.inputSearch
            });
        }
    };

    $rootScope.$on('updateSearchbar', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.currentlySearching(false);
            angular.forEach(args, function(settingValue, settingName) {
                $scope[settingName] = settingValue;
            });
        }
    });

    $scope.resetSearch = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.$emit('reset');
            $scope.reset();
        }
    };

    $scope.toggleSearchHilite = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.searchFieldHilite = !$scope.searchFieldHilite;
        }
    };

    $scope.replaceSearchInput = function(oldText, newText) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = $scope.inputSearch.replace(oldText, newText);
            $scope.reset();
            $scope.search();
        }
    };

    $scope.newSearchInput = function(newText) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = newText;
            $scope.reset();
            $scope.search();
        }
    };

    $scope.reset = function () {
        if ($scope.activeTab === $scope.tabName) {
            $scope.showSearchPreviewResults = false;
            $scope.showSuggestions = false;
            $scope.searchMessages = 0;
            $scope.searchTime = 0;
            $scope.suggestions = [];
            $scope.searchMatches = 0;
                $scope.searchFieldHilite = false;
        }
    };


    $scope.searchKeyUp = function($event) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.reset();
            $scope.searchFieldHilite = true;
            if ($event.keyCode === 13) { // ENTER
                $timeout.cancel(typingTimer);
            } else {
                $timeout.cancel(typingTimer);
                typingTimer = $timeout(function() {
                  $scope.searchPreview();
                }, 500);
            }
        }
    };

    $scope.searchPreview = function () {
        if ($scope.activeTab === $scope.tabName) {
            $scope.reset();
                $scope.currentlySearching(false);
            $scope.$emit('searchPreviewRequest', $scope.inputSearch);
        }
    };


    $rootScope.$on('searchPreviewData', function(evt, result) {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.currentlySearching(false);
            $scope.searchMatches = result.payload.matches;
            $scope.searchTime = result.payload.timeTaken;
            $scope.showSearchPreviewResults = true;
	    $scope.showSortingPanel = false;
	}
    });

});



}());
