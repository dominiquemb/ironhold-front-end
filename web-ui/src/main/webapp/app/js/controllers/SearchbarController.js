'use strict';

ironholdApp.controller('SearchbarController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);

    var typingTimer;

    $scope.showSearchPreviewResults = false;
    $scope.searchMatches;
    $scope.showSuggestions = false;
    $scope.searchFieldHilite = false;
    $scope.searchTime;
    $scope.suggestons;

    searchResultsService.prepForBroadcast("-", "- ");

    messagesService.one("searchHistory").get().then(function(result) {
       $scope.searchHistory = result.payload;
    });

    $scope.search = function() {
	$scope.$emit('search', {
		inputSearch: $scope.inputSearch
	});
    }

    $scope.updateSearch = function() {
	$scope.$emit('updateSearch');
    }

    $rootScope.$on('updateSearchbar', function(evt, args) {
	angular.forEach(args, function(settingValue, settingName) {
		$scope[settingName] = settingValue;
	});
    });

    $scope.toggleSearchHilite = function() {
	$scope.searchFieldHilite = !$scope.searchFieldHilite;
    }

    $scope.replace = function(oldText, newText) {
        $scope.inputSearch = $scope.inputSearch.replace(oldText, newText);
        $scope.reset();
        $scope.search();
    }

    $scope.replace = function(newText) {
        $scope.inputSearch = newText;
        $scope.reset();
        $scope.search();
    }

    $scope.reset = function () {
        $scope.showSearchPreviewResults = false;
        $scope.showSuggestions = false;
        $scope.searchMessages = 0;
        $scope.searchTime = 0;
        $scope.suggestions = [];
    }


    $scope.searchKeyUp = function($event) {
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

    $scope.searchPreview = function () {
        $scope.reset();
        messagesService.one("count").get({criteria: $scope.inputSearch}).then(function(result) {
            $scope.searchMatches = result.payload.matches;
            $scope.searchTime = result.payload.timeTaken;
            $scope.showSearchPreviewResults = true;
        });

    }
});
