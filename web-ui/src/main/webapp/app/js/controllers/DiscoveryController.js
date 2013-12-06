'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $sce) {
    $scope.showSearchResults = false;
    $scope.showMessage = false;
  
    $scope.initCustomScrollbars = function(selector) {
        setTimeout(function() {
                $(selector).jScrollPane({
                        verticalArrowPositions: 'split',
                        horizontalArrowPositions: 'split'
                });
        }, 0);
    };

    $scope.reinitScrollbars = function() {
	$('.scrollbar-hidden').data('jsp').reinitialise();
    }

    $scope.toggleActiveState = function(item) {
	item.selected = !item.selected;
    }

    $scope.toggleCollapse = function(item) {
	item.collapsed = !item.collapsed;
    }

    $scope.isSearchResultsVisible = function() {
        return $scope.showSearchResults;
    }

    $scope.isMessageVisible = function() {
        return $scope.showMessage;
    }

    $scope.hasAttachmentHighlight = function(message) {
        return message.attachmentWithHighlights.length > 0;
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

    $scope.selectMessage = function(message) {
        $scope.currentMessage = message;
        $scope.showMessage = true;
    }

    $scope.search = function () {
        $scope.showSearchResults = true;
        $scope.searchMatches = Math.ceil(Math.random()*1000);
        $scope.searchTime = Math.ceil(Math.random()*100);
	
	$scope.initCustomScrollbars('.scrollbar-hidden');

        $scope.facets =[
                                      {
                                         "name" :
                                            {
                                               "value" : "from",
					       "collapsed": false,
                                               "label" : "From by name:",
                                               "order" : 1
                                            },
                                         "valueMap" :
                                            [
                                               {
                                                  "label" : "Thelma Cline",
                                                  "value" : 28,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Melissa Knight",
                                                  "value" : 28,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Tyrone Ayala",
                                                  "value" : 26,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Nathan Thompson",
                                                  "value" : 25,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Trevor Holman",
                                                  "value" : 24,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Russell Hobbs",
                                                  "value" : 23,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Wyatt Santiago",
                                                  "value" : 22,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Virginia Pitts",
                                                  "value" : 22,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Todd Gay",
                                                  "value" : 22,
						  "selected": false
                                               },
                                               {
                                                  "label" : "Ron Chambers",
                                                  "value" : 22,
						  "selected": false
                                               }
                                            ]
                                      },
                                      {
                                         "name" :
                                            {
                                               "value" : "from_domain",
                                               "label" : "From by domain:",
					       "collapsed": false,
                                               "order" : 2
                                            },
                                         "valueMap" :
                                            [
                                               {
                                                  "label" : "yah00.us",
                                                  "value" : 139,
						  "selected": false
                                               },
                                               {
                                                  "label" : "ma1l2u.net",
                                                  "value" : 98,
						  "selected": false
                                               },
                                               {
                                                  "label" : "somema1l.com",
                                                  "value" : 82,
						  "selected": false
                                               },
                                               {
                                                  "label" : "yah00.co.uk",
                                                  "value" : 74,
						  "selected": false
                                               },
                                               {
                                                  "label" : "somema1l.us",
                                                  "value" : 66,
						  "selected": false
                                               },
                                               {
                                                  "label" : "everyma1l.co.uk",
                                                  "value" : 62,
						  "selected": false
                                               },
                                               {
                                                  "label" : "somema1l.org",
						  "selected": false,
                                                  "value" : 61
                                               },
                                               {
                                                  "label" : "ma1lbox.com",
						  "selected": false,
                                                  "value" : 59
                                               },
                                               {
                                                  "label" : "yah00.biz",
						  "selected": false,
                                                  "value" : 58
                                               },
                                               {
                                                  "label" : "ma1l2u.us",
						  "selected": false,
                                                  "value" : 53
                                               }
                                            ]
                                      },
                                      {
                                         "name" :
                                            {
                                               "value" : "to",
                                               "label" : "To by name:",
					       "collapsed": false,
                                               "order" : 3
                                            },
                                         "valueMap" :
                                            [
                                               {
                                                  "label" : "Ruth Kinney",
						  "selected": false,
                                                  "value" : 46
                                               },
                                               {
                                                  "label" : "Tammy Chaney",
						  "selected": false,
                                                  "value" : 45
                                               },
                                               {
                                                  "label" : "Tina Chang",
						  "selected": false,
                                                  "value" : 43
                                               },
                                               {
                                                  "label" : "Thelma Gaines",
						  "selected": false,
                                                  "value" : 41
                                               },
                                               {
                                                  "label" : "Tammy Harvey",
						  "selected": false,
                                                  "value" : 39
                                               },
                                               {
                                                  "label" : "Russell Hobbs",
						  "selected": false,
                                                  "value" : 39
                                               },
                                               {
                                                  "label" : "Virginia Pitts",
						  "selected": false,
                                                  "value" : 38
                                               },
                                               {
                                                  "label" : "Wyatt Santiago",
						  "selected": false,
                                                  "value" : 37
                                               },
                                               {
                                                  "label" : "Lynn Robles",
						  "selected": false,
                                                  "value" : 37
                                               },
                                               {
						  "selected": false,
                                                  "label" : "Terry Roberson",
                                                  "value" : 35
                                               }
                                            ]
                                      },
                                      {
                                         "name" :
                                            {
                                               "value" : "to_domain",
                                               "label" : "To by domain:",
					       "collapsed": true,
                                               "order" : 4
                                            },
                                         "valueMap" :
                                            [
                                               {
                                                  "label" : "yah00.us",
						  "selected": false,
                                                  "value" : 337
                                               },
                                               {
                                                  "label" : "ma1l2u.net",
						  "selected": false,
                                                  "value" : 242
                                               },
                                               {
                                                  "label" : "ma1lbox.com",
						  "selected": false,
                                                  "value" : 186
                                               },
                                               {
                                                  "label" : "somema1l.com",
						  "selected": false,
                                                  "value" : 158
                                               },
                                               {
                                                  "label" : "everyma1l.net",
						  "selected": false,
                                                  "value" : 150
                                               },
                                               {
                                                  "label" : "everyma1l.co.uk",
						  "selected": false,
                                                  "value" : 143
                                               },
                                               {
                                                  "label" : "b1zmail.net",
						  "selected": false,
                                                  "value" : 139
                                               },
                                               {
                                                  "label" : "yah00.co.uk",
						  "selected": false,
                                                  "value" : 136
                                               },
                                               {
                                                  "label" : "somema1l.org",
						  "selected": false,
                                                  "value" : 131
                                               },
                                               {
                                                  "label" : "somema1l.us",
						  "selected": false,
                                                  "value" : 123
                                               }
                                            ]
                                      },
                                      {
                                         "name" :
                                            {
                                               "value" : "date",
                                               "label" : "Year:",
					       "collapsed": true,
                                               "order" : 5
                                            },
                                         "valueMap" :
                                            [
                                               {
                                                  "label" : "2010",
						  "selected": false,
                                                  "value" : 101
                                               },
                                               {
                                                  "label" : "2009",
						  "selected": false,
                                                  "value" : 169
                                               },
                                               {
                                                  "label" : "2008",
						  "selected": false,
                                                  "value" : 235
                                               },
                                               {
                                                  "label" : "2007",
						  "selected": false,
                                                  "value" : 191
                                               },
                                               {
                                                  "label" : "2006",
						  "selected": false,
                                                  "value" : 192
                                               },
                                               {
						  "selected": false,
                                                  "label" : "2005",
                                                  "value" : 189
                                               },
                                               {
						  "selected": false,
                                                  "label" : "2004",
                                                  "value" : 187
                                               },
                                               {
						  "selected": false,
                                                  "label" : "2003",
                                                  "value" : 179
                                               },
                                               {
						  "selected": false,
                                                  "label" : "2002",
                                                  "value" : 190
                                               },
                                               {
						  "selected": false,
                                                  "label" : "2001",
                                                  "value" : 182
                                               }
                                            ]
                                      },
                                      {
                                         "name" :
                                            {
                                               "value" : "file_ext",
                                               "label" : "Attachment file type:",
					       "collapsed": true,
                                               "order" : 6
                                            },
                                         "valueMap" :
                                            [
                                               {
						  "selected": false,
                                                  "label" : "pdf",
                                                  "value" : 1891
                                               }
                                            ]
                                      },
                                      {
                                         "name" :
                                            {
                                               "value" : "msg_type",
                                               "label" : "Message type:",
					       "collapsed": true,
                                               "order" : 7
                                            },
                                         "valueMap" :
                                            [
                                               {
						  "selected": false,
                                                  "label" : "email",
                                                  "value" : 1891
                                               }
                                            ]
                                      }
                                   ];

        $scope.messages =                        [
                                                                {
                                                                   "bodyWithHighlights" : "\"Good! �Splendid! �_Now_ we're all right and safe! Did you tell Aunty?\" I was going to say yes; but she chipped in and says: �\"About what, Sid?\" \"Why, about the way the whole thing was done.\" \"What whole thing?\" \"Why, _the_ whole thing. �There ain't but one; how we set the runaway nigger free�me and Tom.\" \n",
                                                                   "subjectWithHighlights" : "\"Good!  Splendid!  _Now_ we're all right...",
                                                                   "attachmentWithHighlights" : "for a sheep worth 4 pence and a <span class=\"blue-hilite\">dog</span> worth a penny,\nand C kill the <span class=\"blue-hilite\">dog</span> before delivery, because bitten",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<370509511.127199.1385256121997.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "\"Good!  Splendid!  _Now_ we're all right...",
                                                                         "messageDate" : "04/27/2006",
                                                                         "year" : "2006",
                                                                         "monthDay" : "0427",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Gloria Witt",
                                                                               "address" : "tgalloway17@yah00.co.uk",
                                                                               "domain" : "yah00.co.uk"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Darla Martin",
                                                                                  "address" : "inwant91@somema1l.org",
                                                                                  "domain" : "somema1l.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Eva Parsons",
                                                                                  "address" : "ftran@yah00.co.uk",
                                                                                  "domain" : "yah00.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Tina Chang",
                                                                                  "address" : "towe@everyma1l.co.uk",
                                                                                  "domain" : "everyma1l.co.uk"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Rachel Johnston",
                                                                                  "address" : "automaticallyin@ma1lbox.net",
                                                                                  "domain" : "ma1lbox.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Austin Schwartz",
                                                                                  "address" : "tschwartz61@hotma1l.com",
                                                                                  "domain" : "hotma1l.com"
                                                                               },
                                                                               {
                                                                                  "name" : "Eva Parsons",
                                                                                  "address" : "ftran@yah00.co.uk",
                                                                                  "domain" : "yah00.co.uk"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Edward Mcconnell",
                                                                                  "address" : "tooksmoke@somema1l.biz",
                                                                                  "domain" : "somema1l.biz"
                                                                               },
                                                                               {
                                                                                  "name" : "Father Hood",
                                                                                  "address" : "gbarnett@somema1l.org",
                                                                                  "domain" : "somema1l.org"
                                                                               }
                                                                            ],
                                                                         "size" : 4040,
                                                                         "body" : "\"Good! �Splendid! �_Now_ we're all right and safe! Did you tell Aunty?\" I was going to say yes; but she chipped in and says: �\"About what, Sid?\" \"Why, about the way the whole thing was done.\" \"What whole thing?\" \"Why, _the_ whole thing. �There ain't but one; how we set the runaway nigger free�me and Tom.\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 1940,
                                                                                  "creationTime" : "04/27/2006",
                                                                                  "modificationTime" : "04/27/2006",
                                                                                  "fileName" : "MonroeCafe.pdf",
                                                                                  "body" : "\n\"Do you know the multiplication table?\"\n \n\n\"I wit not what ye refer to.\"\n \n\n\"How much is 9 times 6?\"\n \n\n\"It is a mystery that is hidden from me by reason that the emergency\nrequiring the fathoming of it hath not in my life-days occurred,\nand so, not having no need to know this thing, I abide barren\nof the knowledge.\"\n \n\n\"If A trade a barrel of onions to B, worth 2 pence the bushel,\nin exchange for a sheep worth 4 pence and a dog worth a penny,\nand C kill the dog before delivery, because bitten by the same,\nwho mistook him for D, what sum is still due to A from B, and\nwhich party pays for the dog, C or D, and who gets the money?\nIf A, is the penny sufficient, or may he claim consequential damages\nin the form of additional money to represent the possible profit\nwhich might have inured from the dog, and classifiable as earned\nincrement, that is to say, usufruct?\"\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=MonroeCafe.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "3 KB",
                                                                         "age" : "7 years and 7 months ago",
                                                                         "formattedMessageDate" : "Thu, 27 Apr 2006 00:00:00 EDT"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "for a sheep worth 4 pence and a <span class=\"blue-hilite\">dog</span> worth a penny, and C kill the <span class=\"blue-hilite\">dog</span> before delivery, because bitten ... still due to A from B, and which party pays for the <span class=\"blue-hilite\">dog</span>, C or D, and who gets the money? If A, is the penny ... possible profit which might have inured from the <span class=\"blue-hilite\">dog</span>, and classifiable as earned increment, that is to",
                                                                   "subjectWithHighlights" : "His face flushed indignantly, and he fired...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<63228029.113963.1385256027294.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "His face flushed indignantly, and he fired...",
                                                                         "messageDate" : "10/01/2010",
                                                                         "year" : "2010",
                                                                         "monthDay" : "1001",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Tina Chang",
                                                                               "address" : "towe@everyma1l.co.uk",
                                                                               "domain" : "everyma1l.co.uk"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Alex Higgins",
                                                                                  "address" : "hstafford@b1zmail.net",
                                                                                  "domain" : "b1zmail.net"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Donald Glover",
                                                                                  "address" : "stownsend@yah00.net",
                                                                                  "domain" : "yah00.net"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Terry Roberson",
                                                                                  "address" : "suspenseof@ma1l2u.net",
                                                                                  "domain" : "ma1l2u.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Kim Carver",
                                                                                  "address" : "ddaniels@b1zmail.org",
                                                                                  "domain" : "b1zmail.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Judy Burks",
                                                                                  "address" : "alwaysit@ma1l2u.us",
                                                                                  "domain" : "ma1l2u.us"
                                                                               }
                                                                            ],
                                                                         "size" : 5728,
                                                                         "body" : "His face flushed indignantly, and he fired this at me: \"Takest me for a clerk? I trow I am not of a blood that--\" \"Answer the question!\" He crowded his wrath down and made out to answer \"No.\" \"Can you write?\" He wanted to resent this, too, but I said: \"You will confine yourself to the questions, and make no comments. You are not here to air your blood or your graces, and nothing of the sort will be permitted. Can you write?\" \"Do you know the multiplication table?\" \"I wit not what ye refer to.\" \"How much is 9 times 6?\" \"It is a mystery that is hidden from me by reason that the emergency requiring the fathoming of it hath not in my life-days occurred, and so, not having no need to know this thing, I abide barren of the knowledge.\" \"If A trade a barrel of onions to B, worth 2 pence the bushel, in exchange for a sheep worth 4 pence and a dog worth a penny, and C kill the dog before delivery, because bitten by the same, who mistook him for D, what sum is still due to A from B, and which party pays for the dog, C or D, and who gets the money? If A, is the penny sufficient, or may he claim consequential damages in the form of additional money to represent the possible profit which might have inured from the dog, and classifiable as earned increment, that is to say, usufruct?\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 1842,
                                                                                  "creationTime" : "10/01/2010",
                                                                                  "modificationTime" : "10/01/2010",
                                                                                  "fileName" : "HazlehurstAccounting.pdf",
                                                                                  "body" : "\nMore than two hours passed and Gerasim took the liberty of making a\nslight noise at the door to attract his attention, but Pierre did not\nhear him.\n \n\n\"Is the cabman to be discharged, your honor?\"\n \n\n\"Oh yes!\" said Pierre, rousing himself and rising hurriedly. \"Look\nhere,\" he added, taking Gerasim by a button of his coat and looking down\nat the old man with moist, shining, and ecstatic eyes, \"I say, do you\nknow that there is going to be a battle tomorrow?\"\n \n\n\"We heard so,\" replied the man.\n \n\n\"I beg you not to tell anyone who I am, and to do what I ask you.\"\n \n\n\"Yes, your excellency,\" replied Gerasim. \"Will you have something to\neat?\"\n \n\n\"No, but I want something else. I want peasant clothes and a pistol,\"\nsaid Pierre, unexpectedly blushing.\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=HazlehurstAccounting.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "5 KB",
                                                                         "age" : "3 years and 2 months ago",
                                                                         "formattedMessageDate" : "Fri, 1 Oct 2010 00:00:00 EDT"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "Caught in the Act Tom Astonishes the School Examination Evening The Master's Dilemma The School House Happy for Two Days Enjoying the Vacation \n",
                                                                   "subjectWithHighlights" : "Caught in the Act  Tom Astonishes the School  Examination...",
                                                                   "attachmentWithHighlights" : "spoken of\nhim as \"Harbison's Bull,\" but a son or a <span class=\"blue-hilite\">dog</span> of that name was \"Bull\nHarbison.\"]\n \n\n\"Oh, that's",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<1266457719.101543.1385255942251.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "Caught in the Act  Tom Astonishes the School  Examination...",
                                                                         "messageDate" : "11/24/2009",
                                                                         "year" : "2009",
                                                                         "monthDay" : "1124",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Dan White",
                                                                               "address" : "churst@hotma1l.biz",
                                                                               "domain" : "hotma1l.biz"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Ricky Spence",
                                                                                  "address" : "kalbert@b1zmail.com",
                                                                                  "domain" : "b1zmail.com"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Lynn Robles",
                                                                                  "address" : "lreilly@ma1l2u.net",
                                                                                  "domain" : "ma1l2u.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Clara Frazier",
                                                                                  "address" : "mbrock@hotma1l.org",
                                                                                  "domain" : "hotma1l.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Ellen Tran",
                                                                                  "address" : "lotsred@hotma1l.org",
                                                                                  "domain" : "hotma1l.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Kim Carver",
                                                                                  "address" : "ddaniels@b1zmail.org",
                                                                                  "domain" : "b1zmail.org"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Leonard Baldwin",
                                                                                  "address" : "vrosa@gma1l.org",
                                                                                  "domain" : "gma1l.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Clara Frazier",
                                                                                  "address" : "mbrock@hotma1l.org",
                                                                                  "domain" : "hotma1l.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Joel Michael",
                                                                                  "address" : "lcochran5@ma1lbox.com",
                                                                                  "domain" : "ma1lbox.com"
                                                                               },
                                                                               {
                                                                                  "name" : "Mother Chaney",
                                                                                  "address" : "jgrimes@somema1l.net",
                                                                                  "domain" : "somema1l.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Ronda Reeves",
                                                                                  "address" : "temerson@gma1l.co.uk",
                                                                                  "domain" : "gma1l.co.uk"
                                                                               }
                                                                            ],
                                                                         "size" : 3442,
                                                                         "body" : "Caught in the Act Tom Astonishes the School Examination Evening The Master's Dilemma The School House Happy for Two Days Enjoying the Vacation \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 1646,
                                                                                  "creationTime" : "11/24/2009",
                                                                                  "modificationTime" : "11/24/2009",
                                                                                  "fileName" : "PeachtreeCityWebsiteMotors.pdf",
                                                                                  "body" : "\n\"Oh, lordy, I'm thankful!\" whispered Tom. \"I know his voice. It's Bull\nHarbison.\" *\n \n\n[* If Mr. Harbison owned a slave named Bull, Tom would have spoken of\nhim as \"Harbison's Bull,\" but a son or a dog of that name was \"Bull\nHarbison.\"]\n \n\n\"Oh, that's good--I tell you, Tom, I was most scared to death; I'd a bet\nanything it was a _stray_ dog.\"\n \n\nThe dog howled again. The boys' hearts sank once more.\n \n\n\"Oh, my! that ain't no Bull Harbison!\" whispered Huckleberry. \"_Do_,\nTom!\"\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=PeachtreeCityWebsiteMotors.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "3 KB",
                                                                         "age" : "4 years ago",
                                                                         "formattedMessageDate" : "Tue, 24 Nov 2009 00:00:00 EST"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "for a sheep worth 4 pence and a <span class=\"blue-hilite\">dog</span> worth a penny, and C kill the <span class=\"blue-hilite\">dog</span> before delivery, because bitten ... still due to A from B, and which party pays for the <span class=\"blue-hilite\">dog</span>, C or D, and who gets the money? If A, is the penny ... possible profit which might have inured from the <span class=\"blue-hilite\">dog</span>, and classifiable as earned increment, that is to ... ducts of thought. Wherefore I beseech you let the <span class=\"blue-hilite\">dog</span> and the onions and these people of the strange and",
                                                                   "subjectWithHighlights" : "\"It is a mystery that is hidden from me by...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<431871714.63680.1385255692180.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "\"It is a mystery that is hidden from me by...",
                                                                         "messageDate" : "03/06/2004",
                                                                         "year" : "2004",
                                                                         "monthDay" : "0306",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Alice Watts",
                                                                               "address" : "visionswe55@b1zmail.net",
                                                                               "domain" : "b1zmail.net"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Judy Burks",
                                                                                  "address" : "alwaysit@ma1l2u.us",
                                                                                  "domain" : "ma1l2u.us"
                                                                               },
                                                                               {
                                                                                  "name" : "Jimmy Tanner",
                                                                                  "address" : "jdunn@ma1l2u.us",
                                                                                  "domain" : "ma1l2u.us"
                                                                               },
                                                                               {
                                                                                  "name" : "Jonathan Hurst",
                                                                                  "address" : "lpeters68@b1zmail.biz",
                                                                                  "domain" : "b1zmail.biz"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Kim Carver",
                                                                                  "address" : "ddaniels@b1zmail.org",
                                                                                  "domain" : "b1zmail.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Ron Chambers",
                                                                                  "address" : "isuntil44@ma1l2u.co.uk",
                                                                                  "domain" : "ma1l2u.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Erika Finley",
                                                                                  "address" : "numberswhile@hotma1l.biz",
                                                                                  "domain" : "hotma1l.biz"
                                                                               },
                                                                               {
                                                                                  "name" : "Rachel Johnston",
                                                                                  "address" : "automaticallyin@ma1lbox.net",
                                                                                  "domain" : "ma1lbox.net"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Ricky Spence",
                                                                                  "address" : "kalbert@b1zmail.com",
                                                                                  "domain" : "b1zmail.com"
                                                                               },
                                                                               {
                                                                                  "name" : "Sue Dorsey",
                                                                                  "address" : "havewas@ma1lbox.net",
                                                                                  "domain" : "ma1lbox.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Lois Mills",
                                                                                  "address" : "probablyisland@everyma1l.co.uk",
                                                                                  "domain" : "everyma1l.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Clarence Walker",
                                                                                  "address" : "obaxter30@ma1l2u.net",
                                                                                  "domain" : "ma1l2u.net"
                                                                               }
                                                                            ],
                                                                         "size" : 7127,
                                                                         "body" : "\"It is a mystery that is hidden from me by reason that the emergency requiring the fathoming of it hath not in my life-days occurred, and so, not having no need to know this thing, I abide barren of the knowledge.\" \"If A trade a barrel of onions to B, worth 2 pence the bushel, in exchange for a sheep worth 4 pence and a dog worth a penny, and C kill the dog before delivery, because bitten by the same, who mistook him for D, what sum is still due to A from B, and which party pays for the dog, C or D, and who gets the money? If A, is the penny sufficient, or may he claim consequential damages in the form of additional money to represent the possible profit which might have inured from the dog, and classifiable as earned increment, that is to say, usufruct?\" \"Verily, in the all-wise and unknowable providence of God, who moveth in mysterious ways his wonders to perform, have I never heard the fellow to this question for confusion of the mind and congestion of the ducts of thought. Wherefore I beseech you let the dog and the onions and these people of the strange and godless names work out their several salvations from their piteous and wonderful difficulties without help of mine, for indeed their trouble is sufficient as it is, whereas an I tried to help I should but damage their cause the more and yet mayhap not live myself to see the desolation wrought.\" \"What do you know of the laws of attraction and gravitation?\" \"If there be such, mayhap his grace the king did promulgate them whilst that I lay sick about the beginning of the year and thereby failed to hear his proclamation.\" \"What do you know of the science of optics?\" \"I know of governors of places, and seneschals of castles, and sheriffs of counties, and many like small offices and titles of honor, but him you call the Science of Optics I have not heard of before; peradventure it is a new dignity.\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 1842,
                                                                                  "creationTime" : "03/06/2004",
                                                                                  "modificationTime" : "03/06/2004",
                                                                                  "fileName" : "PelhamBakery.pdf",
                                                                                  "body" : "\nEvery one had a Gun\n \n\nTom caught on a Splinter\n \n\nJim advises a Doctor\n \n\nUncle Silas in Danger\n \n\nOld Mrs. Hotchkiss\n \n\nAunt Sally talks to Huck\n \n\nTom Sawyer wounded\n \n\nThe Doctor speaks for Jim\n \n\nTom rose square up in Bed\n \n\n\"Hand out them Letters\"\n \n\nTom's Liberality\n \n\nIN this book a number of dialects are used, to wit:  the Missouri negro\ndialect; the extremest form of the backwoods Southwestern dialect; the\nordinary \"Pike County\" dialect; and four modified varieties of this\nlast. The shadings have not been done in a haphazard fashion, or by\nguesswork; but painstakingly, and with the trustworthy guidance and\nsupport of personal familiarity with these several forms of speech.\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=PelhamBakery.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "6 KB",
                                                                         "age" : "9 years and 8 months ago",
                                                                         "formattedMessageDate" : "Sat, 6 Mar 2004 00:00:00 EST"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "for a sheep worth 4 pence and a <span class=\"blue-hilite\">dog</span> worth a penny, and C kill the <span class=\"blue-hilite\">dog</span> before delivery, because bitten ... still due to A from B, and which party pays for the <span class=\"blue-hilite\">dog</span>, C or D, and who gets the money? If A, is the penny ... possible profit which might have inured from the <span class=\"blue-hilite\">dog</span>, and classifiable as earned increment, that is to ... ducts of thought. Wherefore I beseech you let the <span class=\"blue-hilite\">dog</span> and the onions and these people of the strange and",
                                                                   "subjectWithHighlights" : "\"You will confine yourself to the questions,...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<958863512.212216.1385256690684.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "\"You will confine yourself to the questions,...",
                                                                         "messageDate" : "03/20/2003",
                                                                         "year" : "2003",
                                                                         "monthDay" : "0320",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Darla Martin",
                                                                               "address" : "inwant91@somema1l.org",
                                                                               "domain" : "somema1l.org"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Bobbie Hardy",
                                                                                  "address" : "movedsuspense@b1zmail.co.uk",
                                                                                  "domain" : "b1zmail.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Ronda Reeves",
                                                                                  "address" : "temerson@gma1l.co.uk",
                                                                                  "domain" : "gma1l.co.uk"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Tammy Chaney",
                                                                                  "address" : "coldhandled@b1zmail.com",
                                                                                  "domain" : "b1zmail.com"
                                                                               },
                                                                               {
                                                                                  "name" : "Joshua Dalton",
                                                                                  "address" : "rhardin@yah00.biz",
                                                                                  "domain" : "yah00.biz"
                                                                               },
                                                                               {
                                                                                  "name" : "Russell Hobbs",
                                                                                  "address" : "sidekickwe67@somema1l.net",
                                                                                  "domain" : "somema1l.net"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Jimmy Tanner",
                                                                                  "address" : "jdunn@ma1l2u.us",
                                                                                  "domain" : "ma1l2u.us"
                                                                               },
                                                                               {
                                                                                  "name" : "Joel Michael",
                                                                                  "address" : "lcochran5@ma1lbox.com",
                                                                                  "domain" : "ma1lbox.com"
                                                                               }
                                                                            ],
                                                                         "size" : 10575,
                                                                         "body" : "\"You will confine yourself to the questions, and make no comments. You are not here to air your blood or your graces, and nothing of the sort will be permitted. Can you write?\" \"Do you know the multiplication table?\" \"I wit not what ye refer to.\" \"How much is 9 times 6?\" \"It is a mystery that is hidden from me by reason that the emergency requiring the fathoming of it hath not in my life-days occurred, and so, not having no need to know this thing, I abide barren of the knowledge.\" \"If A trade a barrel of onions to B, worth 2 pence the bushel, in exchange for a sheep worth 4 pence and a dog worth a penny, and C kill the dog before delivery, because bitten by the same, who mistook him for D, what sum is still due to A from B, and which party pays for the dog, C or D, and who gets the money? If A, is the penny sufficient, or may he claim consequential damages in the form of additional money to represent the possible profit which might have inured from the dog, and classifiable as earned increment, that is to say, usufruct?\" \"Verily, in the all-wise and unknowable providence of God, who moveth in mysterious ways his wonders to perform, have I never heard the fellow to this question for confusion of the mind and congestion of the ducts of thought. Wherefore I beseech you let the dog and the onions and these people of the strange and godless names work out their several salvations from their piteous and wonderful difficulties without help of mine, for indeed their trouble is sufficient as it is, whereas an I tried to help I should but damage their cause the more and yet mayhap not live myself to see the desolation wrought.\" \"What do you know of the laws of attraction and gravitation?\" \"If there be such, mayhap his grace the king did promulgate them whilst that I lay sick about the beginning of the year and thereby failed to hear his proclamation.\" \"What do you know of the science of optics?\" \"I know of governors of places, and seneschals of castles, and sheriffs of counties, and many like small offices and titles of honor, but him you call the Science of Optics I have not heard of before; peradventure it is a new dignity.\" \"Yes, in this country.\" Try to conceive of this mollusk gravely applying for an official position, of any kind under the sun! Why, he had all the earmarks of a typewriter copyist, if you leave out the disposition to contribute uninvited emendations of your grammar and punctuation. It was unaccountable that he didn't attempt a little help of that sort out of his majestic supply of incapacity for the job. But that didn't prove that he hadn't material in him for the disposition, it only proved that he wasn't a typewriter copyist yet. After nagging him a little more, I let the professors loose on him and they turned him inside out, on the line of scientific war, and found him empty, of course. He knew somewhat about the warfare of the time--bushwhacking around for ogres, and bull-fights in the tournament ring, and such things--but otherwise he was empty and useless. Then we took the other young noble in hand, and he was the first one's twin, for ignorance and incapacity. I delivered them into the hands of the chairman of the Board with the comfortable consciousness that their cake was dough. They were examined in the previous order of precedence. \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 2528,
                                                                                  "creationTime" : "03/20/2003",
                                                                                  "modificationTime" : "03/20/2003",
                                                                                  "fileName" : "MershonIndustries.pdf",
                                                                                  "body" : "\n\"Have you got one of them papers, Tom?\"\n \n\n\"Well then, how you going to find the marks?\"\n \n\n\"I don't want any marks. They always bury it under a ha'nted house or on\nan island, or under a dead tree that's got one limb sticking out. Well,\nwe've tried Jackson's Island a little, and we can try it again some\ntime; and there's the old ha'nted house up the Still-House branch, and\nthere's lots of dead-limb trees--dead loads of 'em.\"\n \n\n\"Is it under all of them?\"\n \n\n\"How you talk! No!\"\n \n\n\"Then how you going to know which one to go for?\"\n \n\n\"Go for all of 'em!\"\n \n\n\"Why, Tom, it'll take all summer.\"\n \n\n\"Well, what of that? Suppose you find a brass pot with a hundred dollars\nin it, all rusty and gray, or rotten chest full of di'monds. How's\nthat?\"\n \n\nHuck's eyes glowed.\n \n\n\"That's bully. Plenty bully enough for me. Just you gimme the hundred\ndollars and I don't want no di'monds.\"\n \n\n\"All right. But I bet you I ain't going to throw off on di'monds. Some\nof 'em's worth twenty dollars apiece--there ain't any, hardly, but's\nworth six bits or a dollar.\"\n \n\n\"No! Is that so?\"\n \n\n\"Cert'nly--anybody'll tell you so. Hain't you ever seen one, Huck?\"\n \n\n\"Not as I remember.\"\n \n\n\"Oh, kings have slathers of them.\"\n \n\n\n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=MershonIndustries.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "10 KB",
                                                                         "age" : "10 years and 8 months ago",
                                                                         "formattedMessageDate" : "Thu, 20 Mar 2003 00:00:00 EST"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "for a sheep worth 4 pence and a <span class=\"blue-hilite\">dog</span> worth a penny, and C kill the <span class=\"blue-hilite\">dog</span> before delivery, because bitten ... still due to A from B, and which party pays for the <span class=\"blue-hilite\">dog</span>, C or D, and who gets the money? If A, is the penny ... possible profit which might have inured from the <span class=\"blue-hilite\">dog</span>, and classifiable as earned increment, that is to ... ducts of thought. Wherefore I beseech you let the <span class=\"blue-hilite\">dog</span> and the onions and these people of the strange and",
                                                                   "subjectWithHighlights" : "\"I wit not what ye refer to.\"  \"How much...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<57329824.121094.1385256078329.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "\"I wit not what ye refer to.\"  \"How much...",
                                                                         "messageDate" : "02/21/2001",
                                                                         "year" : "2001",
                                                                         "monthDay" : "0221",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "George Suarez",
                                                                               "address" : "mywhat@somema1l.biz",
                                                                               "domain" : "somema1l.biz"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Julie Duran",
                                                                                  "address" : "inis23@ma1l2u.org",
                                                                                  "domain" : "ma1l2u.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Matthew Robles",
                                                                                  "address" : "rabbitdo89@hotma1l.org",
                                                                                  "domain" : "hotma1l.org"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Carol Miller",
                                                                                  "address" : "lhutchinson@yah00.co.uk",
                                                                                  "domain" : "yah00.co.uk"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Andrea Valencia",
                                                                                  "address" : "cbranch@yah00.net",
                                                                                  "domain" : "yah00.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Russell Hobbs",
                                                                                  "address" : "sidekickwe67@somema1l.net",
                                                                                  "domain" : "somema1l.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Matthew Robles",
                                                                                  "address" : "rabbitdo89@hotma1l.org",
                                                                                  "domain" : "hotma1l.org"
                                                                               }
                                                                            ],
                                                                         "size" : 8427,
                                                                         "body" : "\"I wit not what ye refer to.\" \"How much is 9 times 6?\" \"It is a mystery that is hidden from me by reason that the emergency requiring the fathoming of it hath not in my life-days occurred, and so, not having no need to know this thing, I abide barren of the knowledge.\" \"If A trade a barrel of onions to B, worth 2 pence the bushel, in exchange for a sheep worth 4 pence and a dog worth a penny, and C kill the dog before delivery, because bitten by the same, who mistook him for D, what sum is still due to A from B, and which party pays for the dog, C or D, and who gets the money? If A, is the penny sufficient, or may he claim consequential damages in the form of additional money to represent the possible profit which might have inured from the dog, and classifiable as earned increment, that is to say, usufruct?\" \"Verily, in the all-wise and unknowable providence of God, who moveth in mysterious ways his wonders to perform, have I never heard the fellow to this question for confusion of the mind and congestion of the ducts of thought. Wherefore I beseech you let the dog and the onions and these people of the strange and godless names work out their several salvations from their piteous and wonderful difficulties without help of mine, for indeed their trouble is sufficient as it is, whereas an I tried to help I should but damage their cause the more and yet mayhap not live myself to see the desolation wrought.\" \"What do you know of the laws of attraction and gravitation?\" \"If there be such, mayhap his grace the king did promulgate them whilst that I lay sick about the beginning of the year and thereby failed to hear his proclamation.\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 3768,
                                                                                  "creationTime" : "02/21/2001",
                                                                                  "modificationTime" : "02/21/2001",
                                                                                  "fileName" : "FortStewartTextiles.pdf",
                                                                                  "body" : "\n\"I have a letter from him,\" she replied.\n \n\nHe glanced at her with timid surprise.\n \n\n\"He's with the army, Father, at Smolensk.\"\n \n\nHe closed his eyes and remained silent a long time. Then as if in\nanswer to his doubts and to confirm the fact that now he understood and\nremembered everything, he nodded his head and reopened his eyes.\n \n\n\"Yes,\" he said, softly and distinctly. \"Russia has perished. They've\ndestroyed her.\"\n \n\nAnd he began to sob, and again tears flowed from his eyes. Princess Mary\ncould no longer restrain herself and wept while she gazed at his face.\n \n\nAgain he closed his eyes. His sobs ceased, he pointed to his eyes, and\nTikhon, understanding him, wiped away the tears.\n \n\nThen he again opened his eyes and said something none of them could\nunderstand for a long time, till at last Tikhon understood and repeated\nit. Princess Mary had sought the meaning of his words in the mood in\nwhich he had just been speaking. She thought he was speaking of Russia,\nor Prince Andrew, of herself, of his grandson, or of his own death, and\nso she could not guess his words.\n \n\n\"Put on your white dress. I like it,\" was what he said.\n \n\nHaving understood this Princess Mary sobbed still louder, and the doctor\ntaking her arm led her out to the veranda, soothing her and trying to\npersuade her to prepare for her journey. When she had left the room the\nprince again began speaking about his son, about the war, and about the\nEmperor, angrily twitching his brows and raising his hoarse voice, and\nthen he had a second and final stroke.\n \n\nPrincess Mary stayed on the veranda. The day had cleared, it was hot and\nsunny. She could understand nothing, think of nothing and feel nothing,\nexcept passionate love for her father, love such as she thought she had\nnever felt till that moment. She ran out sobbing into the garden and as\nfar as the pond, along the avenues of young lime trees Prince Andrew had\nplanted.\n\n\n\n \n\"Yes... I... I... I wished for his death! Yes, I wanted it to end\nquicker.... I wished to be at peace.... And what will become of me? What\nuse will peace be when he is no longer here?\" Princess Mary murmured,\npacing the garden with hurried steps and pressing her hands to her bosom\nwhich heaved with convulsive sobs.\n \n\nWhen she had completed the tour of the garden, which brought her again\nto the house, she saw Mademoiselle Bourienne--who had remained at\nBogucharovo and did not wish to leave it--coming toward her with a\nstranger. This was the Marshal of the Nobility of the district, who\nhad come personally to point out to the princess the necessity for her\nprompt departure. Princess Mary listened without understanding him; she\nled him to the house, offered him lunch, and sat down with him. Then,\nexcusing herself, she went to the door of the old prince's room. The\ndoctor came out with an agitated face and said she could not enter.\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=FortStewartTextiles.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "8 KB",
                                                                         "age" : "12 years and 9 months ago",
                                                                         "formattedMessageDate" : "Wed, 21 Feb 2001 00:00:00 EST"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "for a sheep worth 4 pence and a <span class=\"blue-hilite\">dog</span> worth a penny, and C kill the <span class=\"blue-hilite\">dog</span> before delivery, because bitten ... still due to A from B, and which party pays for the <span class=\"blue-hilite\">dog</span>, C or D, and who gets the money? If A, is the penny ... possible profit which might have inured from the <span class=\"blue-hilite\">dog</span>, and classifiable as earned increment, that is to ... ducts of thought. Wherefore I beseech you let the <span class=\"blue-hilite\">dog</span> and the onions and these people of the strange and",
                                                                   "subjectWithHighlights" : "\"How much is 9 times 6?\"  \"It is a mystery...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<736010588.128285.1385256129967.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "\"How much is 9 times 6?\"  \"It is a mystery...",
                                                                         "messageDate" : "09/28/2001",
                                                                         "year" : "2001",
                                                                         "monthDay" : "0928",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Trevor Holman",
                                                                               "address" : "whohave@gma1l.com",
                                                                               "domain" : "gma1l.com"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Lois Mills",
                                                                                  "address" : "probablyisland@everyma1l.co.uk",
                                                                                  "domain" : "everyma1l.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Jessica Romero",
                                                                                  "address" : "onhave@ma1lbox.biz",
                                                                                  "domain" : "ma1lbox.biz"
                                                                               },
                                                                               {
                                                                                  "name" : "Dan Avery",
                                                                                  "address" : "acasey@ma1lbox.com",
                                                                                  "domain" : "ma1lbox.com"
                                                                               },
                                                                               {
                                                                                  "name" : "Trevor Holman",
                                                                                  "address" : "whohave@gma1l.com",
                                                                                  "domain" : "gma1l.com"
                                                                               },
                                                                               {
                                                                                  "name" : "Austin Schwartz",
                                                                                  "address" : "tschwartz61@hotma1l.com",
                                                                                  "domain" : "hotma1l.com"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Donald Glover",
                                                                                  "address" : "stownsend@yah00.net",
                                                                                  "domain" : "yah00.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Ronda Reeves",
                                                                                  "address" : "temerson@gma1l.co.uk",
                                                                                  "domain" : "gma1l.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Darla Martin",
                                                                                  "address" : "inwant91@somema1l.org",
                                                                                  "domain" : "somema1l.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Don Griffith",
                                                                                  "address" : "illthey59@somema1l.biz",
                                                                                  "domain" : "somema1l.biz"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Paula Stanley",
                                                                                  "address" : "vknowles20@somema1l.us",
                                                                                  "domain" : "somema1l.us"
                                                                               }
                                                                            ],
                                                                         "size" : 9491,
                                                                         "body" : "\"How much is 9 times 6?\" \"It is a mystery that is hidden from me by reason that the emergency requiring the fathoming of it hath not in my life-days occurred, and so, not having no need to know this thing, I abide barren of the knowledge.\" \"If A trade a barrel of onions to B, worth 2 pence the bushel, in exchange for a sheep worth 4 pence and a dog worth a penny, and C kill the dog before delivery, because bitten by the same, who mistook him for D, what sum is still due to A from B, and which party pays for the dog, C or D, and who gets the money? If A, is the penny sufficient, or may he claim consequential damages in the form of additional money to represent the possible profit which might have inured from the dog, and classifiable as earned increment, that is to say, usufruct?\" \"Verily, in the all-wise and unknowable providence of God, who moveth in mysterious ways his wonders to perform, have I never heard the fellow to this question for confusion of the mind and congestion of the ducts of thought. Wherefore I beseech you let the dog and the onions and these people of the strange and godless names work out their several salvations from their piteous and wonderful difficulties without help of mine, for indeed their trouble is sufficient as it is, whereas an I tried to help I should but damage their cause the more and yet mayhap not live myself to see the desolation wrought.\" \"What do you know of the laws of attraction and gravitation?\" \"If there be such, mayhap his grace the king did promulgate them whilst that I lay sick about the beginning of the year and thereby failed to hear his proclamation.\" \"What do you know of the science of optics?\" \"I know of governors of places, and seneschals of castles, and sheriffs of counties, and many like small offices and titles of honor, but him you call the Science of Optics I have not heard of before; peradventure it is a new dignity.\" \"Yes, in this country.\" Try to conceive of this mollusk gravely applying for an official position, of any kind under the sun! Why, he had all the earmarks of a typewriter copyist, if you leave out the disposition to contribute uninvited emendations of your grammar and punctuation. It was unaccountable that he didn't attempt a little help of that sort out of his majestic supply of incapacity for the job. But that didn't prove that he hadn't material in him for the disposition, it only proved that he wasn't a typewriter copyist yet. After nagging him a little more, I let the professors loose on him and they turned him inside out, on the line of scientific war, and found him empty, of course. He knew somewhat about the warfare of the time--bushwhacking around for ogres, and bull-fights in the tournament ring, and such things--but otherwise he was empty and useless. Then we took the other young noble in hand, and he was the first one's twin, for ignorance and incapacity. I delivered them into the hands of the chairman of the Board with the comfortable consciousness that their cake was dough. They were examined in the previous order of precedence. \"Name, so please you?\" \"Pertipole, son of Sir Pertipole, Baron of Barley Mash.\" \"Also Sir Pertipole, Baron of Barley Mash.\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 1568,
                                                                                  "creationTime" : "09/28/2001",
                                                                                  "modificationTime" : "09/28/2001",
                                                                                  "fileName" : "RiceboroFurnishings.pdf",
                                                                                  "body" : "\n\"Why are you wandering about like an outcast?\" asked her mother. \"What\ndo you want?\"\n \n\n\"Him... I want him... now, this minute! I want him!\" said Natasha, with\nglittering eyes and no sign of a smile.\n \n\nThe countess lifted her head and looked attentively at her daughter.\n \n\n\"Don't look at me, Mamma! Don't look; I shall cry directly.\"\n \n\n\"Sit down with me a little,\" said the countess.\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=RiceboroFurnishings.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "9 KB",
                                                                         "age" : "12 years and 2 months ago",
                                                                         "formattedMessageDate" : "Fri, 28 Sep 2001 00:00:00 EDT"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "spoken of him as \"Harbison's Bull,\" but a son or a <span class=\"blue-hilite\">dog</span> of that name was \"Bull Harbison.\"] \"Oh, that's good--I ... death; I'd a bet anything it was a _stray_ <span class=\"blue-hilite\">dog</span>.\" The <span class=\"blue-hilite\">dog</span> howled again. The boys' hearts sank once more",
                                                                   "subjectWithHighlights" : "[* If Mr. Harbison owned a slave named Bull,...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<915106005.144305.1385256246900.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "[* If Mr. Harbison owned a slave named Bull,...",
                                                                         "messageDate" : "10/12/2002",
                                                                         "year" : "2002",
                                                                         "monthDay" : "1012",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Matthew Robles",
                                                                               "address" : "rabbitdo89@hotma1l.org",
                                                                               "domain" : "hotma1l.org"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Carmen Boyer",
                                                                                  "address" : "endball@ma1l2u.net",
                                                                                  "domain" : "ma1l2u.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Rachel Johnston",
                                                                                  "address" : "automaticallyin@ma1lbox.net",
                                                                                  "domain" : "ma1lbox.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Julie Duran",
                                                                                  "address" : "inis23@ma1l2u.org",
                                                                                  "domain" : "ma1l2u.org"
                                                                               },
                                                                               {
                                                                                  "name" : "Cassandra Molina",
                                                                                  "address" : "sotrees@everyma1l.net",
                                                                                  "domain" : "everyma1l.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Andrea Valencia",
                                                                                  "address" : "cbranch@yah00.net",
                                                                                  "domain" : "yah00.net"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Jimmy Tanner",
                                                                                  "address" : "jdunn@ma1l2u.us",
                                                                                  "domain" : "ma1l2u.us"
                                                                               },
                                                                               {
                                                                                  "name" : "Ralph Wyatt",
                                                                                  "address" : "shebert35@yah00.biz",
                                                                                  "domain" : "yah00.biz"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Dennis Farley",
                                                                                  "address" : "dsuarez@gma1l.us",
                                                                                  "domain" : "gma1l.us"
                                                                               },
                                                                               {
                                                                                  "name" : "Johnny Cohen",
                                                                                  "address" : "theyshepherd@everyma1l.co.uk",
                                                                                  "domain" : "everyma1l.co.uk"
                                                                               }
                                                                            ],
                                                                         "size" : 4457,
                                                                         "body" : "[* If Mr. Harbison owned a slave named Bull, Tom would have spoken of him as \"Harbison's Bull,\" but a son or a dog of that name was \"Bull Harbison.\"] \"Oh, that's good--I tell you, Tom, I was most scared to death; I'd a bet anything it was a _stray_ dog.\" The dog howled again. The boys' hearts sank once more. \"Oh, my! that ain't no Bull Harbison!\" whispered Huckleberry. \"_Do_, Tom!\" Tom, quaking with fear, yielded, and put his eye to the crack. His whisper was hardly audible when he said: \"Oh, Huck, _its a stray dog_!\" \"Quick, Tom, quick! Who does he mean?\" \"Huck, he must mean us both--we're right together.\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 1776,
                                                                                  "creationTime" : "10/12/2002",
                                                                                  "modificationTime" : "10/12/2002",
                                                                                  "fileName" : "AlmaIndustries.pdf",
                                                                                  "body" : "\n\"Well, did you have to go to Congress to get it?\"\n \n\n\"Well, neither does William Fourth have to go to the sea to get a sea\nbath.\"\n \n\n\"How does he get it, then?\"\n \n\n\"Gets it the way people down here gets Congress-waterin barrels.  There\nin the palace at Sheffield they've got furnaces, and he wants his water\nhot.  They can't bile that amount of water away off there at the sea.\nThey haven't got no conveniences for it.\"\n \n\n\"Oh, I see, now.  You might a said that in the first place and saved\ntime.\"\n \n\nWhen she said that I see I was out of the woods again, and so I was\ncomfortable and glad.  Next, she says:\n \n\n\"Do you go to church, too?\"\n \n\n\"Where do you set?\"\n \n\n\"Why, in our pew.\"\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=AlmaIndustries.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "4 KB",
                                                                         "age" : "11 years and 1 month ago",
                                                                         "formattedMessageDate" : "Sat, 12 Oct 2002 00:00:00 EDT"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "for a sheep worth 4 pence and a <span class=\"blue-hilite\">dog</span> worth a penny, and C kill the <span class=\"blue-hilite\">dog</span> before delivery, because bitten ... still due to A from B, and which party pays for the <span class=\"blue-hilite\">dog</span>, C or D, and who gets the money? If A, is the penny ... possible profit which might have inured from the <span class=\"blue-hilite\">dog</span>, and classifiable as earned increment, that is to ... ducts of thought. Wherefore I beseech you let the <span class=\"blue-hilite\">dog</span> and the onions and these people of the strange and",
                                                                   "subjectWithHighlights" : "\"Answer the question!\"  He crowded his wrath...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<1050572952.241424.1385256876186.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "\"Answer the question!\"  He crowded his wrath...",
                                                                         "messageDate" : "12/05/2007",
                                                                         "year" : "2007",
                                                                         "monthDay" : "1205",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Alice Watts",
                                                                               "address" : "visionswe55@b1zmail.net",
                                                                               "domain" : "b1zmail.net"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Nathan Thompson",
                                                                                  "address" : "coldis@somema1l.us",
                                                                                  "domain" : "somema1l.us"
                                                                               },
                                                                               {
                                                                                  "name" : "Gail Tate",
                                                                                  "address" : "chutchinson@ma1lbox.co.uk",
                                                                                  "domain" : "ma1lbox.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Judy Burks",
                                                                                  "address" : "alwaysit@ma1l2u.us",
                                                                                  "domain" : "ma1l2u.us"
                                                                               },
                                                                               {
                                                                                  "name" : "Lynn Robles",
                                                                                  "address" : "lreilly@ma1l2u.net",
                                                                                  "domain" : "ma1l2u.net"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Bobbie Hardy",
                                                                                  "address" : "movedsuspense@b1zmail.co.uk",
                                                                                  "domain" : "b1zmail.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Chuck Webster",
                                                                                  "address" : "caveslibrary@hotma1l.com",
                                                                                  "domain" : "hotma1l.com"
                                                                               },
                                                                               {
                                                                                  "name" : "George Suarez",
                                                                                  "address" : "mywhat@somema1l.biz",
                                                                                  "domain" : "somema1l.biz"
                                                                               },
                                                                               {
                                                                                  "name" : "Carol Miller",
                                                                                  "address" : "lhutchinson@yah00.co.uk",
                                                                                  "domain" : "yah00.co.uk"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Mother Chaney",
                                                                                  "address" : "jgrimes@somema1l.net",
                                                                                  "domain" : "somema1l.net"
                                                                               },
                                                                               {
                                                                                  "name" : "Maria Bryan",
                                                                                  "address" : "staylor@gma1l.biz",
                                                                                  "domain" : "gma1l.biz"
                                                                               },
                                                                               {
                                                                                  "name" : "Bobbie Hardy",
                                                                                  "address" : "movedsuspense@b1zmail.co.uk",
                                                                                  "domain" : "b1zmail.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Terri Alvarez",
                                                                                  "address" : "myis@ma1lbox.co.uk",
                                                                                  "domain" : "ma1lbox.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Tasha Rios",
                                                                                  "address" : "inin19@yah00.biz",
                                                                                  "domain" : "yah00.biz"
                                                                               }
                                                                            ],
                                                                         "size" : 8681,
                                                                         "body" : "\"Answer the question!\" He crowded his wrath down and made out to answer \"No.\" \"Can you write?\" He wanted to resent this, too, but I said: \"You will confine yourself to the questions, and make no comments. You are not here to air your blood or your graces, and nothing of the sort will be permitted. Can you write?\" \"Do you know the multiplication table?\" \"I wit not what ye refer to.\" \"How much is 9 times 6?\" \"It is a mystery that is hidden from me by reason that the emergency requiring the fathoming of it hath not in my life-days occurred, and so, not having no need to know this thing, I abide barren of the knowledge.\" \"If A trade a barrel of onions to B, worth 2 pence the bushel, in exchange for a sheep worth 4 pence and a dog worth a penny, and C kill the dog before delivery, because bitten by the same, who mistook him for D, what sum is still due to A from B, and which party pays for the dog, C or D, and who gets the money? If A, is the penny sufficient, or may he claim consequential damages in the form of additional money to represent the possible profit which might have inured from the dog, and classifiable as earned increment, that is to say, usufruct?\" \"Verily, in the all-wise and unknowable providence of God, who moveth in mysterious ways his wonders to perform, have I never heard the fellow to this question for confusion of the mind and congestion of the ducts of thought. Wherefore I beseech you let the dog and the onions and these people of the strange and godless names work out their several salvations from their piteous and wonderful difficulties without help of mine, for indeed their trouble is sufficient as it is, whereas an I tried to help I should but damage their cause the more and yet mayhap not live myself to see the desolation wrought.\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 3472,
                                                                                  "creationTime" : "12/05/2007",
                                                                                  "modificationTime" : "12/05/2007",
                                                                                  "fileName" : "AtlantaMedicalsupplies.pdf",
                                                                                  "body" : "\nHe drove to their house in some agitation. The memory of Natasha was his\nmost poetic recollection. But he went with the firm intention of letting\nher and her parents feel that the childish relations between himself and\nNatasha could not be binding either on her or on him. He had a brilliant\nposition in society thanks to his intimacy with Countess Bezukhova,\na brilliant position in the service thanks to the patronage of an\nimportant personage whose complete confidence he enjoyed, and he was\nbeginning to make plans for marrying one of the richest heiresses in\nPetersburg, plans which might very easily be realized. When he entered\nthe Rostovs' drawing room Natasha was in her own room. When she heard\nof his arrival she almost ran into the drawing room, flushed and beaming\nwith a more than cordial smile.\n \n\nBoris remembered Natasha in a short dress, with dark eyes shining from\nunder her curls and boisterous, childish laughter, as he had known her\nfour years before; and so he was taken aback when quite a different\nNatasha entered, and his face expressed rapturous astonishment. This\nexpression on his face pleased Natasha.\n \n\n\"Well, do you recognize your little madcap playmate?\" asked the\ncountess.\n \n\nBoris kissed Natasha's hand and said that he was astonished at the\nchange in her.\n \n\n\"How handsome you have grown!\"\n \n\n\"I should think so!\" replied Natasha's laughing eyes.\n \n\n\"And is Papa older?\" she asked.\n \n\nNatasha sat down and, without joining in Boris' conversation with the\ncountess, silently and minutely studied her childhood's suitor. He felt\nthe weight of that resolute and affectionate scrutiny and glanced at her\noccasionally.\n \n\nBoris' uniform, spurs, tie, and the way his hair was brushed were all\ncomme il faut and in the latest fashion. This Natasha noticed at once.\nHe sat rather sideways in the armchair next to the countess, arranging\nwith his right hand the cleanest of gloves that fitted his left hand\nlike a skin, and he spoke with a particularly refined compression of his\n\n\n\nlips about the amusements of the highest Petersburg society, recalling\nwith mild irony old times in Moscow and Moscow acquaintances. It was\nnot accidentally, Natasha felt, that he alluded, when speaking of the\nhighest aristocracy, to an ambassador's ball he had attended, and to\ninvitations he had received from N.N. and S.S.\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=AtlantaMedicalsupplies.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "8 KB",
                                                                         "age" : "5 years and 11 months ago",
                                                                         "formattedMessageDate" : "Wed, 5 Dec 2007 00:00:00 EST"
                                                                      }
                                                                },
                                                                {
                                                                   "bodyWithHighlights" : "continued to whisper for some little time. Presently a <span class=\"blue-hilite\">dog</span> set up a long, lugubrious howl just outside--within ... spoken of him as \"Harbison's Bull,\" but a son or a <span class=\"blue-hilite\">dog</span> of that name was \"Bull Harbison.\"] \"Oh, that's good--I ... death; I'd a bet anything it was a _stray_ <span class=\"blue-hilite\">dog</span>.\" The <span class=\"blue-hilite\">dog</span> howled again. The boys' hearts sank once more",
                                                                   "subjectWithHighlights" : "They continued to whisper for some little...",
                                                                   "attachmentWithHighlights" : "",
                                                                   "formattedIndexedMailMessage" :
                                                                      {
                                                                         "messageId" : "<109700206.103376.1385255954368.JavaMail.ilya@crunchbang>",
                                                                         "subject" : "They continued to whisper for some little...",
                                                                         "messageDate" : "01/27/2007",
                                                                         "year" : "2007",
                                                                         "monthDay" : "0127",
                                                                         "sender" :
                                                                            {
                                                                               "name" : "Clara Frazier",
                                                                               "address" : "mbrock@hotma1l.org",
                                                                               "domain" : "hotma1l.org"
                                                                            },
                                                                         "to" :
                                                                            [
                                                                               {
                                                                                  "name" : "Craig Odom",
                                                                                  "address" : "ksuarez@everyma1l.biz",
                                                                                  "domain" : "everyma1l.biz"
                                                                               }
                                                                            ],
                                                                         "cc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Ronda Reeves",
                                                                                  "address" : "temerson@gma1l.co.uk",
                                                                                  "domain" : "gma1l.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Melissa Knight",
                                                                                  "address" : "aswith@everyma1l.co.uk",
                                                                                  "domain" : "everyma1l.co.uk"
                                                                               },
                                                                               {
                                                                                  "name" : "Ellen Tran",
                                                                                  "address" : "lotsred@hotma1l.org",
                                                                                  "domain" : "hotma1l.org"
                                                                               }
                                                                            ],
                                                                         "bcc" :
                                                                            [
                                                                               {
                                                                                  "name" : "Joshua Dalton",
                                                                                  "address" : "rhardin@yah00.biz",
                                                                                  "domain" : "yah00.biz"
                                                                               }
                                                                            ],
                                                                         "size" : 6148,
                                                                         "body" : "They continued to whisper for some little time. Presently a dog set up a long, lugubrious howl just outside--within ten feet of them. The boys clasped each other suddenly, in an agony of fright. \"Which of us does he mean?\" gasped Huckleberry. \"I dono--peep through the crack. Quick!\" \"No, _you_, Tom!\" \"I can't--I can't _do_ it, Huck!\" \"Please, Tom. There 'tis again!\" \"Oh, lordy, I'm thankful!\" whispered Tom. \"I know his voice. It's Bull Harbison.\" * [* If Mr. Harbison owned a slave named Bull, Tom would have spoken of him as \"Harbison's Bull,\" but a son or a dog of that name was \"Bull Harbison.\"] \"Oh, that's good--I tell you, Tom, I was most scared to death; I'd a bet anything it was a _stray_ dog.\" The dog howled again. The boys' hearts sank once more. \"Oh, my! that ain't no Bull Harbison!\" whispered Huckleberry. \"_Do_, Tom!\" Tom, quaking with fear, yielded, and put his eye to the crack. His whisper was hardly audible when he said: \"Oh, Huck, _its a stray dog_!\" \n",
                                                                         "importance" : null,
                                                                         "attachments" :
                                                                            [
                                                                               {
                                                                                  "size" : 2898,
                                                                                  "creationTime" : "01/27/2007",
                                                                                  "modificationTime" : "01/27/2007",
                                                                                  "fileName" : "AustellDevelopment.pdf",
                                                                                  "body" : "\n\"Hello, what's up?  Don't cry, bub.  What's the trouble?\"\n \n\n\"Pap, and mam, and sis, and\"\n \n\nThen I broke down.  He says:\n \n\n\"Oh, dang it now, _don't_ take on so; we all has to have our troubles,\nand this 'n 'll come out all right.  What's the matter with 'em?\"\n \n\n\"They'rethey'reare you the watchman of the boat?\"\n \n\n\"Yes,\" he says, kind of pretty-well-satisfied like.  \"I'm the captain\nand the owner and the mate and the pilot and watchman and head\ndeck-hand; and sometimes I'm the freight and passengers.  I ain't as\nrich as old Jim Hornback, and I can't be so blame' generous and good\nto Tom, Dick, and Harry as what he is, and slam around money the way he\ndoes; but I've told him a many a time 't I wouldn't trade places with\nhim; for, says I, a sailor's life's the life for me, and I'm derned if\n_I'd_ live two mile out o' town, where there ain't nothing ever goin'\non, not for all his spondulicks and as much more on top of it.  Says I\"\n \n\nI broke in and says:\n \n\n\"They're in an awful peck of trouble, and\"\n \n\n\"Why, pap and mam and sis and Miss Hooker; and if you'd take your\nferryboat and go up there\"\n \n\n\"Up where?  Where are they?\"\n \n\n\"Why, there ain't but one.\"\n \n\n\"What, you don't mean the Walter Scott?\"\n \n\n\"Good land! what are they doin' _there_, for gracious sakes?\"\n \n\n\"Well, they didn't go there a-purpose.\"\n \n\n\"I bet they didn't!  Why, great goodness, there ain't no chance for 'em\nif they don't git off mighty quick!  Why, how in the nation did they\never git into such a scrape?\"\n\n\n\n \n\"Easy enough.  Miss Hooker was a-visiting up there to the town\"\n \n\n\n",
                                                                                  "contentType" : "application/octet-stream; name=AustellDevelopment.pdf",
                                                                                  "contentDisposition" : "attachment",
                                                                                  "fileExt" : "pdf"
                                                                               }
                                                                            ],
                                                                         "sources" : null,
                                                                         "messageType" : "EMAIL",
                                                                         "formattedSize" : "6 KB",
                                                                         "age" : "6 years and 10 months ago",
                                                                         "formattedMessageDate" : "Sat, 27 Jan 2007 00:00:00 EST"
                                                                      }
                                                                }
                                                             ];

    }
});
