Ext.define('istsos.view.MainPanel', {
    extend: 'istsos.view.ui.MainPanel',
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        Ext.getCmp("mainMenu").update(istsos.engine.pageManager.getMenuHtml());
    }
});