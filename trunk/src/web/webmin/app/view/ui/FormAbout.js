/*
 * File: app/view/ui/FormAbout.js
 * Date: Thu Jan 26 2012 10:25:23 GMT+0100 (CET)
 *
 * This file was generated by Ext Designer version 1.2.2.
 * http://www.sencha.com/products/designer/
 *
 * This file will be auto-generated each and everytime you export.
 *
 * Do NOT hand edit this file.
 */

Ext.define('istsos.view.ui.FormAbout', {
    extend: 'Ext.form.Panel',

    border: 0,
    height: 250,
    width: 400,
    bodyPadding: 10,
    title: '',

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'displayfield',
                    value: 'Version 2.1.1',
                    fieldLabel: 'Build information',
                    labelWidth: 140,
                    anchor: '100%'
                },
                {
                    xtype: 'displayfield',
                    value: 'Display Field',
                    fieldLabel: 'Build date',
                    anchor: '100%'
                }
            ]
        });

        me.callParent(arguments);
    }
});