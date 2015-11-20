/*
 * Copyright (c) 2015 PTC Inc.
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PTC Inc. and is subject to the terms of a software license agreement.
 * You shall not disclose such confidential information and shall use
 * it only in accordance with the terms of the license agreement.
 *
 */

/* var s_runtimePluginVersion = "10.2.30.38"; */

TW.Runtime.Widgets.terminalWidget = function () {
    var thisWidget = this;
    thisWidget.selectedRows = [];
    thisWidget.selectedInstances = [];
    this.BackgroundStyle = TW.getStyleFromStyleDefinition(thisWidget.getProperty('BackgroundStyle', ''));
    var OccurrenceIdField = thisWidget.getProperty('OccurrenceField');
    if (!OccurrenceIdField) {
        OccurrenceIdField = 'treeId';
    }
    var localData;
    var instancesPromise = $.Deferred();
    var dataPromise = $.Deferred();

    this.runtimeProperties = function () {
        return {
            'needsDataLoadingAndError': false
        };
    };

    this.renderHtml = function () {
        return '<div class="widget-content widget-terminal" style="display:block;"></div>';
    };

    this.resize = function (width, height) {
        this.terminal.resize();
    };

    this.afterRender = function () {
        var thisWidget = this;
        var options = {
            greetings: thisWidget.getProperty('GreetingText') || "Execute commands on a remote server",
            name: thisWidget.jqElementId
        };
        if (!thisWidget.getProperty('isAuthenticated')) {
            if (thisWidget.getProperty('Username')) {
                options.prompt = "Password: ";
            } else {
                options.prompt = "Username: ";
            }
        }
        $('.widget-terminal').terminal(function (command, term) {
            if (thisWidget.getProperty('isAuthenticated')) {
                thisWidget.setProperty('NextCommand', command);
                thisWidget.jqElement.triggerHandler('CommandTyped');
                term.pause();
            } else {
                if (thisWidget.getProperty('Username')) {
                    thisWidget.setProperty('Password', command);
                    term.pause();
                    thisWidget.jqElement.triggerHandler('AuthenticationAttempt');
                } else {
                    term.set_prompt("Password: ");
                    thisWidget.setProperty('Username', command)
                }
            }
        }, options).focus();
        thisWidget.updateBGColor();
        this.terminal = $('#' + thisWidget.jqElementId).terminal();
    };

    this.updateProperty = function (updatePropertyInfo) {
        //console.log("update property" , updatePropertyInfo.TargetProperty);
        this.setProperty(updatePropertyInfo.TargetProperty, updatePropertyInfo.RawSinglePropertyValue);

        switch (updatePropertyInfo.TargetProperty) {
            case 'LastCommandResult':
            {
                this.terminal.echo(thisWidget.getProperty('LastCommandResult'));
                this.terminal.resume();
                break;
            }
            case 'NextCommand':
            {
                this.terminal.exec(thisWidget.getProperty('NextCommand'));
                break;
            }

            case 'BackgroundStyle':
            {
                thisWidget.BackgroundStyle = TW.getStyleFromStyleDefinition(thisWidget.getProperty('BackgroundStyle', ''));
                thisWidget.updateBGColor();
                break;
            }
            case 'isAuthenticated':
            {
                if (thisWidget.getProperty("isAuthenticated")) {
                    thisWidget.terminal.echo("Login Successful");
                    this.terminal.set_prompt(">");
                } else {
                    thisWidget.terminal.echo("Login Failed");
                    thisWidget.setProperty('Username', '');
                    thisWidget.terminal.set_prompt("Username: ");
                }
                this.terminal.resume();
                break;
            }
        }
    };
    this.getParentRow = function (id) {
        var parent;
        if (!localData) {
            return;
        }

        var i = localData.length;
        while (i--) {
            if (localData[i].objectId === id) {
                return localData[i];
            }
        }

        return null;
    };

    this.constructIdPath = function (row) {
        var id = row[OccurrenceIdField] + '';
        if (id.charAt(0) === '/') {
            return id;
        }
        var parent = row;
        var count = 0;
        while (parent && id.charAt(0) !== '/' && count++ < 100) {
            id = '/' + id;
            parent = this.getParentRow(parent.parentId);
            if (parent) {
                var pid = parent[OccurrenceIdField];
                if (pid !== '/' && pid) {
                    id = pid + id;
                }
            }
        }
        row._cachedOccurrencePath = id;
        return id;
    };

    this.updateBGColor = function () {
        const element = $('#' + thisWidget.jqElementId);

        if (thisWidget.BackgroundStyle.backgroundColor) {
            element.css("background-color", thisWidget.BackgroundStyle.backgroundColor);
            element.find(".cmd").css("background-color", thisWidget.BackgroundStyle.backgroundColor);
            element.find(".inverted").css("color", thisWidget.BackgroundStyle.backgroundColor);
        }
        if (thisWidget.BackgroundStyle.foregroundColor) {
            element.css("color", thisWidget.BackgroundStyle.foregroundColor);
            element.find(".cmd").css("color", thisWidget.BackgroundStyle.foregroundColor);
            element.find(".inverted").css("background-color", thisWidget.BackgroundStyle.foregroundColor);
        }
        if (thisWidget.BackgroundStyle.textSize) {
            element.addClass("textsize-" + thisWidget.BackgroundStyle.textSize);
            element.find(".cmd").addClass("textsize-" + thisWidget.BackgroundStyle.textSize);
        }
    };

    this.beforeDestroy = function () {
        if (thisWidget.jqElement) {
            thisWidget.jqElement.find('img').unbind('click');
        }
        thisWidget.reverseidMap = null;
        localData = dataPromise = instancesPromise = thisWidget = formatter = null;
    };

    this.serviceInvoked = function (serviceName) {
        if (serviceName === 'Clear') {
            thisWidget.terminal.clear();
        } else {
            TW.log.error('terminal widget, unexpected serviceName invoked "' + serviceName + '"');
        }
    };
};
