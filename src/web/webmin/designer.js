/*
 * File: designer.js
 * Date: Thu Jan 26 2012 10:25:23 GMT+0100 (CET)
 *
 * This file was generated by Ext Designer version 1.2.2.
 * http://www.sencha.com/products/designer/
 *
 * This file will be auto-generated each and everytime you export.
 *
 * Do NOT hand edit this file.
 */

Ext.Loader.setConfig({
    enabled: true
});

Ext.application({
    name: 'istsos',

    stores: [
        'DataQualityStore'
    ],

    launch: function() {
        Ext.QuickTips.init();

        var cmp1 = Ext.create('istsos.view.MainPanel', {
            renderTo: Ext.getBody()
        });
        cmp1.show();
        var cmp2 = Ext.create('istsos.view.BasePage', {
            renderTo: Ext.getBody()
        });
        cmp2.show();
        var cmp3 = Ext.create('istsos.view.FormContactInfo', {
            renderTo: Ext.getBody()
        });
        cmp3.show();
        var cmp4 = Ext.create('istsos.view.FormAbout', {
            renderTo: Ext.getBody()
        });
        cmp4.show();
        var cmp5 = Ext.create('istsos.view.FormContacts', {
            renderTo: Ext.getBody()
        });
        cmp5.show();
        var cmp6 = Ext.create('istsos.view.FormConfig', {
            renderTo: Ext.getBody()
        });
        cmp6.show();
        var cmp7 = Ext.create('istsos.view.FormDatabase', {
            renderTo: Ext.getBody()
        });
        cmp7.show();
        var cmp8 = Ext.create('istsos.view.FormDataQuality', {
            renderTo: Ext.getBody()
        });
        cmp8.show();
    }
});
