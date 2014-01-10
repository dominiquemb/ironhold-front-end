'use strict';
var ironholdApp = angular.module('ironholdApp', ['ngRoute','ngResource','ngSanitize','ui.bootstrap','restangular','ui.router','ivpusic.cookie'])
    .config(function ($stateProvider, $urlRouterProvider) {
	    $urlRouterProvider.otherwise('/login');	    
	    $stateProvider
	    .state('login', {
		    url: "/login",
		    templateUrl: "views/Login.html",
		    controller: 'LoginController'
	    })
	    .state('main', {
		    url: "/main",
		    templateUrl: "views/Navigation.html"
	    })
	    .state('main.discovery', {
		    url: "/discovery",
		    templateUrl: "views/Discovery.html",
		    controller: 'DiscoveryController'
	    })
	    .state('main.tags', {
		    url: "/tags",
		    templateUrl: "views/Tags.html"
	    })
	    .state('main.folders', {
		    url: "/folders",
		    templateUrl: "views/Folders.html"
	    })
	    .state('main.logs', {
		    url: "/logs",
		    templateUrl: "views/Logs.html"
	    })
	    .state('main.users', {
		    url: "/users",
		    templateUrl: "views/Users.html"
	    })
	    .state('main.settings', {
		    url: "/settings",
		    templateUrl: "views/Settings.html"
	    });
    /*
        $routeProvider.when('/discovery',
            {
                templateUrl:'views/Discovery.html',
                controller: 'DiscoveryController'
            });
        $routeProvider.otherwise({redirectTo: '/discovery'});
        //$locationProvider.html5Mode(true);
    */ });

ironholdApp.factory('logInService', function($rootScope, ipCookie) {
	var loggedIn = false;

	var sessions = function() {
		this.logOutCallbacks = [];
		this.logInCallbacks = [];
	};

	sessions.prototype = {
		onLogOut: function(callback) {
			this.logOutCallbacks.push(callback);
		},

		onLogIn: function(callback) {
			this.logInCallbacks.push(callback);
		},

		logIn: function() {
			loggedIn = true;
			ipCookie('ironholdSession', null /* REPLACE THIS WITH A SESSION ID LATER */, { expires: 99 });
			angular.forEach(this.logInCallbacks, function(callback, index) {
				callback();
			});
		},

		logOut: function() {
			loggedIn = false;
			ipCookie.remove('ironholdSession');
			angular.forEach(this.logOutCallbacks, function(callback, index) {
				callback();
			});
		},

		confirmLoggedIn: function($state) {
			if (ipCookie('ironholdSession') === undefined) {
				if (loggedIn !== true) {
					$state.go('login');
					return false;
				} 
			}
			return true;
		},

		isLoggedIn: function() {
			return loggedIn;
		}
	};
	return new sessions();
});

ironholdApp.factory('messagesService', function(Restangular) {
	var messagesService = Restangular.setBaseUrl('http://localhost:8080/messages');
	return messagesService;
});

ironholdApp.factory('usersService', function(Restangular) {
	var usersService =  Restangular.setBaseUrl('http://localhost:8080/users');
	return usersService;
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

ironholdApp.directive('facetCollection', function() {
	return {
		scope: true,
		restrict: 'ACE',
		controller: 'FacetController',
		link: function(scope, elem, attrs) {
// To be updated later
/*			$(elem).find('.facet-toggle').on('click', function() {
				toggleFacet(facetValue, facet.name.value);
			});
*/		}
	}
});

ironholdApp.directive('collapsible', function() {
	return {
/*		scope: {
			'collapsedArrow': '@',
			'uncollapsedArrow': '@'
		},
*/
		scope: true,
		restrict: 'ACE',
		link: function(scope, elem, attrs) {
			var arrow = $(elem).find('.collapse-trigger');
			if ($(elem).hasClass('collapsed')) {
				arrow.addClass(scope.collapsedArrow);
			}
			else arrow.addClass(scope.uncollapsedArrow);
			arrow.on('click', function() {
				$(elem).toggleClass('collapsed');
				arrow.toggleClass(scope.collapsedArrow + ' ' + scope.uncollapsedArrow);
			});
		}
	}
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
