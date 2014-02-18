'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.msgs;
    $scope.pageSize = 10;
    $scope.currentPage = 1;
    $scope.selectedFacets = [];

    searchResultsService.prepForBroadcast("-", "- ");

    $scope.initCustomScrollbars = function(selector) {
        $timeout(function() {
                $(selector).jScrollPane({
                        verticalArrowPositions: 'split',
                        horizontalArrowPositions: 'split',
			showArrows: true
                });
        }, 0);
    }

    if (usersService) {
	    usersService.one("searchHistory").get().then(function(result) {
		$scope.$emit('searchHistoryData', result);
	    });
    }

    $rootScope.$on('modeRequest', function(evt, data) {
	messagesService
		.one(data.messageId)
		.one(data.mode)
		.get({criteria: data.inputSearch})
		.then(function(result) {
			$scope.$emit('modeData', {
				mode: data.mode,
				payload: result.payload
			});
		});
    });

    $rootScope.$on('selectResultRequest', function(evt, message, inputSearch) {
        messagesService.one(message.formattedIndexedMailMessage.messageId).get({criteria: inputSearch}).then(function(result) {
	    $scope.$emit('selectResultData', result);
            $scope.$emit('selectMessage', message);
        });
    });

    $rootScope.$on('searchPreviewRequest', function(evt, inputSearch) {
        messagesService.one("count").get({criteria: inputSearch}).then(function(result) {
            $scope.$emit('totalResultsChange', result);
	    $scope.$emit('searchPreviewData', result);
        });
    });

    $scope.reinitScrollbars = function() {
    	$('.scrollbar-hidden').data('jsp').reinitialise();
    }

    $rootScope.$on('pageChange', function(evt, page) {
	$scope.currentPage = page;
	$scope.$emit('updateSearch', {
		inputSearch: $scope.inputSearch
	});
    });

    $rootScope.$on('facetToggled', function(evt, facet, selectedFacets) {
	$scope.selectedFacets = selectedFacets;
	$scope.$emit('updateSearch', {
		inputSearch: $scope.inputSearch
	});
    });

    $rootScope.$on('updateSearch', function(evt, args) {
        messagesService.post("", $scope.selectedFacets, {
		criteria: args.inputSearch, 
		page: $scope.currentPage, 
		pageSize: $scope.pageSize
		}, 
		{
		"Accept": "application/json", 
		"Content-type" : "application/json"
		})
	.then(function(result) {
	    $scope.$emit('updateFooter', {
		searchTime: result.payload.timeTaken,
		searchMatches: result.payload.matches
	    });
	    $scope.$emit('updateSearchbar', {
		searchTime: result.payload.timeTaken,
		searchMatches: result.payload.matches
	    });
		
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
    });
	
    $rootScope.$on('search', function(evt, args) {
	$scope.inputSearch = args.inputSearch;
        messagesService.get({
		criteria: args.inputSearch, 
		facets: "from,from_domain,to,to_domain,date,msg_type,file_ext", 
		pageSize: $scope.pageSize
	    	})
	    .then(function(result) {
		$scope.$emit('updateFooter', {
			searchTime: result.payload.timeTaken,
			searchMatches: result.payload.matches
		});
	    	$scope.$emit('updateSearchbar', {
			searchTime: result.payload.timeTaken,
			searchMatches: result.payload.matches,
			suggestions: result.payload.suggestions,
			showSearchPreviewResults: true,
			showSuggestions: (result.payload.suggestions[0].options.length > 0) ? true : false
	    	});

		    $scope.msgs = result.payload.messages;
		    angular.forEach($scope.msgs, function(message) {
			message.collapsedBody = true;
			message.collapsedAttachments = false;
		    });
		    searchResultsService.prepForBroadcast(result.payload.matches, $scope.searchTime);
		    $scope.initCustomScrollbars('.scrollbar-hidden');

		    $scope.$emit('facets', result.payload.facets);

		    $scope.$emit('results', {
			'matches': result.payload.matches,
			'resultEntries': $scope.msgs
			});
              });
    });
});
