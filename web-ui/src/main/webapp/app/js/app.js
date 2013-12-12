'use strict';
var ironholdApp = angular.module('ironholdApp', ['ngRoute','ngResource','ngSanitize','splitter','ui.bootstrap'])
    .config(function ($routeProvider, $locationProvider) {
        $routeProvider.when('/discovery',
            {
                templateUrl:'views/Discovery.html',
                controller: 'DiscoveryController'
            });
        $routeProvider.otherwise({redirectTo: '/discovery'});
        //$locationProvider.html5Mode(true);
     });

ironholdApp.factory('searchResultsService', function ($rootScope) {
    var sharedService = { };

    sharedService.searchMatches = 0;
    sharedService.searchTime = 0;

    sharedService.prepForBroadcast = function(searchMatches, searchTime) {
        this.searchMatches = searchMatches;
        this.searchTime = searchTime;
        this.broadcastItem();
    }

    sharedService.broadcastItem = function() {
        $rootScope.$broadcast('handleSearchResultsChange');
    }

    return sharedService;
});

ironholdApp.directive('clearForm', function() {
	return {
		scope: {
		},
		restrict: 'ACE',
		link: function(scope, elem, attrs) {
			scope.$watch(function() {
				return $(elem).find('.clear-form-input').val().length;
			}, function(length) {
				if (length > 0) {
					$(elem).addClass('clear-form-active');
				}
			});
			$(elem).find('.clear-form-trigger').on('click', function() {
				$(elem).find('.clear-form-input').val('');
				$(elem).removeClass('clear-form-active');
				scope.$parent.$apply();
			});
		}
	}
});
/*
ironholdApp.directive('splitter', function() {
	return {
		scope: {},
		restrict: 'ACE',
		link: function(scope, elem, attrs) {
			$(elem).
		};
	}
});
*/
ironholdApp.directive('truncate', function() {
	return {	
		scope: {
			'charPxlWidth': '=truncFontWidth',
			'desiredHeight': '=truncDesiredHeight',
			'trailingDots': '=truncTrailingDots',
			'containerWidth': '=containerWidth',
			'text': '@truncText'
		},
		restrict: 'ACE',
		link: function(scope, elem, attrs) {
			scope.$watch(
			'[charPxlWidth, containerWidth, desiredHeight, trailingDots, text]', 
			function() {
					var width = ( scope.containerWidth !== undefined ) ? scope.containerWidth : $(elem).width(),
					charsPerLine = Math.floor( width / scope.charPxlWidth ),
					totalLines = Math.floor(scope.text.length / charsPerLine),
					fontSize = parseInt($(elem).css('font-size')),
					totalHeight = Math.floor( fontSize * totalLines),
					desiredLines = Math.floor( scope.desiredHeight / fontSize ),
					maxChars = (scope.trailingDots) ? Math.floor((charsPerLine * desiredLines)) - 3 : Math.floor((charsPerLine * desiredLines));
					if ((totalHeight > scope.desiredHeight) && (scope.desiredHeight > 0)) {
						if (scope.$parent.truncated !== true) {
							scope.$parent.truncated = true;
							scope.$parent.originalText = scope.text;
						}
						var result = scope.text.split("").splice(0, maxChars).join("") + ( (scope.trailingDots) ? "..." : "");
						$(elem).html(result);
					}
					else {
						$(elem).html(scope.text);
					}
			},
			true);
		}
	}
});

String.prototype.endsWith = function(suffix) {
    return this.toLowerCase().indexOf(suffix.toLowerCase(), this.length - suffix.length) !== -1;
};


String.prototype.plaintext = function(text) {
	if (typeof text === "String") {
		text.replace(/<(?:.|\n)*?>/gm, '');
	}
	else return false;
};

Array.prototype.remove = function() {
    var what, a = arguments, L = a.length, ax;
    while (L && this.length) {
        what = a[--L];
        while ((ax = this.indexOf(what)) !== -1) {
            this.splice(ax, 1);
        }
    }
    return this;
};
