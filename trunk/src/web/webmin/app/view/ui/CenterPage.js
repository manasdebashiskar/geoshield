Ext.define('istsos.view.ui.CenterPage', {
    extend: 'istsos.view.ui.BasePage',
    constructor: function(config) {
        if(Ext.isObject(config)){
            if(config["istTitle"]){
                this.istTitle = config["istTitle"];
            }
            if(config["istBody"]){
                this.istBody = config["istBody"];
            }
        }
        this.callParent(arguments);
    },
    initComponent: function() {
        this.callParent(arguments);
        if(this.istTitle){
            this.setTitle(this.istTitle);
        }
        if(this.istBody){
            this.setBody(this.istBody); 
        }
    },
    setTitle: function(title){
        this.getComponent(0).removeAll(true);
        if(Ext.isString(title)){
            this.getComponent(0).update(title);
        }else{
            if(Ext.isArray(title)){
                if(title.length==1){
                    this.getComponent(0).add(Ext.create(title[0]));
                }else if(title.length==2){
                    this.getComponent(0).add(Ext.create(title[0],title[1]));
                }else{
                    throw "CenterPage can't handle the configuration array given.";
                }
            }
        }
    },
    setBody: function(body){
        this.getComponent(1).removeAll(true);
        if(Ext.isString(body)){
            this.getComponent(1).update(body);
        }else{
            if(Ext.isArray(body)){
                if(body.length==1){
                    this.getComponent(1).add(Ext.create(body[0]));
                }else if(body.length==2){
                    this.getComponent(1).add(Ext.create(body[0],body[1]));
                }else{
                    throw "CenterPage can't handle the given body configuration array.";
                }
            }
        }
    }
});