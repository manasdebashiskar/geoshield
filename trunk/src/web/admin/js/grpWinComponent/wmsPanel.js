/*
 * GeoShield
 * Copyright(c) 2009-2010, Istituto Scienze della Terra - SUPSI
 * geoservice@supsi.ch
 *
 * See the MIT License for more details.
 * https://sites.google.com/site/geoshieldproject/license
 */

Ext.ns("Wms");


Wms.LayersPermissions = Ext.extend(Ext.Panel, {
    initComponent: function() {

        var combo = new GeoShield.ServicesUrls.ComboBox();

        // Layers

        var sync = new Ext.Button({
            text:'Sync',
            tooltip:'Syncronize database with service',
            iconCls:'option',
            disabled: false
        });
        var addLay = new Ext.Button({
            text:'Add',
            tooltip:'Add a layer manually',
            iconCls:'add',
            disabled: false
        });
        var remLay = new Ext.Button({
            text:'Rem',
            tooltip:'Remove the selected layer',
            iconCls:'remove',
            disabled: false
        });

            
        var gridConf = {
            tbar:[addLay,remLay,sync]
        };

        var grid = new GeoShield.Layers.Grid(gridConf);
        var form = new GeoShield.LayersPermissions.Form();

        grid.getSelectionModel().on('evToggleChecked',
            function( rowIndex, c){
                var record = grid.getStore().getAt(rowIndex);
                console.log("evToggleChecked: "+rowIndex);
                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                var idLay = record.get("idLay");
                var checked = c;
                Ext.Ajax.request({
                    url: './admin/LayersManagerCtr',
                    params: {
                        REQUEST : 'checkLayersPermissions',
                        checked : checked,
                        idLay   : idLay,
                        idGrp   : idGrp
                    },
                    success: function(){
                        Ext.Info.msg('Update layers permissions', 'TRUE');
                        GeoShield.Layers.Store.load();
                    },
                    failure:  function(form, action) {
                        Ext.Info.msg('Update layers permissions', 'FALSE');
                    }
                });
            });

        grid.on('rowclick',
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
            },form); 

        combo.on("select",
            function(combo, record, index ){
                var idSur = record.get("idSur");
                GeoShield.Layers.Store.load("idSur;"+idSur);
            });


        var northPan = new Ext.Panel({
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
                items: [combo]
            })
            ]
        });

        var eastPan = new Ext.Panel({
            region      : 'east',
            split       : true,
            layout      : 'fit',
            collapsible : true,
            width       : 600,
            items: [form]
        });

        var submit = eastPan.addButton({
            text: 'Validate',
            disabled:false
        }); 

        var submit2 = eastPan.addButton({
            text: 'Apply',
            disabled:false,
            scope: form,
            handler: function(){
                Ext.Info.msg('CQL Filter', 'Modification applied?');
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
                        Ext.Msg.show({
                            title:'Warning',
                            msg: 'Server message:<br>'+action.result.error,
                            buttons: Ext.Msg.OK,
                            //animEl: 'srv-form-win',
                            icon: Ext.MessageBox.WARNING
                        });
                    }
                });
            }
        });

        var centerPan = new Ext.Panel({
            region      : 'center',
            split       : true,
            layout      : 'fit',
            collapsible : true,
            width       : 250,
            items       :[grid]
        });


        this.title = 'WMS/WFS permissions';
        this.border = false;
        this.layout = 'border'; 
        this.items = [northPan, eastPan, centerPan];
        
        //this.html = "ciao";

        /*component configuration code here! */
        Wms.LayersPermissions.superclass.initComponent.call(this);
    }
});
