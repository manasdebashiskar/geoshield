Ext.define('istsos.view.pages.ServiceLogs', {
    extend: 'istsos.view.ui.CenterPage',
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.setTitle("About / Service Logs");
        me.setBody(
            "<p><STRONG>Logs:</STRONG></p>");
    }
});