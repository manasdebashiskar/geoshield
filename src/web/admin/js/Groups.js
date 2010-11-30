Ext.ns("GeoShield","GeoShield.Groups");

GeoShield.Groups.Grid = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function(config) {
        this.border = false;
        this.loadMask = true;
        this.sm = new Ext.grid.RowSelectionModel({
            singleSelect:true
        });
        this.store = GeoShield.Groups.GridStore.getStore();
        this.columns = [
        {
            id          : "id",
            header      : "id",
            dataIndex   : "idGrp",
            sortable    : true,
            width       : 15
        },
        {
            header      : "Group name",
            dataIndex   : "nameGrp",
            sortable    : true,
            width       : 30
        }
        ];
        this.stripeRows = true;
        this.viewConfig = {
            forceFit    : true
        }
        if(config!=undefined){
            Ext.apply(this, config);
        }
        GeoShield.Groups.Grid.superclass.initComponent.call(this);
    }
});

GeoShield.Groups.GridStore = function() {
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
                    url: './admin/GroupAccountCtr',
                    baseParams: {
                        REQUEST:"groups"
                    },
                    root: 'groups',
                    fields: [
                    {
                        name: 'idGrp',
                        type: 'int'
                    },

                    {
                        name: 'nameGrp',
                        type: 'string'
                    }
                    ]
                });
                return this.store;
            }

        }
    };
}();


GeoShield.Groups.Form = Ext.extend(Ext.FormPanel, { 
    initComponent: function(config) {
        this.labelWidth= 75; // label settings here cascade unless overridden
        this.url='save-form.php';
        this.bodyStyle='padding:5px 5px 0';
        this.width= 120;
        this.requestParam= "insertUser";
        this.defaults= {
            width: 150
        };
        this.defaultType= 'textfield';
        this.items= [
        {
            fieldLabel: 'Id',
            name: 'idGrp',
            allowBlank:true,
            hidden: true,
            hideLabel: true
        },
        {
            fieldLabel: 'Group name',
            name: 'nameGrp',
            allowBlank:false,
            anchor: '100%'
        }
        ]
        if(config!=undefined){
            Ext.apply(this, config);
        }
        GeoShield.Groups.Form.superclass.initComponent.call(this);
    }
});
