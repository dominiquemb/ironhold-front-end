(function () {
   'use strict';


ironholdApp.directive('resultsFeed', function() {
        return {
                scope: true,
                restrict: 'ACE',
                controller: 'MultipleResultDisplayController',
                templateUrl: 'views/MultipleResultPanel.html'
        };
});
/*
ironholdApp.directive('clientKey', function() {
	return {
		restrict: 'ACE',
		scope: false,
                controller: 'LoginController',
		link: function(scope, elem) {
			var genericSubs = ['rq6', 'ih650ww001'],
			sub = null, 
			matchingSub = false;

    			scope.subdomain = document.domain.split('.')[0];
			
			for (sub in genericSubs) {
				if (scope.subdomain === sub) {
					matchingSub = true;
				}
			}

			if (!matchingSub) {
				$(elem).val(scope.subdomain);
				scope.mainLogin.clientKey = scope.subdomain;
				$(elem).attr('disabled', 'true');
			}
		}
	};
});

ironholdApp.directive('username', function() {
	return {
		restrict: 'ACE',
		scope: false,
                controller: 'LoginController',
		link: function(scope, elem) {
			var delimiters = ['/', '@', '\\'],
			usernameval = null,
			d = null,
			clientkeyval = null;

			scope.$watch(function() {
				return	$(elem).val();
			}, function(newval, oldval) {
				for (d in delimiters) {
					if (newval.indexOf(delimiters[d]) !== -1) {
						clientkeyval = newval.split(delimiters[d])[0];
					}
				}

				if (clientkeyval) {
					$('.client-key').attr('disabled', 'false');
					$('.client-key').val(clientkeyval);
					scope.mainLogin.clientKey.$modelValue = clientkeyval;
					$('.client-key').trigger('input');
					$('.client-key').attr('disabled', 'true');
					$(elem).val('');
					clientkeyval = null;
				}
			});
		}
	};
});
*/
ironholdApp.directive('clearForm', function() {
       return {
               scope: {
               },
               restrict: 'ACE',
               link: function(scope, elem, attrs) {
                    /*jshint unused:false*/
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
       };
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
                        /*jshint unused:false*/
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
        };
});

ironholdApp.directive('collapsible', function() {
        return {
/*              scope: {
                        'collapsedArrow': '@',
                        'uncollapsedArrow': '@'
                },
*/
                scope: true,
                restrict: 'ACE',
                link: function(scope, elem, attrs) {
                        /*jshint unused:false*/
                        var arrow = $(elem).find('.collapse-trigger');
                        if ($(elem).hasClass('collapsed')) {
                                arrow.addClass(scope.collapsedArrow);
                        } else {
                        arrow.addClass(scope.uncollapsedArrow);
                        }
                        arrow.on('click', function() {
                                $(elem).toggleClass('collapsed');
                                arrow.toggleClass(scope.collapsedArrow + ' ' + scope.uncollapsedArrow);
                        });
                }
        };
});

ironholdApp.directive('facetCollection', function() {
        return {
                scope: true,
                restrict: 'ACE',
                controller: 'FacetController',
                templateUrl: 'views/FacetPanel.html'
        };
});

ironholdApp.directive('filterCollection', function() {
        return {
                scope: true,
                restrict: 'ACE',
                controller: 'FilterController',
                templateUrl: 'views/FilterPanel.html'
        };
});

ironholdApp.directive('wireframe', function() {
        return {
                scope: true,
                restrict: 'ACE',
                templateUrl: 'views/Wireframe.html'
        };
});

ironholdApp.directive('searchQuery', function() {
        return {
                restrict: 'ACE',
                templateUrl: 'views/Searchbar.html'
        };
});

ironholdApp.directive('searchbar', function() {
        return {
                scope: true,
                restrict: 'ACE',
                controller: 'SearchbarController',
                templateUrl: 'views/SearchbarPanel.html'
        };
});

ironholdApp.directive('resultDetail', function() {
        return {
                scope: true,
                restrict: 'ACE',
                controller: 'SingleResultDisplayController',
                templateUrl: 'views/SingleResultPanel.html'
        };
});

ironholdApp.directive('pagination', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/Pagination.html',
		controller: 'PaginationController'
	};
});

ironholdApp.directive('userActionsPanel', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/UserActionsPanel.html',
		controller: 'UserActionsController'
	};
});

ironholdApp.directive('attachmentsView', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/PreviewTabs/AttachmentsView.html'
	};
});

ironholdApp.directive('messageHeader', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/PreviewTabs/MessageHeader.html'
	};
});

ironholdApp.directive('body', function() {
	return {
		restrict: "ACE",
		controller: 'BodyController'
	};
});

ironholdApp.directive('errors', function() {
	return {
		restrict: 'ACE',
		controller: 'ErrorsController'
	};
});

ironholdApp.directive('sortingPanel', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/SortingPanel.html',
	};
});

ironholdApp.directive('footer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/Footer.html',
		controller: 'FooterController'
	};
});

ironholdApp.directive('searchResultsList', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/SearchResultsList.html'
	};
});

ironholdApp.directive('usersResultsList', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/UsersResultsList.html'
	};
});

ironholdApp.directive('searchTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'SearchController'
	};
});

ironholdApp.directive('usersTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'UsersController'
	};
});

ironholdApp.directive('usersSearchTab', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'UsersSearchController'
	};
});

ironholdApp.directive('messageSearchTab', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'MessageSearchController'
	};
});

ironholdApp.directive('controlbar', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/PreviewTabs/Controlbar.html'
	};
});

ironholdApp.directive('discoveryTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'DiscoveryController'
	};
});

ironholdApp.directive('foldersTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'FoldersController'
	};
});

ironholdApp.directive('logsTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'LogsController'
	};
});

ironholdApp.directive('settingsTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'SettingsController'
	};
});

}());
