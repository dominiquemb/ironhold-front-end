'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService, usersService) {
    logInService.confirmLoggedIn($state);

    $scope.msgs;
    $scope.pageSize = 10;
    $scope.currentPage = 1;

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

    $scope.reinitScrollbars = function() {
    	$('.scrollbar-hidden').data('jsp').reinitialise();
    }

    $rootScope.$on('pageChange', function(evt, page) {
	$scope.currentPage = page;
	$scope.$emit('updateSearch', {
		inputSearch: $scope.inputSearch
	});
    });

    $rootScope.$on('updateSearch', function(evt, args) {
        messagesService.one("demo").post("", $scope.selectedFacets, {
		criteria: args.inputSearch, 
		page: $scope.currentPage, 
		pageSize: $scope.pageSize
		}, 
		{
		"Accept": "application/json", 
		"Content-type" : "application/json"
		})
	.then(function(result) {
	    $scope.$emit('updateSearchbar', {
		searchTime: result.payload.timeTaken
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
        messagesService.one("demo").get({criteria: args.inputSearch, facets: "from,from_domain,to,to_domain,date,msg_type,file_ext", pageSize: $scope.pageSize}).then(function(result) {
	    $scope.$emit('updateSearchbar', {
		searchTime: result.payload.timeTaken,
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
