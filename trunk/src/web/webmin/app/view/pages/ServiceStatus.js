Ext.define('istsos.view.pages.ServiceStatus', {
    extend: 'istsos.view.ui.CenterPage',
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.setTitle("About / Service Status");
        me.setBody(
            "<p><STRONG>Welcome</STRONG></p>"+
            "<p>The istSOS service belong to the Institute of Earth Science</p>");
    }
});