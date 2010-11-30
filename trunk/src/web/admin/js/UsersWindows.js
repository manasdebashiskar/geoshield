
MyDesktop.UserManagerWindow = Ext.extend(Ext.app.Module, {
    id:'user-manager-win',
    store: null,
    init : function(){
        this.launcher = {
            text: 'user-manager-win',
            iconCls:'icon-grid',
            handler : this.createWindow,
            scope: this
        }
    },
    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('user-manager-win');
        if(!win){

            var grid = new GeoShield.Users.Grid({
                store: GeoShield.Users.GridStore.getStore()
            });

            var addBut = new Ext.Button({
                text:'Add user',
                tooltip:'Create a new user',
                iconCls:'usr_add',
                listeners: {
                    'click': {
                        fn: function(e, t){
                            var module = MyDesktop.getModule('user-form-win');
                            if(module){
                                module.createWindow();
                            }
                        }
                    }
                }
            });

            var delBut = new Ext.Button({
                id: 'delBut',
                text:'Delete',
                tooltip:'Delete selected user',
                iconCls:'usr_delete',
                disabled: true
            });

            delBut.on('click',
                function(e,t){
                    var record = this.getSelected();
                    Ext.MessageBox.show({
                        title:'Delete user?',
                        msg: 'Are you sure you want to delete user "'+
                        record.get("nameUsr")+'"?',
                        buttons: Ext.MessageBox.YESNO,
                        scope: grid.getSelectionModel(),
                        fn: function(btn){
                            if(btn=='yes'){
                                Ext.MessageBox.wait('Please wait','Deleting user..');
                                if(this.getCount() == 1){
                                    var rec = this.getSelected();
                                    var name = rec.data.nameUsr;
                                    Ext.Ajax.request({
                                        url: './admin/UserAccountCtr',
                                        params: {
                                            REQUEST: 'deleteUser',
                                            idUsr: rec.data.idUsr
                                        },
                                        success: function(){
                                            GeoShield.Users.GridStore.load();
                                            GeoShield.Groups.GridStore.load();
                                            GeoShield.Users.MembersStore.load();
                                            Ext.MessageBox.hide();
                                            Ext.Info.msg('Delete user', 'User {0} deleted from database.', name);
                                        },
                                        failure:  function(form, action) {
                                            Ext.MessageBox.hide();
                                            Ext.Msg.show({
                                                title:'Warning',
                                                msg: 'Server message:<br>'+action.result.error,
                                                buttons: Ext.Msg.OK,
                                                animEl: 'delBut',
                                                icon: Ext.MessageBox.WARNING
                                            });
                                        }
                                    });
                                }
                                else if(this.getCount() > 1){
                                    var recs = this.getSelections();
                                }
                            }
                        },
                        animEl: 'mb4'
                    });
                }, grid.getSelectionModel()
                );

            var propBut = new Ext.Button({
                text:'Edit',
                tooltip:'Modify selected user',
                iconCls:'option',
                disabled: true
            });

            propBut.on('click',
                function(e, t){
                    var module = MyDesktop.getModule('user-form-win');
                    if(module){
                        module.createWindow();
                        //console.dir(this.getSelected());
                        module.loadRecord(this.getSelected());
                    }
                }, grid.getSelectionModel()
                );

            var grpBut = new Ext.Button({
                text:'Manage Groups',
                tooltip:'Open user-group control panel',
                iconCls:'group'
            });

            grid.on('rowclick',
                function( grid, rowIndex, ev){
                    if(grid.selModel.getCount()>0){
                        delBut.enable();
                        if(grid.selModel.getCount()==1){
                            propBut.enable();
                        }else{
                            propBut.disable();
                        }
                    }else{
                        delBut.disable();
                        propBut.disable();
                    }
                });

            GeoShield.Users.GridStore.getStore().on('load',
                function(e){
                    if(this.getSelectionModel().getCount()>0){
                        delBut.enable();
                        if(this.getSelectionModel().getCount()==1){
                            propBut.enable();
                        }else{
                            propBut.disable();
                        }
                    }else{
                        delBut.disable();
                        propBut.disable();
                    }
                }, grid
                );

            win = desktop.createWindow({
                id       : 'user-manager-win',
                title    : 'User accounts',
                closable : true,
                iconCls: 'icon-grid',
                width    : 700,
                height   : 350,
                shim     :false,
                animCollapse   :false,
                constrainHeader:true,
                layout   : 'fit',
                waitMsg:'Loading Data...',
                waitMsgTarget: true,
                items    : [grid],
                tbar:[addBut,delBut,propBut,grpBut],
                myOnLoad : function(c){
                    GeoShield.Users.GridStore.load();
                    this.un('show',win.myOnLoad);
                }
            });
            win.on('show',win.myOnLoad);
        }
        win.show();
    }
});

MyDesktop.UserFormWindow = Ext.extend(Ext.app.Module, {
    id:'user-form-win',
    userForm: null,
    request: null,
    init : function(){
        this.request = "insertUser";
        this.launcher = {
            text: 'user-form-win',
            iconCls:'icon-grid',
            handler : this.createWindow,
            scope: this
        }
    },
    loadRecord: function(record) {
        this.userForm.getForm().loadRecord(record);
        this.userForm.requestParam = "updateUser";
        var mg = GeoShield.Users.getGridMembership({}, record);
        Ext.getCmp(this.tabId).add(mg);
        mg.store.load();
    },
    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('user-form-win');
        if(!win){
            // Panel for Content
            this.tabId = Ext.id();
            this.userForm = new Ext.FormPanel({
                id: 'real-user-form-win',
                title    : 'Details',
                labelWidth: 75, // label settings here cascade unless overridden
                bodyStyle:'padding:5px 5px 0',
                width: 120,
                requestParam: "insertUser",
                defaults: {
                    width: 150
                },
                defaultType: 'textfield',
                items: [
                {
                    fieldLabel: 'Id',
                    name: 'idUsr',
                    allowBlank:true,
                    hidden: true,
                    hideLabel: true
                },
                {
                    fieldLabel: "Attivo",
                    xtype: 'checkbox',
                    name: 'isActiveUsr',
                    allowBlank: true
                },
                {
                    fieldLabel: 'User name',
                    name: 'nameUsr',
                    allowBlank: false
                },
                {
                    fieldLabel: 'Password',
                    name: 'pswUsr',
                    allowBlank:false
                },
                {
                    fieldLabel: 'Fist name',
                    name: 'firstNameUsr',
                    allowBlank:true
                },
                {
                    fieldLabel: 'Last name',
                    name: 'lastNameUsr',
                    allowBlank:true
                },
                {
                    fieldLabel: 'Email',
                    name: 'emailUsr',
                    allowBlank:true
                },
                {
                    fieldLabel: "Ufficio",
                    name: 'officeUsr',
                    allowBlank: true
                },
                {
                    fieldLabel: "Telefono",
                    name: 'telUsr',
                    allowBlank: true
                },
                {
                    fieldLabel: "Fax",
                    name: 'faxUsr',
                    allowBlank: true
                },
                {
                    fieldLabel: "Indirizzo",
                    xtype: 'textarea',
                    name: 'addressUsr',
                    allowBlank: true
                }
                ]
            });

            var submit = this.userForm.addButton({
                text: 'Save',
                disabled:false,
                scope: this.userForm,
                handler: function(){
                    this.form.submit({
                        url: 'admin/UserAccountCtr?REQUEST='+this.requestParam,
                        waitMsg:'Saving Data...',
                        success: function (form, action) {
                            GeoShield.Users.GridStore.load();
                            GeoShield.Users.MembersStore.load();
                            GeoShield.Groups.GridStore.load();
                            //alert(form.requestParam);
                            if(form.requestParam == 'insertUser'){
                                form.reset();
                                Ext.Info.msg('User editor', 'New user added.');
                            }else{
                                Ext.Info.msg('User editor', 'Existing user updated.');
                            }
                        },
                        failure:function(form, action) {
                            Ext.Msg.show({
                                title:'Warning',
                                msg: 'Server message:<br>'+action.result.error,
                                buttons: Ext.Msg.OK,
                                animEl: 'user-form-win',
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                }
            });

            win = desktop.createWindow({
                id       : 'user-form-win',
                title    : 'User editor',
                closable : true,
                iconCls  : 'user_form',
                width    : 280,
                height   : 400,
                layout   : 'fit',
                modal    : false,
                items    : [
                new Ext.TabPanel({
                    activeTab: 0,
                    id: this.tabId,
                    frame:true,
                    defaults:{
                        autoHeight: true
                    },
                    items:[this.userForm]
                })
                ]
            });

        }
        win.show();
    }
});



MyDesktop.GroupsUserFormWindow = Ext.extend(Ext.app.Module, {
    id:'group-user-form-win',
    userForm: null,
    request: null,
    init : function(){
        this.request = "insertUser";
        this.launcher = {
            text: 'group-user-form-win',
            iconCls:'icon-grid',
            handler : this.createWindow,
            scope: this
        }
    },
    loadRecord: function(record,usrRecord) {
        this.userForm.getForm().loadRecord(record);
        this.userForm.requestParam = "updateGroupUser";
        this.userForm.findById('userField').setValue(usrRecord.get("lastNameUsr") + " " + usrRecord.get("firstNameUsr"));
    },
    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('group-user-form-win');
        if(!win){
            // Panel for Content
            this.userForm = new Ext.FormPanel({
                id: 'real-group-user-form-win',
                labelWidth: 75, // label settings here cascade unless overridden
                bodyStyle:'padding:5px 5px 0',
                width: 120,
                requestParam: "insertGroupUser",
                defaults: {
                    width: 150
                },
                defaultType: 'textfield',
                items: [
                {
                    fieldLabel: 'Id',
                    name: 'idGus',
                    allowBlank:true,
                    hidden: true,
                    hideLabel: true
                },
                {
                    fieldLabel: "User",
                    name: 'user',
                    id: 'userField',
                     disabledClass: '',
                    disabled: true
                },
                {
                    fieldLabel: "Group name",
                    name: 'nameGrp',
                     disabledClass: '',
                    disabled: true
                },
                {
                    fieldLabel: 'Expiration',
                    name: 'expirationGus',
                    allowBlank:true,
                    xtype: 'datefield',
                    format: 'd/m/Y'
                },
                {
                    fieldLabel: 'Invoice',
                    name: 'invoiceGus',
                    allowBlank:true
                }
                ]
            });

            var submit = this.userForm.addButton({
                text: 'Save',
                disabled:false,
                scope: this.userForm,
                handler: function(){
                    this.form.submit({
                        url: 'admin/UserAccountCtr',
                        params: {
                            REQUEST: this.requestParam
                        },
                        waitMsg:'Saving Data...',
                        success: function (form, action) {
                            //GeoShield.Users.GridStore.load();
                            GeoShield.Users.MembersStore.load();
                            //GeoShield.Groups.GridStore.load();
                            GeoShield.Users.MembershipStore.load();
                            //alert(form.requestParam);
                            Ext.Info.msg('Membership editor', 'Commit successfull');
                            /*
                            if(form.requestParam == 'insertGroupUser'){
                                form.reset();
                                Ext.Info.msg('User editor', 'New user added.');
                            }else{
                                Ext.Info.msg('User editor', 'Existing user updated.');
                            }
                            */
                        },
                        failure:function(form, action) {
                            Ext.Msg.show({
                                title:'Warning',
                                msg: 'Server message:<br>'+action.result.error,
                                buttons: Ext.Msg.OK,
                                animEl: 'user-form-win',
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                }
            });

            win = desktop.createWindow({
                id       : 'group-user-form-win',
                title    : 'Membership editor',
                closable : true,
                iconCls  : 'group_form',
                width    : 280,
                height   : 190,
                layout   : 'fit',
                modal    : true,
                items    : [this.userForm]
            });

        }
        win.show();
    }
});