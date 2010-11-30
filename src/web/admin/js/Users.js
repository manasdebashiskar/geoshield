Ext.ns("GeoShield","GeoShield.Users");

GeoShield.Users.Grid = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function(config) {
        this.store=GeoShield.Users.GridStore.getStore();
        this.border=false;
        this.loadMask= true;
        this.sm = new Ext.grid.RowSelectionModel({
            singleSelect:true
        });
        this.columns = [
        {
            id:'id',
            header: "id",
            width: 30,
            dataIndex: 'idUsr',
            sortable: true
        },
        {
            header: "User name",
            width: 60,
            dataIndex: 'nameUsr',
            sortable: true
        },
        {
            header: "Password",
            width: 60,
            dataIndex: 'pswUsr',
            sortable: true
        },
        {
            header: "First name",
            width: 60,
            dataIndex: 'firstNameUsr',
            sortable: true
        },
        {
            header: "Last name",
            width: 70,
            dataIndex: 'lastNameUsr',
            sortable: true
        },
        {
            header: "Email",
            width: 140,
            dataIndex: 'emailUsr',
            sortable: true
        },
        {
            header: "Ufficio",
            width: 100,
            dataIndex: 'officeUsr',
            sortable: true
        },
        {
            header: "Tel",
            width: 80,
            dataIndex: 'telUsr',
            sortable: true
        },
        {
            header: "Fax",
            width: 80,
            dataIndex: 'faxUsr',
            sortable: true
        },
        {
            header: "Indirizzo",
            width: 100,
            dataIndex: 'addressUsr',
            sortable: true
        },
        {
            header: "Attivo",
            width: 50,
            dataIndex: 'isActiveUsr',
            sortable: true
        }
        ];
        this.stripeRows= true;
        this.viewConfig= {
            forceFit:true
        }
        if(config!=undefined){
            Ext.apply(this, config);
        }
        GeoShield.Users.Grid.superclass.initComponent.call(this);
    }
});

GeoShield.Users.GridStore = function() {
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
                    url: './admin/UserAccountCtr',
                    baseParams: {
                        REQUEST: "users"
                    },
                    root: 'users',
                    fields: [
                    {
                        name: 'idUsr',
                        type: 'int'
                    },
                    {
                        name: 'nameUsr',
                        type: 'string'
                    },
                    {
                        name: 'pswUsr',
                        type: 'string'
                    },
                    {
                        name: 'firstNameUsr',
                        type: 'string'
                    },
                    {
                        name: 'lastNameUsr',
                        type: 'string'
                    },
                    {
                        name: 'emailUsr',
                        type: 'string'
                    },
                    {
                        name: 'officeUsr',
                        type: 'string'
                    },
                    {
                        name: 'telUsr',
                        type: 'string'
                    },
                    {
                        name: 'faxUsr',
                        type: 'string'
                    },
                    {
                        name: 'addressUsr',
                        type: 'string'
                    },
                    {
                        name: 'isActiveUsr',
                        type: 'bool'
                    }
                    ]
                });
                return this.store;
            }

        }
    };
}();

GeoShield.Users.GridMembers = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function(config) {
        this.store=GeoShield.Users.MembersStore.getStore();
        this.title= "Members";
        this.sm = new Ext.grid.SmartCheckboxSelectionModel({
            dataIndex:'hasGroup',
            email: true,
            alwaysSelectOnCheck: false,
            excel: false,
            singleSelect:true
        });
        this.loadMask= true;
        this.border=false;
        this.cm= new Ext.grid.ColumnModel([
            this.sm,
            {
                id:'id',
                header: "id",
                width: 30,
                dataIndex: 'idUsr',
                sortable: true,
                hidden: true
            },
            {
                header: "User name",
                width: 60,
                dataIndex: 'nameUsr',
                sortable: true
            },
            {
                header: "Password",
                width: 60,
                dataIndex: 'pswUsr',
                sortable: true,
                hidden: true
            },
            {
                header: "Fist name",
                width: 60,
                dataIndex: 'firstNameUsr',
                sortable: true,
                hidden: false
            },
            {
                header: "Last name",
                width: 60,
                dataIndex: 'lastNameUsr',
                sortable: true,
                hidden: false
            },
            {
                header: "Email",
                width: 100,
                dataIndex: 'emailUsr',
                sortable: true,
                hidden: false
            },
            {
                header: "Expiration",
                width: 50,
                dataIndex: 'expirationGus',
                sortable: true,
                hidden: false
            },
            {
                header: "Invoice",
                width: 40,
                dataIndex: 'invoiceGus',
                sortable: true,
                hidden: false
            }
            ])
        this.viewConfig={
            forceFit:true
        }
        if(config!=undefined){
            Ext.apply(this, config);
        }
        GeoShield.Users.Grid.superclass.initComponent.call(this);
    }
});

GeoShield.Users.MembersStore = function() {
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
                    url: 'admin/UserAccountCtr', 
                    baseParams: {
                        REQUEST: "users"
                    },
                    root: 'users',
                    fields: [
                    {
                        name: 'idUsr',
                        type: 'int'
                    },
                    {
                        name: 'nameUsr',
                        type: 'string'
                    },
                    {
                        name: 'pswUsr',
                        type: 'string'
                    },
                    {
                        name: 'firstNameUsr',
                        type: 'string'
                    },
                    {
                        name: 'lastNameUsr',
                        type: 'string'
                    },
                    {
                        name: 'emailUsr',
                        type: 'string'
                    },
                    {
                        name: 'officeUsr',
                        type: 'string'
                    },
                    {
                        name: 'telUsr',
                        type: 'string'
                    },
                    {
                        name: 'faxUsr',
                        type: 'string'
                    },
                    {
                        name: 'addressUsr',
                        type: 'string'
                    },
                    {
                        name: 'isActiveUsr',
                        type: 'bool'
                    }
                    /*,
                    {
                        name: 'groups'
                    },
                    {
                        name: 'hasGroup',
                        mapping: 'groups',
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (i = 0; i < v.length; i++) {
                                    if(v[i].idGrp==idGrp){
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }*/,
                    {
                        name: 'groupsUsers'
                    },
                    {
                        name: 'hasGroup',
                        mapping: 'groupsUsers',
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
                        name: 'expirationGus',
                        mapping: 'groupsUsers',
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (i = 0; i < v.length; i++) {
                                    if(v[i].idGrpFk.idGrp==idGrp){
                                        if(v[i].expirationGus==null){
                                            return "<span style='font-size: 17px;'>&#8734;</span>";
                                        }else{
                                            return v[i].expirationGus;
                                        }
                                    }
                                }
                            }
                            return "";
                        }
                    },
                    {
                        name: 'invoiceGus',
                        mapping: 'groupsUsers',
                        convert: function(v){
                            if(v!=undefined){
                                var idGrp = Ext.getCmp("real-group-form-win").getForm().findField("idGrp").getValue();
                                for (i = 0; i < v.length; i++) {
                                    if(v[i].idGrpFk.idGrp==idGrp){
                                        if(v[i].invoiceGus==null){
                                            return "N/P";
                                        }else{
                                            return v[i].invoiceGus;
                                        }
                                    }
                                }
                            }
                            return "";
                        }
                    }
                    ]
                }
                );
                return this.store;
            }
        }
    };
}();

GeoShield.Users.Form = Ext.extend(Ext.FormPanel, {
    initComponent: function(config) {
        this.labelWidth= 75; // label settings here cascade unless overridden
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
            name: 'idUsr',
            allowBlank:true,
            hidden: true,
            hideLabel: true
        },
        {
            fieldLabel: 'User name',
            name: 'nameUsr',
            allowBlank:false,
            anchor: '100%'
        },
        {
            fieldLabel: 'Password',
            name: 'pswUsr',
            allowBlank:false,
            anchor: '100%'
        },
        {
            fieldLabel: "Fist name",
            name: 'firstNameUsr',
            allowBlank: true,
            anchor: '100%'
        },
        {
            fieldLabel: "Last name",
            name: 'lastNameUsr',
            allowBlank: true,
            anchor: '100%'
        },
        {
            fieldLabel: "Email",
            name: 'emailUsr',
            allowBlank: true,
            anchor: '100%'
        },
        {
            fieldLabel: "Ufficio",
            name: 'officeUsr',
            allowBlank: true,
            anchor: '100%'
        },
        {
            fieldLabel: "Telefono",
            name: 'telUsr',
            allowBlank: true,
            anchor: '100%'
        },
        {
            fieldLabel: "Fax",
            name: 'faxUsr',
            allowBlank: true,
            anchor: '100%'
        },
        {
            fieldLabel: "Indirizzo",
            xtype: 'textarea',
            name: 'addressUsr',
            allowBlank: true,
            anchor: '100%'
        },
        {
            fieldLabel: "Attivo",
            xtype: 'checkbox',
            name: 'isActiveUsr',
            allowBlank: true,
            anchor: '100%'
        }
        ]
        if(config!=undefined){
            Ext.apply(this, config);
        }
        GeoShield.Users.Form.superclass.initComponent.call(this);
    }
}); 



GeoShield.Users.MembershipStore = function() {
    return {
        store : null,
        load: function(filter){
            if(this.store!=null){
                if(filter != undefined){
                    this.store.baseParams.idUsr = filter;
                }
                this.store.load();
            }
        },
        getStore: function(idUsr){
            if(this.store!=null){
                if(idUsr != undefined){
                    this.store.baseParams.idUsr = idUsr;
                }
                return this.store;
            }else{
                var bp = {
                    REQUEST: "groupUsers"
                };
                if(idUsr!=null){
                    bp.idUsr=idUsr;
                }
                // {"expirationGus":null,"idGrpFk":{"idGrp":1,"nameGrp":"ist"},"idGus":94,"invoiceGus":null}
                this.store = new Ext.data.JsonStore({
                    url: 'admin/UserAccountCtr',
                    baseParams: bp,
                    root: 'groupUsers',
                    fields: [
                    {
                        name: 'idGus',
                        type: 'int'
                    },
                    {
                        name: 'nameGrp',
                        type: 'string',
                        mapping: 'idGrpFk.nameGrp'
                    },
                    {
                        name: 'expirationGus',
                        type: 'date',
                         dateFormat: 'd/m/Y'/*,
                        mapping: 'expirationGus',
                        convert: function(v){
                            if(v==null){
                                return "&#8734;";
                            }else{
                                return v;
                            }
                        }*/
                    },
                    {
                        name: 'invoiceGus',
                        mapping: 'invoiceGus'/*,
                        convert: function(v){
                            if(v==null){
                                return "N/P";
                            }else{
                                return v[i].invoiceGus;
                            }
                        }*/
                    }
                    ]
                }
                );
                return this.store;
            }
        }
    }; 
}();

GeoShield.Users.getGridMembership = function (config, usrRecord) {
    var idGr = Ext.id();
    var addBut = new Ext.Button({
        text:'Add user',
        tooltip:'Create a new group',
        iconCls:'add',
        listeners: {
            'click': {
                fn: function(e, t){
                    var module = MyDesktop.getModule('group-user-form-win');
                    if(module){
                        module.createWindow();
                    }
                }
            }
        }
    });
            
    var editBut = new Ext.Button({
        text:'Edit',
        tooltip:'Modify selected membership',
        iconCls:'option',
        disabled: true,
        listeners: {
            'click': {
                fn: function(e, t){
                    var module = MyDesktop.getModule('group-user-form-win');
                    if (module) {
                        module.createWindow();
                        var gr = Ext.getCmp(idGr);
                        module.loadRecord(gr.getSelectionModel().getSelected(),usrRecord);
                    }
                }
            }
        }
    });

    var delBut = new Ext.Button({
        id: 'delBut',
        text:'Delete',
        tooltip:'Delete selected user',
        iconCls:'remove',
        disabled: true
    });

    return new Ext.grid.GridPanel(Ext.apply({
        store: GeoShield.Users.MembershipStore.getStore(usrRecord.get("idUsr")),
        title: "Memberships",
        id: idGr,
        loadMask: true,
        border: false,
        tbar: [addBut,delBut,editBut],
        listeners: {
            'rowclick': {
                fn: function( grid, rowIndex, ev){
                    if(grid.selModel.getCount()>0){
                        delBut.enable();
                        if(grid.selModel.getCount()==1){
                            editBut.enable();
                        }else{
                            editBut.disable();
                        }
                    }else{
                        delBut.disable();
                        editBut.disable();
                    }
                }
            }
        },
        cm: new Ext.grid.ColumnModel([
        {
            id:'id',
            header: "id",
            width: 30,
            dataIndex: 'idGus',
            sortable: true,
            hidden: true
        },
        {
            header: "Group name",
            width: 60,
            dataIndex: 'nameGrp',
            sortable: true
        },
        {
            header: "Expiration",
            width: 60,
            dataIndex: 'expirationGus',
            sortable: true,
            hidden: false,
            renderer:  Ext.util.Format.dateRenderer('d/m/Y')
        },
        {
            header: "Invoice",
            width: 60,
            dataIndex: 'invoiceGus',
            sortable: true,
            hidden: false
        }
        ]),
        viewConfig:{
            forceFit:true
        }
    }, config));
};
