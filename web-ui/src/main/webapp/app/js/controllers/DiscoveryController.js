'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService) {
    var typingTimer;

    $scope.showSearchResults = false;
    $scope.showSearchPreviewResults = false;
    $scope.showSuggestions = false;
    $scope.showMessage = false;
    $scope.selectedFacets = [];
    searchResultsService.prepForBroadcast("-", "- ");
    var restMessagesService = Restangular.setBaseUrl('http://localhost:8080/messages');

    $scope.initCustomScrollbars = function(selector) {
        $timeout(function() {
                $(selector).jScrollPane({
                        verticalArrowPositions: 'split',
                        horizontalArrowPositions: 'split'
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
        restMessagesService.one("demo").post("", $scope.selectedFacets, {criteria: $scope.inputSearch}, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
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

    $scope.toggleCollapse = function(item) {
	    item = !item;
    }

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
        $scope.unselectAllMessages();
        message.selected = true;
        $scope.currentMessage = message;
        $scope.showMessage = true;
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
    }


    $scope.searchKeyUp = function($event) {
        $scope.reset();
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
        restMessagesService.one("demo","count").get({criteria: $scope.inputSearch}).then(function(result) {
            $scope.searchMatches = result.payload.matches;
            $scope.searchTime = result.payload.timeTaken;
            $scope.showSearchPreviewResults = true;
        });

    }

    $scope.search = function () {
        restMessagesService.one("demo").get({criteria: $scope.inputSearch, facets: "from,from_domain,to,to_domain,date,msg_type,file_ext"}).then(function(result) {
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
