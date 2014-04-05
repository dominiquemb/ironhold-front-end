/*global module:false*/
module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    // Metadata.
    pkg: grunt.file.readJSON('package.json'),
    banner: '/*! <%= pkg.title || pkg.name %> - v<%= pkg.version %> - ' +
      '<%= grunt.template.today("yyyy-mm-dd") %>\n' +
      '<%= pkg.homepage ? "* " + pkg.homepage + "\\n" : "" %>' +
      '* Copyright (c) <%= grunt.template.today("yyyy") %> <%= pkg.author.name %>;' +
      ' Licensed <%= _.pluck(pkg.licenses, "type").join(", ") %> */\n',
    // Task configuration.
    concat: {
      options: {
        banner: '<%= banner %>',
        stripBanners: true
      },
      dist: {
        src: ['lib/<%= pkg.name %>.js'],
        dest: 'dist/<%= pkg.name %>.js'
      }
    },
    uglify: {
        options: {
          mangle: false
        },
        all: {
          files: {
            'src/main/webapp/app/js/app.js':['src/main/webapp/app/js/app.js'],
            'src/main/webapp/app/js/directives/GlobalDirectives.js':['src/main/webapp/app/js/directives/GlobalDirectives.js'],
            'src/main/webapp/app/js/controllers/BodyController.js':['src/main/webapp/app/js/controllers/BodyController.js'],
            'src/main/webapp/app/js/controllers/DiscoveryController.js':['src/main/webapp/app/js/controllers/DiscoveryController.js'],
            'src/main/webapp/app/js/controllers/ErrorsController.js':['src/main/webapp/app/js/controllers/ErrorsController.js'],
            'src/main/webapp/app/js/controllers/FacetController.js':['src/main/webapp/app/js/controllers/FacetController.js'],
            'src/main/webapp/app/js/controllers/FilterController.js':['src/main/webapp/app/js/controllers/FilterController.js'],
            'src/main/webapp/app/js/controllers/FoldersController.js':['src/main/webapp/app/js/controllers/FoldersController.js'],
            'src/main/webapp/app/js/controllers/FooterController.js':['src/main/webapp/app/js/controllers/FooterController.js'],
            'src/main/webapp/app/js/controllers/LoginController.js':['src/main/webapp/app/js/controllers/LoginController.js'],
            'src/main/webapp/app/js/controllers/LogsController.js':['src/main/webapp/app/js/controllers/LogsController.js'],
            'src/main/webapp/app/js/controllers/MessageSearchController.js':['src/main/webapp/app/js/controllers/MessageSearchController.js'],
            'src/main/webapp/app/js/controllers/MultipleResultDisplayController.js':['src/main/webapp/app/js/controllers/MultipleResultDisplayController.js'],
            'src/main/webapp/app/js/controllers/PaginationController.js':['src/main/webapp/app/js/controllers/PaginationController.js'],
            'src/main/webapp/app/js/controllers/SearchbarController.js':['src/main/webapp/app/js/controllers/SearchbarController.js'],
            'src/main/webapp/app/js/controllers/SearchController.js':['src/main/webapp/app/js/controllers/SearchController.js'],
            'src/main/webapp/app/js/controllers/SettingsController.js':['src/main/webapp/app/js/controllers/SettingsController.js'],
            'src/main/webapp/app/js/controllers/SingleResultDisplayController.js':['src/main/webapp/app/js/controllers/SingleResultDisplayController.js'],
            'src/main/webapp/app/js/controllers/TabController.js':['src/main/webapp/app/js/controllers/TabController.js'],
            'src/main/webapp/app/js/controllers/UserActionsController.js':['src/main/webapp/app/js/controllers/UserActionsController.js'],
            'src/main/webapp/app/js/controllers/UsersController.js':['src/main/webapp/app/js/controllers/UsersController.js']
          }
        }
    },
    cssmin: {
      all: {
        files: {
          'src/main/webapp/app/css/roboto.css': ['src/main/webapp/app/css/roboto.css'],
          'src/main/webapp/app/css/login.css': ['src/main/webapp/app/css/login.css'],
          'src/main/webapp/app/css/custom.css': ['src/main/webapp/app/css/custom.css'],
          'src/main/webapp/app/css/jquery.splitter.css': ['src/main/webapp/app/css/jquery.splitter.css'],
          'src/main/webapp/app/css/font-awesome.css': ['src/main/webapp/app/css/font-awesome.css'],
          'src/main/webapp/app/css/jscrollpane/jquery.jscrollpane.css': ['src/main/webapp/app/css/jscrollpane/jquery.jscrollpane.css'],
          'src/main/webapp/app/css/ace-fonts.css': ['src/main/webapp/app/css/ace-fonts.css'],
          'src/main/webapp/app/css/roboto.css': ['src/main/webapp/app/css/roboto.css'],
          'src/main/webapp/app/css/roboto.css': ['src/main/webapp/app/css/roboto.css'],
          'src/main/webapp/app/css/roboto.css': ['src/main/webapp/app/css/roboto.css'],
        }
      }
    },
    jshint: {
      options: {
        curly: true,
        eqeqeq: true,
        immed: true,
        latedef: true,
        newcap: true,
        noarg: true,
        sub: true,
        undef: true,
        unused: true,
        boss: true,
        eqnull: true,
        browser: true,
        globals: {
          jQuery: true
        },
        predef: ["angular","alert","ironholdApp","$","console"]
      },
      gruntfile: {
        src: 'Gruntfile.js'
      },
      all: {
        src: ['src/main/webapp/app/js/app.js','src/main/webapp/app/js/**/*.js']
      }
    },
    qunit: {
      files: ['test/**/*.html']
    },
    watch: {
      gruntfile: {
        files: '<%= jshint.gruntfile.src %>',
        tasks: ['jshint:gruntfile']
      },
      lib_test: {
        files: '<%= jshint.lib_test.src %>',
        tasks: ['jshint:lib_test', 'qunit']
      }
    },
    htmlangular: {
	options: {
		relaxerror: [
			'Element head is missing a required instance of child element title.'
		],
		reportpath: null,
		tmplext: 'html',
		customattrs: [
		    'tooltip',
		    'tooltip-animation',
		    'trunc-text',
		    'trunc-font-width',
		    'trunc-desired-height',
		    'trunc-trailing-dots'
		]
	},
	all: ["src/main/webapp/app/index.html","src/main/webapp/**/*.html"]
    }
  });

  // These plugins provide necessary tasks.
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-qunit');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-html-angular-validate');

  // Default task.
  grunt.registerTask('default', ['htmlangular','jshint']);

};
