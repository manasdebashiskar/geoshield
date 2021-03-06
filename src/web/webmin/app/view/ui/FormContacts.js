/*
 * File: app/view/ui/FormContacts.js
 * Date: Thu Jan 26 2012 10:25:23 GMT+0100 (CET)
 *
 * This file was generated by Ext Designer version 1.2.2.
 * http://www.sencha.com/products/designer/
 *
 * This file will be auto-generated each and everytime you export.
 *
 * Do NOT hand edit this file.
 */

Ext.define('istsos.view.ui.FormContacts', {
    extend: 'Ext.form.Panel',

    border: 0,
    bodyPadding: 10,
    title: '',

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            dockedItems: [
                {
                    xtype: 'toolbar',
                    ui: 'footer',
                    dock: 'bottom',
                    layout: {
                        pack: 'end',
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'button',
                            text: 'Cancel'
                        },
                        {
                            xtype: 'button',
                            text: 'Submit'
                        }
                    ]
                }
            ],
            items: [
                {
                    xtype: 'fieldset',
                    title: 'Authority',
                    items: [
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Name',
                            anchor: '100%'
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Version',
                            anchor: '100%'
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: 'Service Identification:',
                    items: [
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Title',
                            anchor: '100%'
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Abstract',
                            anchor: '100%'
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Keywords',
                            anchor: '100%'
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Fees',
                            anchor: '100%'
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Access constrains',
                            anchor: '100%'
                        }
                    ]
                }
            ]
        });

        me.callParent(arguments);
    }
});