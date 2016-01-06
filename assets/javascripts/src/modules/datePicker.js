define([
    'jquery',
    'moment',
    'bootstrap-daterangepicker',
    'query-object'
], function ($, moment, daterangepicker, queryObject) {
    'use strict';

// default to HTML5 history API for updating URL params
// (otherwise we get a full page reload when modifying them)
    queryObject.useHistory = true;

    function formatField(start, end, formatString) {
        if (!formatString) {
            formatString = 'dddd Do MMMM YYYY';
        }
        var formatted = ''
        var s = moment(start).format(formatString);
        var e = moment(end).format(formatString);

        if (s === e) {
            formatted = s;
        } else {
            formatted = [s, e].join(' - ');
        }
        return formatted;
    }

    function dateIsToday(date) {
        return moment(date).isSame(new Date(), 'day');
    }

    // used to compare a date range to the previous equivalent period
    function getPreviousPeriod(start, end) {
        var s = moment(start);
        var e = moment(end);
        var dayDiff = e.diff(s, 'days');
        var startPrevious = s.subtract(dayDiff, 'days').hours(0).minutes(0).seconds(0).valueOf();
        var endPrevious = e.subtract(dayDiff, 'days').hours(0).minutes(0).seconds(0).valueOf();

        return {
            start: startPrevious,
            end: endPrevious
        };
    }

    function setPreviousPeriod(that) {
        var prev = getPreviousPeriod(that.start, that.end);

        that.startPrev = prev.start,
            that.endPrev = prev.end
        that.prevString = formatField(that.startPrev, that.endPrev, 'D/MM/YYYY');
    }

    var datePicker = {
        start: undefined,
        end: undefined,
        startPrev: undefined,
        endPrev: undefined,
        prevString: undefined,
        queryParams: undefined,
        // defaults to a week, globally
        defaultStart: moment().subtract(21, 'days').hours(0).minutes(0).seconds(0).valueOf(),
        defaultEnd: Date.now(),
        refresh: 0,

        domReady: function() {
            var that = this;
            that.queryParams = queryObject.get();

            if (that.queryParams) {
                // still have to be sure the correct params are there
                that.start = (that.queryParams.fromDate) ? parseInt(that.queryParams.fromDate) : that.defaultStart;
                that.end = (that.queryParams.toDate) ? parseInt(that.queryParams.toDate) : that.defaultEnd;
            } else {
                that.start = that.defaultStart;
                that.end = that.defaultEnd;
            }
            setPreviousPeriod(that);
            $('#datepicker').value = formatField(that.start, that.end);

            $('#datepicker').daterangepicker({
                format: 'DD/MM/YYYY',
                maxDate: new Date(),
                startDate: new Date(that.start),
                endDate: new Date(that.end),
                parentEl: that.placeholder
            }).on('apply.daterangepicker', function(ev, picker) {
                $(this).val(formatField(picker.startDate, picker.endDate));

                that.start = picker.startDate;
                that.end = picker.endDate;

                queryObject.set({
                    fromDate: moment(that.start).valueOf(),
                    toDate: moment(that.end).valueOf()
                });
                setPreviousPeriod(that);
                document.location.reload(true);
            });
        },
        // triggers automatically on a refresh cycle
        update: function() {
            // is one of the dates today?
            // if so, update it to right now
            // to trigger a data refresh
            if(dateIsToday(this.start)) {
                var startOfToday = moment().hours(0).minutes(0).seconds(0).valueOf();
                this.start = startOfToday
                queryObject.remove('fromDate');
            } else if (dateIsToday(this.end)) {
                this.end = Date.now();
                queryObject.remove('toDate');
            }
        },
        startTimer: function() {
            // Clear current state
            this.stopTimer();
            var refreshInterval = this.refresh && Number(this.refresh);

            if (refreshInterval) {
                this._refreshInterval = setInterval(this.update.bind(this), refreshInterval);
            }
        },
        stopTimer: function() {
            if (this._refreshInterval) {
                clearInterval(this._refreshInterval);
                delete this._refreshInterval;
            }
        },
        /* == Lifecycle == */
        ready: function() {
            this.startTimer();
        },
        attached: function() {
            this.startTimer();
        },
        detached: function() {
            this.stopTimer();
        },
        init: function() {
            this.domReady();
            this.startTimer();
        }
    }

    return datePicker

});