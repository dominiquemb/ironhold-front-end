'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);

    var typingTimer;

    $scope.mode = 'text';
    $scope.showSearchPreviewResults = false;
    $scope.showSuggestions = false;
    $scope.searchFieldHilite = false;
    $scope.currentPage = 1;
    $scope.pageSize = 10;
    $scope.msgs;

    searchResultsService.prepForBroadcast("-", "- ");

    messagesService.one("demo", "demo").one("searchHistory").get().then(function(result) {
       $scope.searchHistory = result.payload;
    });

    $scope.switchMode = function(newMode) {
        $scope.mode = newMode;
    }

    $scope.toggleSearchHilite = function() {
	$scope.searchFieldHilite = !$scope.searchFieldHilite;
    }

    $scope.initCustomScrollbars = function(selector) {
        $timeout(function() {
                $(selector).jScrollPane({
                        verticalArrowPositions: 'split',
                        horizontalArrowPositions: 'split',
			showArrows: true
                });
        }, 0);
    }

    $scope.reinitScrollbars = function() {
    	$('.scrollbar-hidden').data('jsp').reinitialise();
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

    $scope.updateSearch = function() {
        messagesService.one("demo").post("", $scope.selectedFacets, {criteria: $scope.inputSearch, page: $scope.currentPage, pageSize: $scope.pageSize}, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
                $scope.searchTime = result.payload.timeTaken;
                $scope.msgs = result.payload.messages;
                angular.forEach($scope.msgs, function(message) {
                    message.collapsedBody = true;
                    message.collapsedAttachments = false;
                });
                searchResultsService.prepForBroadcast($scope.searchMatches, $scope.searchTime);
		$scope.$emit('results', {
			'matches': result.payload.matches,
			'resultEntries': $scope.msgs
		});
        });
    }

    $scope.reset = function () {
        $scope.showSearchResults = false;
        $scope.showSearchPreviewResults = false;
        $scope.showSuggestions = false;
        $scope.showMessage = false;
        $scope.searchMessages = 0;
        $scope.searchTime = 0;
        $scope.messages = [];
        $scope.suggestions = [];
        $scope.currentPage = 1;
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
        messagesService.one("demo","count").get({criteria: $scope.inputSearch}).then(function(result) {
            $scope.searchMatches = result.payload.matches;
            $scope.searchTime = result.payload.timeTaken;
            $scope.showSearchPreviewResults = true;
        });

    }

    $scope.goTo = function(page) {
        if (page > 0) {
            $scope.currentPage = page;
            $scope.updateSearch();
        }
    }

    $scope.search = function () {
        messagesService.one("demo").get({criteria: $scope.inputSearch, facets: "from,from_domain,to,to_domain,date,msg_type,file_ext", pageSize: $scope.pageSize}).then(function(result) {
            $scope.searchTime = result.payload.timeTaken;
            $scope.suggestions = result.payload.suggestions;
            $scope.showSearchPreviewResults = true;
	    $scope.msgs = result.payload.messages;
            angular.forEach($scope.msgs, function(message) {
                message.collapsedBody = true;
                message.collapsedAttachments = false;
            });
            $scope.showSuggestions = $scope.suggestions.length > 0 && $scope.suggestions[0].options.length > 0;
            searchResultsService.prepForBroadcast(result.payload.matches, $scope.searchTime);
    	    $scope.initCustomScrollbars('.scrollbar-hidden');

	    $scope.$emit('facets', result.payload.facets);
	    $scope.$emit('results', {
		'matches': result.payload.matches,
		'resultEntries': $scope.msgs
		});
        });
    }
});
