/*
 *
 * Copyright (c) 2015 PTC Inc.
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PTC Inc. and is subject to the terms of a software license agreement.
 * You shall not disclose such confidential information and shall use
 * it only in accordance with the terms of the license agreement.
 *
 *
 */

/* var s_idePluginVersion = "10.2.30.38"; */

TW.IDE.Widgets.terminalWidget = function () {
    this.widgetIconUrl = function () {
        return "../Common/extensions/terminal-widget/ui/terminalWidget/terminalWidget.ide.png";
    };

    this.widgetProperties = function () {
        return {
            'name': 'TerminalWidget',
            'description': 'A widget to display a terminal in',
            'category': ['Common', 'Components'],
            'isResizable': true,
            'supportsAutoResize': true,
            'properties': {
                'Width': {
                    'defaultValue': 500
                },
                'Height': {
                    'defaultValue': 400
                },
                'Password': {
                    'isBindingTarget': true,
                    'isBindingSource': true,
                    'isVisible': true,
                    'baseType': 'STRING',
                    'description': 'If no password is configured on the SshThing, then one here.'
                },
                'Username': {
                    'isBindingTarget': true,
                    'isBindingSource': true,
                    'isVisible': true,
                    'baseType': 'STRING',
                    'description': 'If no password is configured on the SshThing, then one here.'
                },
                'BackgroundStyle': {
                    'baseType': 'STYLEDEFINITION',
                    'defaultValue': '',
                    'description': 'The background, foreground and text size of the widget'
                },
                "NextCommand": {
                    'description': 'The next command to be executed',
                    'baseType': 'STRING',
                    'isBindingSource': true,
                    'isBindingTarget': true
                },
                'LastCommandResult': {
                    'description': 'The result of the last command to be added',
                    'baseType': 'STRING',
                    'isBindingSource': false,
                    'isBindingTarget': true
                },
                'GreetingText': {
                    'description': 'The initial text to be displayed on the terminal',
                    'baseType': 'STRING',
                    'defaultValue': 'Execute commands on a remote server',
                    'isBindingSource': false,
                    'isBindingTarget': false
                },
                'isAuthenticated': {
                    'description': 'Perform authentication before executing commands',
                    'baseType': 'BOOLEAN',
                    'defaultValue': true,
                    'isBindingSource': false,
                    'isBindingTarget': true
                }
            }
        };
    };

    this.customWidgetEvents = {
        'CommandTyped': {},
        "AuthenticationAttempt": {}
    };

    this.customWidgetServices = {
        'Clear': {'warnIfNotBound': false}
    };

    this.widgetEvents = function () {
        return this.customWidgetEvents;
    };

    this.widgetServices = function () {
        return this.customWidgetServices;
    };

    this.renderHtml = function () {
        var html = '';
        html += '<div class="widget-content widget-terminal">';
        html += '<img src="../Common/extensions/terminal-widget/ui/terminalWidget/terminalWidget.ide.png" />';
        html += '</div>';
        return html;
    };

    this.afterSetProperty = function (name, value) {
        return false;
    };
};