'use strict';

ironholdApp.controller('SearchbarController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    var typingTimer;

    $scope.showSearchPreviewResults = false;
    $scope.searchMatches;
    $scope.showSuggestions = false;
    $scope.searchFieldHilite = false;
    $scope.searchTime;
    $scope.suggestons;
    $scope.inputSearch = '';
    $scope.currentlySearching = false;

    searchResultsService.prepForBroadcast("-", "- ");

    $rootScope.$on('searchHistoryData', function(evt, result) {
       $scope.searchHistory = result.payload;
    });

    $scope.search = function(query) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.inputSearch = query;
		$scope.currentlySearching = true;
		$scope.$emit('search', {
			inputSearch: $scope.inputSearch
		});
	}
    }

    $scope.updateSearch = function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.currentlySearching = false;
		$scope.$emit('updateSearch');
	}
    }

    $rootScope.$on('updateSearchbar', function(evt, args) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.currentlySearching = false;
		angular.forEach(args, function(settingValue, settingName) {
			$scope[settingName] = settingValue;
		});
	}
    });

    $scope.toggleSearchHilite = function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.searchFieldHilite = !$scope.searchFieldHilite;
	}
    }

    $scope.replaceSearchInput = function(oldText, newText) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.inputSearch = $scope.inputSearch.replace(oldText, newText);
		$scope.reset();
		$scope.search();
	}
    }

    $scope.newSearchInput = function(newText) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.inputSearch = newText;
		$scope.reset();
		$scope.search();
	}
    }

    $scope.reset = function () {
	if ($scope.activeTab === $scope.tabName) {
		$scope.showSearchPreviewResults = false;
		$scope.showSuggestions = false;
		$scope.searchMessages = 0;
		$scope.searchTime = 0;
		$scope.suggestions = [];
		$scope.searchMatches = 0;
	}
    }


    $scope.searchKeyUp = function($event) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.reset();
		$scope.searchFieldHilite = true;
		if ($event.keyCode == 13) { // ENTER
		    $timeout.cancel(typingTimer);
		    $scope.search();
		} else {
		    $timeout.cancel(typingTimer);
		    typingTimer = $timeout(function() {
			  $scope.searchPreview();
			}, 500);
		}
	}
    }

    $scope.searchPreview = function () {
	if ($scope.activeTab === $scope.tabName) {
		$scope.reset();
		$scope.$emit('searchPreviewRequest', $scope.inputSearch);
	}
    }

    $rootScope.$on('searchPreviewData', function(evt, result) {
	if ($scope.activeTab === $scope.tabName) {
            $scope.searchMatches = result.payload.matches;
            $scope.searchTime = result.payload.timeTaken;
            $scope.showSearchPreviewResults = true;
	}
    });

});
