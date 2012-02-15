Ext.define('istsos.view.pages.ContactInfo', {
    extend: 'istsos.view.ui.CenterPage',
    requires: 'istsos.view.FormContacts',
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.setTitle("Setting / Contacts");
        me.setBody(Ext.create('istsos.view.FormContacts'));
    }
});