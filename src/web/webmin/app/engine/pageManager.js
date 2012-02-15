Ext.ns("istsos","istsos.engine","istsos.engine.pageManager");

istsos.engine.pageManager.getMenuHtml = function(){
    var htmlMenu = "";
    for (var h in istsos.engine.pageConfig){
        htmlMenu += "<div class='menuHead'>"+h+"</div>";
        for (var l in istsos.engine.pageConfig[h]){
            htmlMenu += "<div class='menuLink' id='menuLink_"+l+"' onClick='istsos.engine.pageManager.menuClick(\"menuLink_"+l+"\");'>"+l+"</div>";
        }
        htmlMenu += "<br>";
    }
    return htmlMenu;
}

istsos.engine.pageManager.resetMenuHtml = function(){
    // Reset style on other menu links
    for (var h in istsos.engine.pageConfig){
        for (var l in istsos.engine.pageConfig[h]){
            var el = Ext.get("menuLink_"+l);
            el.setStyle("color", "#3465a4");
        }
    }
}

istsos.engine.pageManager.getMenuConfig = function(menuLink) {
    for (var h in istsos.engine.pageConfig){
        for (var l in istsos.engine.pageConfig[h]){
            if ("menuLink_"+l == menuLink) {
                return istsos.engine.pageConfig[h][l];
            }
        }
    }
    throw "Menu configuration object not found: " + menuLink;
}

istsos.engine.pageManager.menuClick = function (menuLinkId){
    // Loading page content
    var mainCenter = Ext.getCmp("mainCenter");
    mainCenter.removeAll(true);
    var conf = istsos.engine.pageManager.getMenuConfig(menuLinkId);
    console.log(conf);
    try{
        mainCenter.add(Ext.create('istsos.view.ui.CenterPage',{
            istTitle: conf['title'],
            istBody: conf['body']
        }));
    }catch(e){
        alert("Page does not exist");
        return;
    } 
    // Changing menu style
    istsos.engine.pageManager.resetMenuHtml();
    var el = Ext.get(menuLinkId);
    el.setStyle("color", "red");
}
