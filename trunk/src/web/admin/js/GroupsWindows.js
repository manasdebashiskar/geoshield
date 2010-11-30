

MyDesktop.GroupManagerWindow = Ext.extend(Ext.app.Module, {
    id:'group-manager-win',
    store: null,
    init : function(){
        this.launcher = {
            text: 'Groups',
            iconCls:'icon-grid',
            handler : this.createWindow,
            scope: this
        }
    },
    createWindow : function(options){
        
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('group-manager-win');

        if(!win){
            
            var grid = new GeoShield.Groups.Grid();
            
            // Window taskbar buttons
            var addBut = new Ext.Button({
                text:'Add group',
                tooltip:'Create a new group',
                iconCls:'usr_add',
                listeners: {
                    'click': {
                        fn: function(e, t){
                            var module = MyDesktop.getModule('simple-group-form-win');
                            if(module){
                                module.createWindow();
                            }
                        }
                    }
                }
            });

            var delBut = new Ext.Button({
                id: 'grpDelBut',
                text:'Delete',
                tooltip:'Delete selected group',
                iconCls:'usr_delete',
                disabled: true
            });

            delBut.on('click',
                function(e, t){

                    var record = this.getSelected();
                    var idGrp = record.get("idGrp");

                    Ext.MessageBox.show({
                        title:'Delete group?',
                        msg: 'Are you sure you want to delete group "'+
                        record.get("nameGrp")+'"?',
                        buttons: Ext.MessageBox.YESNO,
                        scope: this,
                        fn: function(btn){
                            if(btn=='yes'){
                                Ext.Ajax.request({
                                    url: 'admin/GroupAccountCtr',
                                    params: {
                                        REQUEST : 'deleteGroup',
                                        idGrp   : idGrp
                                    },
                                    success: function(result, request){
                                        var jsonRes = Ext.util.JSON.decode(result.responseText);
                                        if (jsonRes.success == false) {
                                            Ext.Info.msg('Group remove', String.escape(jsonRes.error));
                                        } else {
                                            Ext.Info.msg('Group remove', String.escape(jsonRes.message));
                                            GeoShield.Users.GridStore.load();
                                            GeoShield.Groups.GridStore.load();
                                        }
                                    },
                                    failure:  function(result, request) {
                                        Ext.Info.msg('Group remove', 'Connection');
                                        Ext.Msg.show({
                                            title:'Warning',
                                            msg: 'Server message:<br>',
                                            buttons: Ext.Msg.OK,
                                            animEl: 'grpDelBut',
                                            icon: Ext.MessageBox.WARNING
                                        });
                                    }
                                });
                            }
                        }
                    });
                }, grid.getSelectionModel());

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
                
            var propBut = new Ext.Button({
                text:'Edit',
                tooltip:'Modify selected group',
                iconCls:'option',
                disabled: true
            });

            propBut.on('click',
                function(e, t){
                    var module = MyDesktop.getModule('complex-group-form-win');
                    if(module){
                        module.createWindow(this.getSelected());
                    }
                }, grid.getSelectionModel()
                );


            GeoShield.Groups.GridStore.getStore().on("load",
                function(){
                    delBut.disable();
                    propBut.disable();
                });

            win = desktop.createWindow({
                id       : 'group-manager-win',
                title    : 'Group accounts',
                closable : true,
                iconCls  : 'icon-grid', 
                width    : 300,
                height   : 350,
                shim     :false, 
                animCollapse   :false,
                constrainHeader:true,
                layout   : 'fit',
                waitMsg:'Loading Data...',
                waitMsgTarget: true,
                items    : [grid],
                tbar:[addBut,delBut,propBut],
                myOnLoad : function(c){
                    GeoShield.Groups.GridStore.load();
                    this.un('show',win.myOnLoad);
                }
            });
            win.on('show',win.myOnLoad);
        }
        win.show();
    }
});


MyDesktop.SimpleGroupFormWindow = Ext.extend(Ext.app.Module, {
    id:'simple-group-form-win',
    init : function(){
        this.request = "insertGroup";
    },
    createWindow : function(options){

        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('simple-group-form-win');

        if(!win){
            
            var form = new GeoShield.Groups.Form();
            
            form.addButton({
                text: 'Save',
                disabled:false,
                scope: form,
                handler: function(){
                    this.getForm().submit({
                        url: "admin/GroupAccountCtr",
                        params: {
                            REQUEST: "insertGroup"
                        },
                        scope: this,
                        waitMsg:"Saving Data...",
                        success: function (form, action) {
                            Ext.Info.msg('Group Editor', 'New user added.');
                            GeoShield.Users.GridStore.load();
                            GeoShield.Groups.GridStore.load();
                            var win = MyDesktop.desktop.getWindow("simple-group-form-win");
                            win.close();
                        },
                        failure:function(form, action) {
                            Ext.Msg.show({
                                title:'Warning',
                                msg: 'Server message:<br>'+action.result.error,
                                buttons: Ext.Msg.OK,
                                animEl: 'simple-group-form-win',
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                }
            });
            
            win = desktop.createWindow({
                id       : 'simple-group-form-win',
                title    : 'Group Editor',
                closable : true,
                //iconCls  : 'group_form',
                width    : 280,
                height   : 120,
                layout   : 'fit',
                modal    : true,
                items    : [form]
            });
        }
        win.show();
    }
});

MyDesktop.ComplexGroupFormWindow = Ext.extend(Ext.app.Module, {
    id:'complex-group-form-win',
    groupForm: null,
    request: null,
    nameGrp: null,
    init : function(){
        this.request = "insertGroup";
    },
    loadRecord: function(record){
        this.groupForm.getForm().loadRecord(record);
        this.groupForm.requestParam = "updateGroup";
    //this.store.load();
    },
    createWindow : function(record){

        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('complex-group-form-win');
        
        if(!win){
            
            this.groupForm = new GeoShield.Groups.Form({
                id: 'real-group-form-win'
            });

            this.grid = new GeoShield.Users.GridMembers({
                title: ""
            });
            this.grid.getStore().removeAll();

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
                        module.loadRecord(this.getSelected());
                    }
                }, this.grid.getSelectionModel() 
                );

            this.grid.on('rowclick',
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
                
            var membGridPan = new Ext.Panel({
                title: "Members",
                border:false,
                layout:'fit',
                items: [this.grid],
                tbar: [addBut,delBut,propBut]
            });

            delBut.on('click',
                function(e,t){
                    var record = this.getSelected();
                    Ext.MessageBox.show({
                        title:'Delete user?',
                        msg: 'Are you sure you want to delete user "'+
                        record.get("nameUsr")+'"?',
                        buttons: Ext.MessageBox.YESNO, 
                        scope: this.grid.getSelectionModel(),
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
                }, this.grid.getSelectionModel()
                );
            
            this.grid.getSelectionModel().on('evToggleChecked',
                function( rowIndex, ev){
                    var record = this.grid.getStore().getAt(rowIndex);
                    var idUsr = record.get("idUsr");
                    var isMember = record.get('hasGroup');
                    var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                    Ext.Ajax.request({
                        url: 'admin/GroupAccountCtr',
                        params: {
                            REQUEST : 'setUserGroup',
                            idUsr   : idUsr,
                            isMember: isMember,
                            idGrp   : idGrp
                        },
                        success: function(result, request){
                            var jsonRes = Ext.util.JSON.decode(result.responseText);
                            if (jsonRes.success == false) {
                                Ext.Msg.show({
                                    title:'Warning',
                                    msg: 'Server message:<br>'+String.escape(jsonRes.error),
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.WARNING
                                });
                            } else {
                                Ext.Info.msg('Membership', String.escape(jsonRes.message));
                            }
                        },
                        failure:  function(result, request) {
                            Ext.Msg.show({
                                title:'Warning',
                                msg: String.escape(result.responseText),
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                });


            var nav = new Ext.Panel({
                region      : 'north',
                split       : false,
                width       : 200,
                height      : 50,
                layout      : 'fit',
                collapsible : false,
                items    : [this.groupForm]
            });

            // *****************************************************************
            // Services Permissions Panel -START
            // *****************************************************************

            var sprGrid = new GeoShield.Services.Grid();

            sprGrid.getSelectionModel().on('SmartRowClick',
                function( grid, rowIndex, ev){
                    var record = grid.store.getAt(rowIndex);
                    var idSrv = record.get("idSrv");
                    var idSpr = record.get("idSpr");
                    if(idSpr==-1){
                        GeoShield.Services.ReqStore.getStore().removeAll();
                        return;
                    }
                    GeoShield.Services.ReqStore.idSpr = idSpr;
                    GeoShield.Services.ReqStore.load("idSrvFk;"+idSrv);
                },this);


            // Add o remove servicesPermissions row x group
            sprGrid.getSelectionModel().on('evToggleChecked',
                function( rowIndex, c){
                    var record = GeoShield.Services.Store.getStore().getAt(rowIndex);
                    var idSur = record.get("idSur");
                    var hasSpr = c;
                    var idGrp = record.get("idGrp");
                    Ext.Ajax.request({
                        url: 'admin/ServicesManagerCtr',
                        params: {
                            REQUEST : 'setServicesPermissions',
                            idSur   : idSur,
                            hasSpr  : hasSpr,
                            idGrp   : idGrp
                        },
                        success: function(result, request){
                            var jsonRes = Ext.util.JSON.decode(result.responseText);
                            if (jsonRes.success == false) {
                                Ext.Msg.show({
                                    title:'Warning',
                                    msg: 'Server message:<br>'+String.escape(jsonRes.error),
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.WARNING
                                });
                            } else {
                                Ext.Info.msg('Services Permissions', String.escape(jsonRes.message));
                            }
                            GeoShield.Services.Store.load();
                            GeoShield.Services.ReqStore.getStore().removeAll();
                            GeoShield.Services.ReqStore.idSpr = undefined;
                            GeoShield.ServicesUrls.Store.load();
                        },
                        failure:  function(result, request) {
                            Ext.Msg.show({
                                title:'Warning',
                                msg: String.escape(result.responseText),
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                });

            var srvPrmAddBut = new Ext.Button({
                text:'Add service',
                tooltip:'Add a new service',
                iconCls:'add',
                listeners: {
                    'click': {
                        fn: function(e, t){
                            var module = MyDesktop.getModule('srv-form-win');
                            if(module){
                                module.createWindow();
                            }
                        }
                    }
                }
            });

            var srvPrmDelBut = new Ext.Button({
                id: 'delBut',
                text:'Delete',
                tooltip:'Delete selected service',
                iconCls:'remove',
                disabled: true
            });

            srvPrmDelBut.on('click',
                function(e,t){
                    var record = this.getSelected();
                    Ext.MessageBox.show({
                        title:'Delete Service Permission?',
                        msg: 'Are you sure you want to delete completely the service and '+
                        'all the permissions with other groups connected?',
                        buttons: Ext.MessageBox.YESNO,
                        scope: this.grid.getSelectionModel(),
                        fn: function(btn){
                            if(btn=='yes'){
                                Ext.MessageBox.wait('Please wait','Deleting service..');
                                if(this.getCount() == 1){
                                    var rec = this.getSelected();
                                    var idSur = rec.get("idSur");
                                    Ext.Ajax.request({
                                        url: 'admin/ServicesManagerCtr',
                                        params: {
                                            REQUEST: 'deleteService',
                                            idSur: idSur
                                        },
                                        success: function(result, request){
                                            Ext.MessageBox.hide();
                                            var jsonRes = Ext.util.JSON.decode(result.responseText);
                                            if (jsonRes.success == false) {
                                                Ext.Msg.show({
                                                    title:'Warning',
                                                    msg: 'Server message:<br>'+String.escape(jsonRes.error),
                                                    buttons: Ext.Msg.OK,
                                                    icon: Ext.MessageBox.WARNING
                                                });
                                            } else {
                                                Ext.Info.msg('Services Permissions', String.escape(jsonRes.message));
                                            }
                                            GeoShield.Services.Store.load();
                                            GeoShield.ServicesUrls.Store.load();
                                            GeoShield.Layers.Store.load();
                                        },
                                        failure:  function(result, request) {
                                            Ext.MessageBox.hide();
                                            Ext.Msg.show({
                                                title:'Warning',
                                                msg: String.escape(result.responseText),
                                                buttons: Ext.Msg.OK,
                                                icon: Ext.MessageBox.WARNING
                                            });
                                        }
                                    });
                                }
                                else if(this.getCount() > 1){
                                    var recs = this.getSelections();
                                }
                            }
                        }
                    });
                }, sprGrid.getSelectionModel() 
                );

            var srvPrmPropBut = new Ext.Button({
                text:'Edit',
                tooltip:'Modify selected service',
                iconCls:'option',
                disabled: true
            });


            srvPrmPropBut.on('click',
                function(e, t){
                    var module = MyDesktop.getModule('srv-form-win');
                    if(module){
                        module.createWindow();
                        module.loadRecord(this.getSelected());
                    }
                }, sprGrid.getSelectionModel()
                );

            var srvPrmGridCont = new Ext.Panel({
                region      : 'center',
                split       : false,
                layout      : 'fit',
                collapsible : false,
                items: [sprGrid],
                tbar: [srvPrmAddBut,srvPrmDelBut,srvPrmPropBut]
            });


            sprGrid.on('rowclick',
                function( grid, rowIndex, ev){
                    if(grid.selModel.getCount()>0){
                        srvPrmDelBut.enable();
                        if(grid.selModel.getCount()==1){
                            srvPrmPropBut.enable();
                        }else{
                            srvPrmPropBut.disable();
                        }
                    }else{
                        srvPrmDelBut.disable();
                        srvPrmPropBut.disable();
                    }
                });
                
            var reqsGrid = new GeoShield.Services.ReqGrid();

            reqsGrid.getSelectionModel().on('evToggleChecked',
                function( rowIndex, c){
                    var record = this.getStore().getAt(rowIndex);
                    var idReq = record.get("idReq");
                    var hasSre = c;
                    var idSpr = record.get("idSpr");
                    Ext.Ajax.request({
                        url: './admin/ServicesManagerCtr',
                        params: {
                            REQUEST : 'setSprReq',
                            idReq   : idReq,
                            hasSre  : hasSre,
                            idSpr   : idSpr
                        },
                        success: function(result, request){
                            var jsonRes = Ext.util.JSON.decode(result.responseText);
                            if (jsonRes.success == false) {
                                Ext.Msg.show({
                                    title:'Warning',
                                    msg: 'Server message:<br>'+String.escape(jsonRes.error),
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.WARNING
                                });
                            } else {
                                Ext.Info.msg('Services Requests', String.escape(jsonRes.message));
                            }
                            GeoShield.Services.ReqStore.load();
                        },
                        failure:  function(result, request) {
                            Ext.Msg.show({
                                title:'Warning',
                                msg: 'Server message:<br>'+String.escape(result.responseText),
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                },reqsGrid);

            var srvPrmFormCont = new Ext.Panel({
                region      : 'east',
                split       : true,
                layout      : 'fit',
                collapsible : true,
                width       : 300,
                items: [
                new Ext.Panel({
                    deferredRender: false,
                    border: false,
                    layout: 'border',
                    items: [
                    new Ext.Panel({
                        id: "sprInfo",
                        region      : 'south',
                        split       : false,
                        width       : 300,
                        height      : 20,
                        layout      : 'fit',
                        collapsible : false,
                        border: false,
                        bodyStyle:'padding: 5px 5px 0px 5px',
                        html: "Note: select available Services from left panel."
                    }),
                    new Ext.Panel({
                        id: "sprCenter",
                        region      : 'center',
                        split       : false,
                        layout      : 'fit',
                        collapsible : false,
                        border: false,
                        items    : [reqsGrid]
                    })
                    ]
                })
                ]
            });

            // *****************************************************************
            // Services Permissions Panel - END
            // *****************************************************************

            // *****************************************************************
            // Layers Permissions Panel -START
            // *****************************************************************

            var surCombo = new GeoShield.ServicesUrls.ComboBox();

            var sync = new Ext.Button({
                text:'Sync',
                tooltip:'Syncronize database with service',
                iconCls:'world',
                disabled: false
            });
            
            var addLay = new Ext.Button({
                text:'Add',
                tooltip:'Add a layer manually',
                iconCls:'add',
                disabled: false,
                listeners: {
                    'click': {
                        fn: function(e, t){
                            var module = MyDesktop.getModule('simple-layers-form-win');
                            if(module){
                                module.createWindow();
                            }
                        }
                    }
                }
            });
            var remLay = new Ext.Button({
                text:'Rem',
                tooltip:'Remove the selected layer',
                iconCls:'remove',
                disabled: true
            });

            var editLay = new Ext.Button({
                text:'Edit',
                tooltip:'Modify selected layer',
                iconCls:'option',
                disabled: true
            });


            var layGrid = new GeoShield.Layers.Grid({
                tbar:[addLay,remLay,editLay,sync] 
            });
            var layForm = new GeoShield.LayersPermissions.Form();

            layGrid.on('rowclick',
                function( grid, rowIndex, ev){
                    if(grid.selModel.getCount()>0){
                        remLay.enable();
                        if(grid.selModel.getCount()==1){
                            editLay.enable();
                        }else{
                            editLay.disable();
                        }
                    }else{
                        remLay.disable();
                        editLay.disable();
                    }
                });

            layGrid.getStore().on("load",
                function(){
                    remLay.disable();
                    editLay.disable();
                });
                
            remLay.on('click',
                function(e,t){
                    var record = this.getSelected();
                    Ext.MessageBox.show({
                        title:'Delete Layer?',
                        msg: 'Are you sure you want to delete completely the Layer and '+
                        'all the CQL group-filters connected to it?',
                        buttons: Ext.MessageBox.YESNO,
                        scope: this.grid.getSelectionModel(),
                        fn: function(btn){
                            if(btn=='yes'){
                                Ext.MessageBox.wait('Please wait','Deleting layer..');
                                if(this.getCount() == 1){
                                    var rec = this.getSelected();
                                    var idLay = rec.get("idLay");
                                    Ext.Ajax.request({
                                        url: 'admin/LayersManagerCtr',
                                        params: {
                                            REQUEST: 'deleteLayer',
                                            idLay: idLay
                                        },
                                        success: function(result, request){
                                            Ext.MessageBox.hide();
                                            var jsonRes = Ext.util.JSON.decode(result.responseText);
                                            if (jsonRes.success == false) {
                                                Ext.Msg.show({
                                                    title:'Warning',
                                                    msg: 'Server message:<br>'+String.escape(jsonRes.error),
                                                    buttons: Ext.Msg.OK,
                                                    icon: Ext.MessageBox.WARNING
                                                });
                                            } else {
                                                Ext.Info.msg('Layers', String.escape(jsonRes.message));
                                            }
                                            GeoShield.Layers.Store.load();
                                        },
                                        failure:  function(result, request) {
                                            Ext.MessageBox.hide();
                                            Ext.Msg.show({
                                                title:'Warning',
                                                msg: String.escape(result.responseText),
                                                buttons: Ext.Msg.OK,
                                                icon: Ext.MessageBox.WARNING
                                            });
                                        }
                                    });
                                }
                                else if(this.getCount() > 1){
                                    var recs = this.getSelections();
                                }
                            }
                        }
                    });
                }, layGrid.getSelectionModel()
                );
                    
            editLay.on('click',
                function(e, t){
                    var module = MyDesktop.getModule('simple-layers-form-win');
                    if(module){
                        module.createWindow();
                        module.loadRecord(this.getSelected());
                    }
                }, layGrid.getSelectionModel()
                );

            layGrid.getSelectionModel().on('beforeToggleChecked',
                function( rowIndex, c){
                    var record = GeoShield.Layers.Store.getStore().getAt(rowIndex);
                    var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                    var idLay = record.get("idLay");
                    var checked = c;
                    if(!c){
                        var btn = Ext.MessageBox.show({
                            title:'Delete layer permission?',
                            msg: 'Are you sure you want to delete this layer permission?<br><br>'+
                            '<small>Note: deleting it you will also delete the filter declaration.</small>',
                            buttons: Ext.MessageBox.YESNO,
                            scope: layForm,
                            fn: function(btn){
                                if(btn=='yes'){
                                    Ext.Ajax.request({
                                        url: 'admin/LayersManagerCtr',
                                        scope: this,
                                        params: {
                                            REQUEST : 'checkLayersPermissions',
                                            checked : checked,
                                            idLay   : idLay,
                                            idGrp   : idGrp
                                        },
                                        success: function(result, request){
                                            var jsonRes = Ext.util.JSON.decode(result.responseText);
                                            if (jsonRes.success == false) {
                                                Ext.Msg.show({
                                                    title:'Warning',
                                                    msg: 'Layers permissions:<br>'+String.escape(jsonRes.error),
                                                    buttons: Ext.Msg.OK,
                                                    icon: Ext.MessageBox.WARNING
                                                });
                                            } else {
                                                Ext.Info.msg('Layers permissions', String.escape(jsonRes.message));
                                            }
                                            GeoShield.Layers.Store.load();
                                            layForm.getForm().reset();
                                            this.disable();
                                        },
                                        failure:  function(result, request) {
                                            Ext.Msg.show({
                                                title:'Warning',
                                                msg: 'Server message:<br>'+String.escape(result.responseText),
                                                buttons: Ext.Msg.OK,
                                                icon: Ext.MessageBox.WARNING
                                            });
                                        }
                                    });
                                }
                            }
                        });
                        return false;
                    }else{
                        return true;
                    }
                });

            layGrid.getSelectionModel().on('evToggleChecked',
                function( rowIndex, c){
                    var record = GeoShield.Layers.Store.getStore().getAt(rowIndex);
                    var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                    var idLay = record.get("idLay");
                    var checked = c;
                    Ext.Ajax.request({
                        url: 'admin/LayersManagerCtr',
                        scope: layForm,
                        params: {
                            REQUEST : 'checkLayersPermissions',
                            checked : checked,
                            idLay   : idLay,
                            idGrp   : idGrp
                        },
                        success: function(result, request){
                            var jsonRes = Ext.util.JSON.decode(result.responseText);
                            if (jsonRes.success == false) {
                                Ext.Msg.show({
                                    title:'Warning',
                                    msg: 'Layers permissions:<br>'+String.escape(jsonRes.error),
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.WARNING
                                });
                            } else {
                                Ext.Info.msg('Layers permissions', String.escape(jsonRes.message));
                            }
                            GeoShield.Layers.Store.load();
                            layForm.getForm().reset();
                            this.disable();
                        },
                        failure:  function(result, request) {
                            Ext.Msg.show({
                                title:'Warning',
                                msg: 'Server message:<br>'+String.escape(result.responseText),
                                buttons: Ext.Msg.OK,
                                icon: Ext.MessageBox.WARNING
                            });
                        }
                    });
                });

            layGrid.on('rowclick',
                function( grid, rowIndex, ev){
                    var record = grid.getStore().getAt(rowIndex);
                    var LprRecord = Ext.data.Record.create([
                    {
                        name: 'idLpr',
                        type: 'integer'
                    },
                    {
                        name: 'idLay',
                        type: 'integer'
                    },
                    {
                        name: 'idGrp',
                        type: 'integer'
                    },
                    {
                        name: 'filterLpr',
                        type: 'string'
                    }
                    ]);
                    var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                    var layPerList = record.get("layPerList");
                    for (var i = 0; i < layPerList.length; i++) {
                        if(layPerList[i].idGrpFk.idGrp == idGrp){
                            this.enable();
                            this.getForm().loadRecord(
                                new LprRecord({
                                    idLpr: layPerList[i].idLpr,
                                    idLay: record.get("idLay"),
                                    idGrp: idGrp,
                                    filterLpr: layPerList[i].filterLpr
                                }));
                            return;
                        }
                    }
                    this.getForm().reset();
                    this.disable();
                },layForm);

            surCombo.on("select",
                function(combo, record, index ){
                    var idSur = record.get("idSur");
                    GeoShield.Layers.Store.load("idSur;"+idSur);
                });

            var layComboPan = new Ext.Panel({
                region      : 'north',
                split       : false,
                layout      : 'fit',
                collapsible : false,
                height      : 35,
                items       : [
                new Ext.FormPanel({
                    labelWidth: 150, // label settings here cascade unless overridden
                    bodyStyle:'padding:5px 5px 0',
                    width: 300,
                    frame: false,
                    defaultType: 'textfield',
                    border:false,
                    defaults: {
                        width: 200
                    },
                    items: [surCombo]
                })
                ]
            });

            var layFormPan = new Ext.Panel({
                region      : 'east',
                split       : true,
                layout      : 'fit',
                collapsible : true,
                width       : 600,
                items: [layForm]
            });

            layFormPan.addButton({
                text: 'Validate',
                disabled: true
            });

            layFormPan.addButton({
                text: 'Apply',
                disabled:false,
                scope: layForm,
                handler: function(){
                    this.form.submit({
                        url: './admin/LayersManagerCtr',
                        params: {
                            REQUEST: "setLayersPermissions"
                        },
                        waitMsg:'Saving Data...',
                        success: function (form, action) {
                            GeoShield.Layers.Store.load();
                            Ext.Info.msg('CQL Filter', 'UPDATE SUCCESSFULL.');
                        },
                        failure:function(form, action) {
                            if (action.failureType != undefined) {
                                Ext.Msg.show({
                                    title:'Warning',
                                    msg: 'Error type:<br>'+action.failureType,
                                    buttons: Ext.Msg.OK,
                                    icon: Ext.MessageBox.WARNING
                                });
                            }else{
                                Ext.Msg.show({
                                    title:'Warning',
                                    msg: 'Server message:<br>'+action.result.error,
                                    buttons: Ext.Msg.OK,
                                    //animEl: 'srv-form-win',
                                    icon: Ext.MessageBox.WARNING
                                });
                            }
                        }
                    });
                }
            });

            var layGridPan = new Ext.Panel({
                region      : 'center',
                split       : true,
                layout      : 'fit',
                collapsible : true,
                width       : 280,
                items       :[layGrid]
            });
            
            // *****************************************************************
            // Layers Permissions Panel - END
            // *****************************************************************

            // *****************************************************************
            // Panel for Content with tab panel - START
            // *****************************************************************

            var cont = new Ext.Panel({
                region      : 'center',
                split       : false,
                width       : 160,
                layout      : 'fit',
                collapsible : false,
                items:
                new Ext.TabPanel({
                    activeTab:0,
                    items: [
                    membGridPan,
                    new Ext.Panel({
                        title: 'Services permissions',
                        border:false,
                        layout:'border',
                        items: [srvPrmFormCont, srvPrmGridCont]
                    }),
                    new Ext.Panel({
                        title : 'WMS/WFS permissions',
                        border : false,
                        layout : 'border',
                        items : [layComboPan, layFormPan, layGridPan]
                    })]
                })
            });

            win = desktop.createWindow({
                id       : 'complex-group-form-win',
                title    : 'Group properties',
                closable : true,
                iconCls  : 'group_form',
                width    : 850,
                height   : 400,
                //border : false,
                plain    : true,
                layout   : 'border',
                hideBorders: true,
                items    : [nav, cont/*,buts*/],
                myOnLoad : function(c){
                    var module = MyDesktop.getModule('complex-group-form-win');
                    if(module){
                        module.groupForm.getForm().loadRecord(record);
                        GeoShield.Users.MembersStore.load();
                        GeoShield.Services.Store.load();
                        GeoShield.ServicesUrls.Store.load("idGrp;"+Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue());
                    //module.store.load();
                    //module.srvPrmStore.load();
                    }
                    this.un('show',win.myOnLoad);
                }
            });

            win.on('show',win.myOnLoad);

        /*
            cancel.on('click',
                function(e,t){
                    this.close();
                }, win
                );

            if(record != undefined){
                this.loadRecord(record);
            }else{
                cont.hide();
            }
                 */
        }
        win.show();
    }
});
