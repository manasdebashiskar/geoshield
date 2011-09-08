Ext.ns("GeoShield","GeoShield.Services");

GeoShield.Services.Grid = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function(config) {
        this.store=GeoShield.Services.Store.getStore();
        this.sm = new Ext.grid.SmartCheckboxSelectionModel({
            dataIndex:'hasGroup',
            email: true,
            alwaysSelectOnCheck: false,
            excel: true,
            singleSelect:true
        });
        this.border=false;
        this.loadMask= true;
        this.columns= [
        this.sm,
        {
            id:'id',
            header: "id",
            width: 5,
            dataIndex: 'idSur',
            sortable: true,
            hidden: true
        },
        {
            header: "Service",
            width: 15,
            dataIndex: 'nameSrv',
            sortable: true
        },
        {
            header: "Path",
            width: 15,
            dataIndex: 'pathSur',
            sortable: true
        },
        {
            header: "Address",
            width: 50,
            dataIndex: 'urlSur',
            sortable: true
        }
        ];
        this.stripeRows= true;
        this.viewConfig= {
            forceFit:true
        }
        GeoShield.Services.Grid.superclass.initComponent.call(this);
        this.store.load();
    }
}); 

GeoShield.Services.Store = function() {
    return {
        store : null,
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
                this.store =new Ext.data.JsonStore({
                    url: './admin/ServicesManagerCtr',
                    baseParams: {
                        REQUEST: "servicesUrls"
                    },
                    root: 'servicesUrls',
                    fields: [
                    {
                        name: 'nameSrv',
                        mapping: 'idSrvFk.nameSrv',
                        type: 'string'
                    },
                    {
                        name: 'idSrv',
                        mapping: 'idSrvFk.idSrv',
                        type: 'int'
                    },
                    {
                        name: 'idSur',
                        type: 'int'
                    },
                    {
                        name: 'pathSur',
                        type: 'string'
                    },
                    {
                        name: 'urlSur',
                        type: 'string'
                    },
                    {
                        name: 'idGrp',
                        type: 'string',
                        convert: function(){
                            return Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue(); 
                        }
                    },
                    {
                        name: 'hasGroup',
                        mapping: 'sprList',
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (i = 0; i < v.length; i++) {
                                    if(v[i].idGrpFk.idGrp==idGrp){
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    },
                    {
                        name: 'idSpr',
                        mapping: 'sprList',
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (i = 0; i < v.length; i++) {
                                    if(v[i].idGrpFk.idGrp==idGrp){
                                        return v[i].idSpr;
                                    }
                                }
                            }
                            return -1;
                        }
                    }
                    ]
                });
                return this.store;
            }

        }
    };
}();

 
GeoShield.Services.ReqGrid = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function(config){
        this.title = 'Permitted service\'s requests';
        this.id = "sprGrid";
        this.store = GeoShield.Services.ReqStore.getStore();
        this.sm = new Ext.grid.SmartCheckboxSelectionModel({
            dataIndex:'hasSre',
            email: true,
            alwaysSelectOnCheck: false,
            excel: false,
            singleSelect:true
        });
        this.border = false;
        this.loadMask = true;
        this.cm = new Ext.grid.ColumnModel([
            this.sm,
            {
                id:'id',
                header: "id",
                dataIndex: 'idReq',
                sortable: true,
                hidden: true
            },
            {
                header: "Request",
                dataIndex: 'nameReq',
                sortable: true
            }
            ]);
        this.stripeRows = true;
        this.viewConfig = {
            forceFit:true
        }; 
        GeoShield.Services.ReqGrid.superclass.initComponent.call(this);
    }
});


GeoShield.Services.ReqStore = function() {
    return {
        store : null,
        idSpr : null,
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
                    url: './admin/ServicesManagerCtr',
                    baseParams: {
                        REQUEST: "requests"
                    },
                    root: 'requests',
                    fields: [
                    {
                        name: 'idReq',
                        type: 'int'
                    },
                    {
                        name: 'nameReq',
                        type: 'string'
                    },,
                    {
                        name: 'idSpr',
                        type: 'int',
                        convert: function(v){
                            if(v!=undefined){
                                var idSpr = GeoShield.Services.ReqStore.idSpr;
                                if(idSpr==undefined){
                                    return -1;
                                }else{
                                    return idSpr;
                                }
                            }
                            return -1;
                        }
                    },
                    {
                        name: 'hasSre',
                        mapping: 'sreList',
                        convert: function(v){
                            if(v!=undefined){
                                var idSpr = GeoShield.Services.ReqStore.idSpr;
                                if(idSpr!=undefined){
                                    for (i = 0; i < v.length; i++) {
                                        if(v[i].idSprFk.idSpr==idSpr){
                                            return true;
                                        }
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

        }
    };
}();
