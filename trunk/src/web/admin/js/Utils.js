Ext.Info = function(){
    var msgCt;

    function createBox(t, s){
        return ['<div class="msg">',
        '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
        '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>',
        '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
        '</div>'].join('');
    }
    return {
        msg : function(title, format){
            if(!msgCt){
                msgCt = Ext.DomHelper.insertFirst(Ext.get('x-desktop'), {
                    id:'msg-div'
                }, true);
            }
            //msgCt.alignTo(document, 'b-r');
            var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
            var m = Ext.DomHelper.append(msgCt, {
                html:createBox(title, s)
                }, true);
            m.slideIn('t').pause(3).ghost("b", {
                remove:true
            });
        },

        init : function(){
            var s = Ext.get('extlib'), t = Ext.get('exttheme');
            if(!s || !t){ // run locally?
                return;
            }
            var lib = Cookies.get('extlib') || 'ext',
            theme = Cookies.get('exttheme') || 'aero';
            if(lib){
                s.dom.value = lib;
            }
            if(theme){
                t.dom.value = theme;
                Ext.get(document.body).addClass('x-'+theme);
            }
            s.on('change', function(){
                Cookies.set('extlib', s.getValue());
                setTimeout(function(){
                    window.location.reload();
                }, 250);
            });

            t.on('change', function(){
                Cookies.set('exttheme', t.getValue());
                setTimeout(function(){
                    window.location.reload();
                }, 250);
            });
        }
    };
}();

