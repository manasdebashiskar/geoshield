Ext.ns("GeoShield","GeoShield.Offerings");

GeoShield.Offerings.Grid = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function(config) {
        /*component configuration code here! */
        //this.title= 'Available Layers';
        this.id="offGrid";
        this.store=GeoShield.Offerings.Store.getStore();
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
                id:'idOff',
                header: "id",
                dataIndex: 'idOff',
                sortable: true,
                hidden: true
            },
            {
                header: "name",
                dataIndex: 'nameOff',
                width: 125,
                sortable: true
            },
            {
                header: "description",
                dataIndex: 'descOff',
                width: 125,
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
        GeoShield.Offerings.Grid.superclass.initComponent.call(this);
    }
});

GeoShield.Offerings.Store = function() {
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
                    url: './admin/OfferingsManagerCtr',
                    baseParams: {
                        REQUEST: "offerings",
                        FILTER: ((filter != undefined)?filter:null)
                    },
                    root: 'offerings',
                    fields: [
                    {
                        name: 'idOff',
                        type: 'int'
                    },
                    {
                        name: 'nameOff',
                        type: 'string'
                    },
                    {
                        name: 'descOff',
                        type: 'string'
                    },
                    {
                        name: "idOpr",
                        mapping: "offPerList",
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (var i = 0; i < v.length; i++) {
                                    if(v[i].idGrpFk.idGrp==idGrp){
                                        return v[i].idOpr;
                                    }
                                }
                            }
                            return -1;
                        }
                    },
                    {
                        name: "checked",
                        mapping: "offPerList",
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
        getRecord: function(idOff,idSurFk,nameOff,descOff){
            if(this.record!=null){
                return new this.record({
                    idOff   : idOff,
                    idSurFk : idSurFk,
                    nameOff : nameOff,
                    descOff : descOff
                });
            }else{
                this.record = Ext.data.Record.create([
                {
                    name: 'idOff',
                    type: 'integer'
                },
                {
                    name: 'idSurFk',
                    type: 'integer'
                },
                {
                    name: 'nameOff',
                    type: 'string'
                },
                {
                    name: 'descOff',
                    type: 'string'
                }
                ]);
                return this.getRecord(idOff,idSurFk,nameOff,descOff);
            }
        }
    };
}();
