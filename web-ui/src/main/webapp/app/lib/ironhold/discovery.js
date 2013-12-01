$(window).load(function() {
	$('.result').on('click', function() {
		var resultEntry = $(this);

		if (resultEntry.hasClass('active-single')) {
			resultEntry.removeClass('active-single');
		}
		else {
			$.each($('.result'), function(index, elem) {
				$(elem).removeClass('active-single');
			});
			resultEntry.addClass('active-single');
		}
	});

	$('.checkmark').on('click', function(evt) {
		evt.preventDefault(true);
		evt.stopPropagation();
		var resultEntry = $(this).parents('.result');
		
		if (resultEntry.hasClass('active-multi')) {
			resultEntry.removeClass('active-multi');
		}
		else {
			resultEntry.addClass('active-multi');
		}
	});

	$('.filter').on('click', function() {
		var filter = $(this);

		if (filter.hasClass('active')) {
			filter.removeClass('active');
		}
		else {
			filter.addClass('active');
		}
	});

	$('.msglist .toggle').on('click', function() {
		$('.msglist').toggleClass('docked');
	});

	var DiscoveryTab = function() {
	};

	DiscoveryTab.prototype = {
		resizeMsgBody: function() {
			var msgviewBodyHeight = $('.msgview_bottom').offset().top - $('.msgview_middle').offset().top;
			$('.msgview_middle').css('height', msgviewBodyHeight);
			return $('.msgview_middle');
		}
	};

	var dtab = new DiscoveryTab();

	$('.msgview_bottom .expandable-toggle').on('click', function() {
		$(this).parents('.msgview_bottom').toggleClass('attachments-hidden');
		setTimeout(function() {
			dtab.resizeMsgBody().data('jsp').reinitialise();
			$('.scrollbar-hidden').data('jsp').reinitialise();
		}, 300);
	});

	$(window).on('resize', function() {
		dtab.resizeMsgBody().data('jsp').reinitialise();
		$('.scrollbar-hidden').data('jsp').reinitialise();
	});

	setTimeout(function() {
		dtab.resizeMsgBody();
	}, 0);

	$('.result').on('classAdded', function(evt, classes) {
		if (classes === "collapsed") {
			var resultEntry = $(this).find('.widget-main');
			$(this).find('.widget-main').textResize({
				charPxlWidth: 7,
				fontSize: 11,
				desiredHeight: 11,
				trailingDots: true,
				containerWidth: resultEntry.width()
			});
		}
	});

	$('.result').on('classRemoved', function(evt, classes) {
		if (classes === "collapsed") {
			var resultEntry = $(this).find('.widget-main'),
			widgetBox = $(this).find('.widget-box');
			resultEntry.textResize({
				text: resultEntry.data('textResizeOriginalText'),
				charPxlWidth: 7,
				fontSize: 11,
				desiredHeight: 44,
				containerWidth: widgetBox.width()
			});
		}
	});

	$('.msglist').on('toggleDock', function() {
		if ( $(this).toggleClass('docked').hasClass('docked') === false ) {
			$('.message-list-container').css('left', '237px');
		}
		
	});

	$.each($('.result'), function(index, elem) {
		var resultEntry = $(this).find('.widget-main'),
		widgetBox = $(this).find('.widget-box');
		resultEntry.textResize({
			charPxlWidth: 7,
			fontSize: 11,
			desiredHeight: 44,
			containerWidth: widgetBox.width()
		});
	});

	$(".main").splitter({
		type: "v",
		outline: true,
		minLeft: 530,
		minRight: 310,
		resizeToWidth: true,
		cookie: "docksplitter",
		accessKey: 'I'  // Alt-Shift-I in FF/IE
	});
});
