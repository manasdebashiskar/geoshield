Ext.ns("GeoShield","GeoShield.Layers");

GeoShield.Layers.Grid = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function(config) {
        /*component configuration code here! */
        //this.title= 'Available Layers';
        this.id="layGrid";
        this.store=GeoShield.Layers.Store.getStore();
        this.border=false;
        this.loadMask=true;

        this.sm = new Ext.grid.SmartCheckboxSelectionModel({
            dataIndex:'checked',
            email: true,
            alwaysSelectOnCheck: false,
            excel: false,
            singleSelect:true
        });
        
        this.cm=new Ext.grid.ColumnModel([
            this.sm,
            {
                id:'idLayer',
                header: "Lay",
                dataIndex: 'idLay',
                sortable: true,
                hidden: true
            },
            {
                header: "name",
                dataIndex: 'nameLay',
                width: 125,
                sortable: true
            },
            {
                header: "geom",
                dataIndex: 'geomLay',
                width: 60,
                sortable: true
            }
            ]);

        this.stripeRows= true;
        this.viewConfig= {
            forceFit:true
        };
        if(config!=undefined){
            Ext.apply(this, config); 
        }
        GeoShield.Layers.Grid.superclass.initComponent.call(this);
    }
}); 

GeoShield.Layers.Store = function() {
    return {
        store : null,
        record: null,
        load: function(filter){
            if(this.store!=null){
                if(filter != undefined){
                    this.store.baseParams.FILTER = filter;
                }
                this.store.load();
            }
        },
        getStore: function(filter){
            if(this.store!=null){
                if(filter != undefined){
                    this.store.baseParams.FILTER = filter;
                }
                return this.store;
            }else{
                this.store = new Ext.data.JsonStore({
                    url: './admin/LayersManagerCtr',
                    baseParams: {
                        REQUEST: "layers",
                        FILTER: ((filter != undefined)?filter:null)
                    },
                    root: 'layers',
                    fields: [
                    {
                        name: 'idLay',
                        type: 'int'
                    },
                    {
                        name: 'idSur',
                        type: 'int',
                        mapping: 'idSurFk.idSur'
                    },
                    {
                        name: 'nameLay',
                        type: 'string'
                    },
                    {
                        name: 'geomLay',
                        type: 'string'
                    },
                    {
                        name: 'nsLay',
                        type: 'string'
                    },
                    {
                        name: 'nsUrlLay',
                        type: 'string'
                    },
                    {
                        name: 'layPerList',
                        type: 'auto'
                    },
                    {
                        name: "idLpr",
                        mapping: "layPerList",
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (var i = 0; i < v.length; i++) {
                                    if(v[i].idGrpFk.idGrp==idGrp){
                                        return v[i].idLpr;
                                    }
                                }
                            }
                            return -1;
                        }
                    },
                    {
                        name: "checked",
                        mapping: "layPerList",
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (var i = 0; i < v.length; i++) {
                                    if(v[i].idGrpFk.idGrp==idGrp){
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }
                    ] 
                });
                return this.store;
            }
            
        },
        getRecord: function(idLay,idSurFk,nameLay,geomLay,nsLay,nsUrlLay){
            if(this.record!=null){
                return new this.record({
                    idLay   : idLay,
                    idSurFk : idSurFk,
                    nameLay : nameLay,
                    geomLay : geomLay,
                    nsLay : nsLay,
                    nsUrlLay : nsUrlLay
                });
            }else{
                this.record = Ext.data.Record.create([
                {
                    name: 'idLay',
                    type: 'integer'
                },
                {
                    name: 'idSurFk',
                    type: 'integer'
                },
                {
                    name: 'nameLay',
                    type: 'string'
                },
                {
                    name: 'geomLay',
                    type: 'string'
                },
                {
                    name: 'nsLay',
                    type: 'string'
                },
                {
                    name: 'nsUrlLay',
                    type: 'string'
                }
                ]);
                return this.getRecord(idLay,idSurFk,nameLay,geomLay,nsLay,nsUrlLay);
            }
        }
    };
}();
