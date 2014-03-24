'use strict';
var ironholdApp = angular.module('ironholdApp', ['ngRoute','ngResource','ngSanitize','ui.bootstrap','restangular','ui.router','ivpusic.cookie'])
    .config(function ($stateProvider, $urlRouterProvider, RestangularProvider) {
        RestangularProvider.setBaseUrl('${rest-api.proto}://${rest-api.host}:${rest-api.port}/${rest-api.prefix}');
	    $urlRouterProvider.otherwise('/login');	    
	    $stateProvider
	    .state('login', {
		    url: "/login",
		    templateUrl: "views/Login.html",
		    controller: 'LoginController'
	    })

	    .state('loggedin', {
		    templateUrl: "views/Navigation.html"
	    })

	    /* Search tab and subtabs */
	    .state('loggedin.main', {
		    templateUrl: "views/TabContainers.html"
	    })

	    .state('loggedin.main.text', {
		    url: "/main",
		    templateUrl: "views/PreviewTabs/TextTab.html"
	    })

	    .state('loggedin.main.body', {
		    templateUrl: "views/PreviewTabs/HtmlTab.html"
	    })

	    .state('loggedin.main.headers', {
		    templateUrl: "views/PreviewTabs/HeadersTab.html"
	    })

	    .state('loggedin.main.sources', {
		    templateUrl: "views/PreviewTabs/AuditTab.html"
	    })

	    /* Discovery tab */
	    .state('main.discovery', {
		   url: "/discovery",
		   templateUrl: "views/Discovery.html"
	    })

	    /* Tags tab and subtabs */
	    .state('main.tags', {
		    url: "/tags",
		    templateUrl: "views/Tags.html"
	    })

	    /* Folders tab and subtabs */
	    .state('main.folders', {
		    url: "/folders",
		    templateUrl: "views/Folders.html"
	    })

	    /* Logs tabs and subtabs */
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
    });

ironholdApp.factory('httpRequestInterceptor', function (logInService) {
  return {
    request: function (config) {
      if (logInService.getAuthdata()) {
            config.headers['Authorization'] ='Basic ' + logInService.getAuthdata();
      }
      return config;
    }
  };
});

ironholdApp.config(function ($httpProvider) {
  $httpProvider.interceptors.push('httpRequestInterceptor');
});

ironholdApp.factory('logInService', function($rootScope, ipCookie, Base64) {
	var loggedIn = false;

	var sessions = function() {
		this.logOutCallbacks = [];
		this.logInCallbacks = [];
	};

	sessions.prototype = {
		getClientKey: function() {
			return (ipCookie('ironholdSession')) ? ipCookie('ironholdSession').clientKey : false;
		},

		getUsername: function() {
			return (ipCookie('ironholdSession')) ? ipCookie('ironholdSession').username : false;
		},

		getAuthdata: function() {
	                return (ipCookie('ironholdSession')) ? ipCookie('ironholdSession').authdata : false;
        },

		onLogOut: function(callback) {
			this.logOutCallbacks.push(callback);
		},

		onLogIn: function(callback) {
			this.logInCallbacks.push(callback);
		},

		logIn: function(clientKey, username, password) {
			loggedIn = true;
			ipCookie(
				'ironholdSession', 
				JSON.stringify({
					'session': null /* REPLACE THIS WITH A SESSION KEY LATER */,
					'clientKey': clientKey,
					'username': username,
					'authdata': Base64.encode(clientKey + "/" + username + ':' + password),
				}),
				{expires: 99}
			);
    			angular.forEach(this.logInCallbacks, function(callback, index) {
				callback();
			});
		},

		logOut: function() {
			loggedIn = false;
			ipCookie.remove('ironholdSession');
			document.execCommand("ClearAuthenticationCache");
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

ironholdApp.factory('messagesService', function(Restangular, logInService) {
	var results = false;
	if (logInService.getAuthdata()) {
		results = Restangular.one("messages");
	}
	return (results) ? results : false;
});

ironholdApp.factory('usersService', function(Restangular, logInService) {
	var results = false;
	if (logInService.getAuthdata()) {
		results = Restangular.one("users");
	}
	return (results) ? results : false;
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

ironholdApp.factory('Base64', function() {
    var keyStr = 'ABCDEFGHIJKLMNOP' +
        'QRSTUVWXYZabcdef' +
        'ghijklmnopqrstuv' +
        'wxyz0123456789+/' +
        '=';
    return {
        encode: function (input) {
            var output = "";
            var chr1, chr2, chr3 = "";
            var enc1, enc2, enc3, enc4 = "";
            var i = 0;

            do {
                chr1 = input.charCodeAt(i++);
                chr2 = input.charCodeAt(i++);
                chr3 = input.charCodeAt(i++);

                enc1 = chr1 >> 2;
                enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                enc4 = chr3 & 63;

                if (isNaN(chr2)) {
                    enc3 = enc4 = 64;
                } else if (isNaN(chr3)) {
                    enc4 = 64;
                }

                output = output +
                    keyStr.charAt(enc1) +
                    keyStr.charAt(enc2) +
                    keyStr.charAt(enc3) +
                    keyStr.charAt(enc4);
                chr1 = chr2 = chr3 = "";
                enc1 = enc2 = enc3 = enc4 = "";
            } while (i < input.length);

            return output;
        },

        decode: function (input) {
            var output = "";
            var chr1, chr2, chr3 = "";
            var enc1, enc2, enc3, enc4 = "";
            var i = 0;

            // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
            var base64test = /[^A-Za-z0-9\+\/\=]/g;
            if (base64test.exec(input)) {
                alert("There were invalid base64 characters in the input text.\n" +
                    "Valid base64 characters are A-Z, a-z, 0-9, '+', '/',and '='\n" +
                    "Expect errors in decoding.");
            }
            input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

            do {
                enc1 = keyStr.indexOf(input.charAt(i++));
                enc2 = keyStr.indexOf(input.charAt(i++));
                enc3 = keyStr.indexOf(input.charAt(i++));
                enc4 = keyStr.indexOf(input.charAt(i++));

                chr1 = (enc1 << 2) | (enc2 >> 4);
                chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                chr3 = ((enc3 & 3) << 6) | enc4;

                output = output + String.fromCharCode(chr1);

                if (enc3 != 64) {
                    output = output + String.fromCharCode(chr2);
                }
                if (enc4 != 64) {
                    output = output + String.fromCharCode(chr3);
                }

                chr1 = chr2 = chr3 = "";
                enc1 = enc2 = enc3 = enc4 = "";

            } while (i < input.length);

            return output;
        }
    };
});

ironholdApp.directive('wireframe', function() {
	return {
		scope: true,
		restrict: 'ACE',
		templateUrl: 'views/Wireframe.html'
	}
});

ironholdApp.directive('searchQuery', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/Searchbar.html'
	}
});

ironholdApp.directive('searchbar', function() {
	return {
		scope: true,
		restrict: 'ACE',
		controller: 'SearchbarController',
		templateUrl: 'views/SearchbarPanel.html'
	}
});

ironholdApp.directive('resultDetail', function() {
	return {
		scope: true,
		restrict: 'ACE',
		controller: 'SingleResultDisplayController',
		templateUrl: 'views/SingleResultPanel.html'
	}
});

ironholdApp.directive('resultsFeed', function() {
	return {
		scope: true,
		restrict: 'ACE',
		controller: 'MultipleResultDisplayController',
		templateUrl: 'views/MultipleResultPanel.html'
	}
});

ironholdApp.directive('facetCollection', function() {
	return {
		scope: true,
		restrict: 'ACE',
		controller: 'FacetController',
		templateUrl: 'views/FacetPanel.html'
	}
});

ironholdApp.directive('filterCollection', function() {
	return {
		scope: true,
		restrict: 'ACE',
		controller: 'FilterController',
		templateUrl: 'views/FilterPanel.html'
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

ironholdApp.filter('to_trusted', ['$sce', function($sce){
        return function(text) {
            return $sce.trustAsHtml(text);
        };
}]);

Date.prototype.getDayName = function() {
	var d = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
	return d[this.getDay()];
}

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
