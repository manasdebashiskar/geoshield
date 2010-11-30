
MyDesktop.SimpleLayersFormWindow = Ext.extend(Ext.app.Module, {
    id:'simple-layers-form-win',
    form: null,
    init : function(){
        
    },
    loadRecord: function(record){
        this.form.getForm().loadRecord(record);
        this.form.baseParams.REQUEST = "updateLayer";
    },
    createWindow : function(options){
        
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('simple-layers-form-win');

        if(!win){

            this.form = new Ext.FormPanel({
                labelWidth: 150,
                bodyStyle:'padding:5px 5px 0',
                width: 200,
                defaultType: 'textfield',
                frame: false,
                border:false,
                baseParams: {
                    REQUEST:'insertLayer'
                },
                defaults: {
                    width: 200
                },
                items: [
                {
                    fieldLabel: 'Id',
                    name: 'idLay',
                    allowBlank:true,
                    hidden: true,
                    hideLabel: true
                },
                new GeoShield.ServicesUrls.ComboBox(),
                {
                    fieldLabel: 'Layer name',
                    name: 'nameLay',
                    allowBlank:false,
                    anchor: '100%'
                },
                {
                    fieldLabel: 'Geometry name',
                    name: 'geomLay',
                    allowBlank:false,
                    anchor: '100%'
                },
                {
                    fieldLabel: 'Namespace name',
                    name: 'nsLay',
                    allowBlank:false,
                    anchor: '100%'
                },
                {
                    fieldLabel: 'Namespace URI',
                    name: 'nsUrlLay',
                    allowBlank:false,
                    anchor: '100%'
                }
                ]
            });
            
            var submit = this.form.addButton({
                text: 'Save',
                disabled:false,
                scope: this.form,
                handler: function(){
                    this.getForm().submit({
                        url: "admin/LayersManagerCtr",
                        waitMsg:"Saving Data...",
                        success: function (form, action) {
                            Ext.Info.msg('Layers editor', String.escape(action.result.message));
                            var win = MyDesktop.desktop.getWindow("simple-layers-form-win");
                            win.close();
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
                id       : 'simple-layers-form-win',
                title    : 'Layer Editor',
                closable : true,
                //iconCls  : 'group_form',
                width    : 500,
                height   : 250,
                layout   : 'fit', 
                modal    : true,
                items    : [this.form]
            });
        }
        win.show();
    }
});
