Ext.ns("GeoShield","GeoShield.LayersPermissions");

GeoShield.LayersPermissions.Form = Ext.extend(Ext.form.FormPanel, {
    initComponent: function(config) {
        this.id= 'real-group-form-win';
        this.labelWidth=60;
        this.requestParam="insertGroup";
        this.defaults={
            width: 100
        };
        this.frame=false;
        this.border=false;
        this.defaultType='textarea';
        this.bodyStyle='padding:5px 5px 0';
        this.items = [
        new Ext.form.Label({
            text: "CQL Filter",
            style: {
                'font-size':"12px"
            //,'font-weight':"bold"
            }
        }),
        new Ext.form.TextField({
            fieldLabel: 'Id',
            name: 'idLpr',
            allowBlank:true,
            hidden: true,
            hideLabel: true
        }),
        new Ext.form.TextArea({
            id:"filterLpr",
            hideLabel: true,
            fieldLabel:"CQL Filter",
            grow: true,
            anchor: '100%',
            name: 'filterLpr',
            growMax: 150,
            style: {
                'padding-top':"10px"
            }
        })
        ];
        GeoShield.LayersPermissions.Form.superclass.initComponent.call(this);
    }
});

GeoShield.LayersPermissions.Store = function() {
    return {
        store : null,
        getRecord: function(layerRecord){
            
        },
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
                        REQUEST: "servicesUrls"
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