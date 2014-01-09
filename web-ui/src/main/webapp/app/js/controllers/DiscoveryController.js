'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);

    var typingTimer;

    $scope.mode = 'text';
    $scope.showSearchResults = false;
    $scope.showSearchPreviewResults = false;
    $scope.showSuggestions = false;
    $scope.searchFieldHilite = false;
    $scope.showMessage = false;
    $scope.selectedFacets = [];
    $scope.currentPage = 1;
    $scope.pageSize = 10;

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

    $scope.unselectAllFacets = function() {
        angular.forEach($scope.selectedFacets, function(facet) {
            facet.selected = false;
        });
        $scope.selectedFacets = [];
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

    $scope.toggleFacet = function(facet, facetGroupCode) {
	    facet.selected = !facet.selected;
        if (facet.selected) {
            $scope.selectedFacets.push(facet);
        } else {
            $scope.selectedFacets.remove(facet);
        }

        $scope.updateSearch();
    }

    $scope.updateSearch = function() {
        messagesService.one("demo").post("", $scope.selectedFacets, {criteria: $scope.inputSearch, page: $scope.currentPage, pageSize: $scope.pageSize}, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
                $scope.searchMatches = result.payload.matches;
                $scope.searchTime = result.payload.timeTaken;
                $scope.messages = result.payload.messages;
                angular.forEach($scope.messages, function(message) {
                    message.collapsedBody = false;
                    message.collapsedAttachments = false;
                });
                searchResultsService.prepForBroadcast($scope.searchMatches, $scope.searchTime);
        });
    }

/*   $scope.toggleCollapse = function(object, item) {
       return object[item] = !object[item];
   }
*/
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
console.log(message);
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
        $scope.showSearchPreviewResults = false;
        $scope.showSuggestions = false;
        $scope.showMessage = false;
        $scope.searchMessages = 0;
        $scope.searchTime = 0;
        $scope.facets = [];
        $scope.messages = [];
        $scope.suggestions = [];
        $scope.selectedFacets = [];
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
            $scope.searchMatches = result.payload.matches;
            $scope.searchTime = result.payload.timeTaken;
            $scope.facets = result.payload.facets;
            $scope.messages = result.payload.messages;
            $scope.suggestions = result.payload.suggestions;
            $scope.showSearchPreviewResults = true;
            $scope.showSearchResults = $scope.searchMatches > 0;
            angular.forEach($scope.messages, function(message) {
                message.collapsedBody = false;
                message.collapsedAttachments = false;
            });
            $scope.showSuggestions = $scope.suggestions.length > 0 && $scope.suggestions[0].options.length > 0;
            searchResultsService.prepForBroadcast($scope.searchMatches, $scope.searchTime);
    	    $scope.initCustomScrollbars('.scrollbar-hidden');
        });
    }
});
