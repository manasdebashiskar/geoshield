Ext.define('istsos.view.pages.AboutIstsos', {
    extend: 'istsos.view.ui.CenterPage',
    requires: 'istsos.view.FormAboutIstsos',
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.setTitle("About / About istSOS");
        me.setBody(Ext.create('istsos.view.FormAboutIstsos'));
    }
});