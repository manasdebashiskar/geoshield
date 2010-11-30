

MyDesktop.ServicesManagerWindow = Ext.extend(Ext.app.Module, {
    id:'service-manager-win',
    store: null,
    init : function(){
        this.launcher = {
            text: 'service-manager-win',
            iconCls:'icon-grid',
            handler : this.createWindow,
            scope: this
        }
    },
    createWindow : function(options){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('service-manager-win');

        if(!win){
            if(!this.store){
                this.store = GeoShield.ServicesUrls.Store.getStore();
            }

            var sm = new Ext.grid.RowSelectionModel({
                singleSelect:true
            });
            
            var grid = new Ext.grid.GridPanel({
                store: this.store,
                border:false,
                loadMask: true,
                columns: [
                {
                    id:'id',
                    header: "id",
                    width: 5,
                    dataIndex: 'idSur',
                    sortable: true
                },
                {
                    header: "Service Type",
                    width: 15,
                    dataIndex: 'nameSrv',
                    sortable: true
                },
                {
                    header: "Context path",
                    width: 15,
                    dataIndex: 'pathSur',
                    sortable: true
                },
                {
                    header: "Service address",
                    width: 50,
                    dataIndex: 'urlSur',
                    sortable: true
                }
                ],
                sm: sm,
                stripeRows: true,
                viewConfig: {
                    forceFit:true
                }
            });

            // Window taskbar buttons
            var addBut = new Ext.Button({
                text:'Add service',
                tooltip:'Create a new service',
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

            var delBut = new Ext.Button({
                id: 'delBut',
                text:'Delete',
                tooltip:'Delete selected service',
                iconCls:'remove',
                disabled: true
            });

            var propBut = new Ext.Button({
                text:'Properties',
                tooltip:'Modify selected service',
                iconCls:'option',
                disabled: true
            });
            
            win = desktop.createWindow({
                id       : 'service-manager-win',
                title    : 'Services panel',
                closable : true,
                iconCls  : 'service',
                width    : 500,
                height   : 300,
                shim     :false,
                animCollapse   :false,
                constrainHeader:true,
                layout   : 'fit',
                items    : [grid],
                tbar:[addBut,delBut,propBut],
                myOnLoad : function(c){
                    var module = MyDesktop.getModule('service-manager-win');
                    if(module){
                        module.store.load();
                    }
                    this.un('show',win.myOnLoad);
                }
            });
            win.on('show',win.myOnLoad);
        }
        win.show();
    }
});



MyDesktop.ServiceFormWindow = Ext.extend(Ext.app.Module, {
    id:'srv-form-win',
    userForm: null,
    init : function(){
        this.launcher = {
            text: 'srv-form-win',
            iconCls:'icon-grid',
            handler : this.createWindow,
            scope: this
        }
    },
    loadRecord: function(record){
        this.combo.getStore().load();
        this.userForm.getForm().loadRecord(record);
        this.userForm.baseParams.REQUEST = "updateServicePermission";
    },
    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('srv-form-win');
        if(!win){
            
            this.combo = new Ext.form.ComboBox({
                fieldLabel: 'Service type',
                hiddenName:'idSrv',
                valueField:'idSrv',
                displayField:'nameSrv',
                forceSelection: true,
                allowBlank:false,
                editable: false,
                store: new Ext.data.JsonStore({
                    url: 'admin/ServicesManagerCtr',
                    baseParams: {
                        REQUEST: "services"
                    },
                    root: 'services',
                    fields: [
                    {
                        name: 'idSrv',
                        type: 'int'
                    },
                    {
                        name: 'nameSrv',
                        type: 'string'
                    }
                    ]
                }),
                typeAhead: true,
                mode: 'remote',
                triggerAction: 'all',
                selectOnFocus:true,
                anchor: '100%'
            });

            this.userForm = new Ext.FormPanel({
                labelWidth: 90, // label settings here cascade unless overridden
                bodyStyle:'padding:5px 5px 0',
                width: 300,
                defaultType: 'textfield',
                frame: false,
                border:false,
                baseParams: {
                    REQUEST:'insertServiceUrl'
                },
                defaults: {
                    width: 200
                },
                items: [
                {
                    fieldLabel: 'Id',
                    name: 'idSur',
                    allowBlank:true,
                    hidden: true,
                    hideLabel: true,
                    anchor: '100%'
                },
                this.combo, 
                {
                    fieldLabel: 'Context path',
                    name: 'pathSur',
                    allowBlank:false,
                    anchor: '100%'
                },
                {
                    fieldLabel: 'Service adress',
                    name: 'urlSur',
                    allowBlank:false,
                    anchor: '100%'
                }
                ]
            });

            var submit = this.userForm.addButton({
                text: 'Save',
                disabled:false,
                scope: this.userForm,
                handler: function(){
                    this.getForm().submit({
                        url: 'admin/ServicesManagerCtr',
                        waitMsg:'Saving Data...',
                        success: function (form, action) {
                            GeoShield.Layers.Store.load();
                            Ext.Info.msg('Services-Urls', String.escape(action.result.message));
                            var win = MyDesktop.desktop.getWindow("srv-form-win");
                            var m = MyDesktop.getModule('service-manager-win');
                            if(m && m.store!=undefined){
                                m.store.load();
                            }
                            win.close();
                            GeoShield.Services.Store.load();
                            GeoShield.ServicesUrls.Store.load();
                            GeoShield.Layers.Store.load();
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

            win = desktop.createWindow({
                id       : 'srv-form-win',
                title    : 'Service editor',
                closable : true,
                iconCls  : 'service',
                width    : 350,
                height   : 180,
                layout   : 'fit',
                modal    : false,
                items    : [this.userForm]
            });

        }
        win.show();
    }
});

