/* global module: false, process: false */
module.exports = function (grunt) {
    'use strict';

    require('time-grunt')(grunt);

    /**
     * Setup
     */
    var isDev = (grunt.option('dev') !== undefined) ? Boolean(grunt.option('dev')) : process.env.GRUNT_ISDEV === '1';
    var pkg = grunt.file.readJSON('package.json');
    var singleRun = grunt.option('single-run') !== false;

    /**
     * Load all grunt-* tasks
     */
    require('load-grunt-tasks')(grunt);

    if (isDev) {
        grunt.log.subhead('Running Grunt in DEV mode');
    }

    /**
     * Project configuration
     */
    grunt.initConfig({

        pkg: pkg,

        dirs: {
            publicDir: {
                root: 'public',
                stylesheets: '<%= dirs.publicDir.root %>/stylesheets',
                javascripts: '<%= dirs.publicDir.root %>/javascripts',
            },
            assets: {
                root: 'assets',
                stylesheets: '<%= dirs.assets.root %>/stylesheets',
                javascripts: '<%= dirs.assets.root %>/javascripts',
            }
        },

        /***********************************************************************
         * Compile
         ***********************************************************************/

        sass: {
            options: {
                outputStyle: 'compressed',
                sourceMap: isDev,
                precision: 5
            },
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= dirs.assets.stylesheets %>',
                    src: [
                        'style.scss',
                        'ie9.style.scss',
                        'tools.style.scss',
                        'event-card.scss',
                        'daterangepicker.css',
                    ],
                    dest: '<%= dirs.publicDir.stylesheets %>',
                    ext: '.css'
                }]
            }
        },

        requirejs: {
            compile: {
                options: {
                    name: 'src/main',
                    baseUrl: '<%= dirs.assets.javascripts %>',
                    // Keep these in sync with the paths found in the karma test-main.js paths
                    paths: {
                        'jquery': 'lib/bower-components/jquery/dist/jquery',
                        'bootstrap-daterangepicker': 'lib/bower-components/bootstrap-daterangepicker/daterangepicker',
                        'moment': 'lib/bower-components/moment/moment',
                        'datePicker': 'src/modules/datePicker',
                        'query-object': 'lib/bower-components/query-object/query-object'
                    },
                    findNestedDependencies: false,
                    wrapShim: true,
                    optimize: isDev ? 'none' : 'uglify2',
                    generateSourceMaps: true,
                    preserveLicenseComments: false,
                    out: '<%= dirs.publicDir.javascripts %>/main.js',
                    include: ['lib/bower-components/requirejs/require']
                }
            }
        },

        /***********************************************************************
         * Copy & Clean
         ***********************************************************************/

        copy: {
            css: {
                files: [{
                    expand: true,
                    cwd: '<%= dirs.assets.stylesheets %>',
                    src: ['**/*.css'],
                    dest: '<%= dirs.publicDir.stylesheets %>'
                }]
            },
            polyfills: {
                src: '<%= dirs.assets.javascripts %>/lib/polyfills.min.js',
                dest: '<%= dirs.publicDir.javascripts %>/lib/polyfills.min.js'
            }
        },

        clean: {

            js: ['<%= dirs.publicDir.javascripts %>'],
            css: ['<%= dirs.publicDir.stylesheets %>'],
            assetMap: 'conf/assets.map',
            dist: ['<%= dirs.publicDir.root %>/dist/']
        },

        /***********************************************************************
         * Watch
         ***********************************************************************/

        watch: {
            compile: {
                files: ['<%= dirs.assets.stylesheets %>/**/*.scss', '<%= dirs.assets.javascripts %>/**/*.js'],
                tasks: ['compile']
            }
        },

        /***********************************************************************
         * Assets
         ***********************************************************************/

        // generate a mapping file of hashed assets
        // and move/rename built files to /dist/
        asset_hash: {
            options: {
                preserveSourceMaps: true,
                assetMap: isDev ? false : 'conf/assets.map',
                hashLength: 8,
                algorithm: 'md5',
                srcBasePath: 'public/',
                destBasePath: 'public/',
                references: [
                    '<%= dirs.publicDir.root %>/dist/stylesheets/**/*.css'
                ]
            },
            staticfiles: {
                files: [{
                    src: [
                        '<%= dirs.publicDir.stylesheets %>/**/*.css',
                        '<%= dirs.publicDir.javascripts %>/**/*.js',
                        '<%= dirs.publicDir.javascripts %>/**/*.map',
                        '<%= dirs.publicDir.images %>/**/*.*'
                    ],
                    dest: '<%= dirs.publicDir.root %>/dist/'
                }]
            }
        },

        /***********************************************************************
         * Test & Validate
         ***********************************************************************/

        karma: {
            options: {
                reporters: ['progress'],
                singleRun: singleRun
            },
            unit: {
                configFile: 'karma.conf.js',
                browsers: ['PhantomJS']
            }
        },

        /**
         * Lint Javascript sources
         */
        eslint: {
            options: {
                configFile: '.eslintrc'
            },
            app: {
                files: [{
                    expand: true,
                    cwd: '<%= dirs.assets.javascripts %>/',
                    src: [
                        'config/**/*.js',
                        'src/**/*.js'
                    ]
                }]
            }
        }
    });


    /***********************************************************************
     * Compile & Validate
     ***********************************************************************/

    grunt.registerTask('validate', ['eslint']);

    grunt.registerTask('build:css', ['sass']);

    grunt.registerTask('compile:css', [
        'clean:css',
        'build:css'
    ]);
    grunt.registerTask('compile:js', function() {
        if (!isDev) {
            grunt.task.run(['validate']);
        }
        grunt.task.run([
            'clean:js',
            'requirejs:compile'
        ]);
    });
    grunt.registerTask('compile', function(){
        grunt.task.run([
            'clean:public',
            'compile:css',
            'compile:js',
        ]);
        /**
         * Only version files for prod builds
         * Wipe out unused non-versioned assets for good measure
         */
        if (!isDev) {
            grunt.task.run([
                'clean:public:prod'
            ]);
        }
    });

    /***********************************************************************
     * Test
     ***********************************************************************/

    grunt.registerTask('test', function(){
        grunt.task.run(['test:unit']);
    });
    grunt.registerTask('test:unit', function() {
        grunt.config.set('karma.options.singleRun', (singleRun === false) ? false : true);
        grunt.task.run(['karma:unit']);
    });


    /***********************************************************************
     * Clean
     ***********************************************************************/

    grunt.registerTask('clean:public', [
        'clean:js',
        'clean:css',
        'clean:assetMap',
        'clean:dist'
    ]);
    // Why don't we clean bookmarklets here? Because this is for cleaning out
    // the pre-hashed assets after hashing, and bookmarklets aren't hashed.
    grunt.registerTask('clean:public:prod', [
        'clean:js',
        'clean:css'
    ]);
};