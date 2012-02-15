Ext.define('istsos.view.pages.ContactInfo', {
    extend: 'istsos.view.ui.CenterPage',
    requires: 'istsos.view.FormContactInfo',
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.setTitle("About / Contact Info");
        me.setBody(Ext.create('istsos.view.FormContactInfo'));
    }
});