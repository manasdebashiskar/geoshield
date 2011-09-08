Ext.ns("GeoShield","GeoShield.ServicesUrls");

GeoShield.ServicesUrls.ComboBox = Ext.extend(Ext.form.ComboBox, {
    initComponent: function(config) {
        this.fieldLabel= 'Available WMS and WFS';
        this.hiddenName='idSur';
        this.forceSelection= false;
        this.allowBlank=false;
        this.editable= false;
        this.store=GeoShield.ServicesUrls.Store.getStore();
        this.valueField='idSur';
        this.displayField='pathSur';
        this.tpl= '<tpl for="."><div class="x-combo-list-item"><strong>{pathSur}</strong> ({nameSrv})</div></tpl>';
        //this.tpl='<tpl for=".">{pathSur} ({nameSrv})</tpl>';
        //this.typeAhead= true;
        this.mode= 'remote';
        this.triggerAction= 'all';
        //this.selectOnFocus=true;
        this.anchor= '100%';
        GeoShield.ServicesUrls.Store.load();
        GeoShield.ServicesUrls.ComboBox.superclass.initComponent.call(this);
    }
});

GeoShield.ServicesUrls.Store = function() {
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
                this.store = new Ext.data.JsonStore({
                    url: './admin/ServicesManagerCtr',
                    baseParams: {
                        REQUEST: "wmswfsSurls"
                    },
                    root: 'servicesUrls',
                    fields: [
                    {
                        name: 'idSrv',
                        mapping: 'idSrvFk.idSrv'
                    },
                    {
                        name: 'nameSrv',
                        mapping: 'idSrvFk.nameSrv'
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
                    }
                    ]
                });
                return this.store;
            }

        }
    };
}();

GeoShield.ServicesUrls.SOSCB = Ext.extend(Ext.form.ComboBox, {
    initComponent: function(config) {
        this.fieldLabel= 'Available SOS';
        this.hiddenName='idSur';
        this.forceSelection= false;
        this.allowBlank=false;
        this.editable= false;
        this.store=GeoShield.ServicesUrls.SosStore.getStore();
        this.valueField='idSur';
        this.displayField='pathSur';
        this.tpl= '<tpl for="."><div class="x-combo-list-item"><strong>{pathSur}</strong> ({nameSrv})</div></tpl>';
        //this.tpl='<tpl for=".">{pathSur} ({nameSrv})</tpl>';
        //this.typeAhead= true;
        this.mode= 'remote';
        this.triggerAction= 'all';
        //this.selectOnFocus=true;
        this.anchor= '100%';
        GeoShield.ServicesUrls.SosStore.load();
        GeoShield.ServicesUrls.ComboBox.superclass.initComponent.call(this);
    }
});

GeoShield.ServicesUrls.SosStore = function() {
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
                this.store = new Ext.data.JsonStore({
                    url: './admin/ServicesManagerCtr',
                    baseParams: {
                        REQUEST: "sosSurls"
                    },
                    root: 'servicesUrls',
                    fields: [
                    {
                        name: 'idSrv',
                        mapping: 'idSrvFk.idSrv'
                    },
                    {
                        name: 'nameSrv',
                        mapping: 'idSrvFk.nameSrv'
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
                    }
                    ]
                });
                return this.store;
            }

        }
    };
}();