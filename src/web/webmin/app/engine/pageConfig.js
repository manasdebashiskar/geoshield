Ext.ns("istsos","istsos.engine");

istsos.engine.pageConfig = {
    "About": {
        "Service status": {
            title: "About / Service Status",
            body: "<p><STRONG>Welcome to</STRONG></p>"+
        "<img src='images/geoshield_logo.png'/>"
        },
        "Service logs": {
            title: "About / Service Logs",
            body: "<p><STRONG>Logs:</STRONG></p>"
        },
        "Contact info": {
            title: "About / Contact Info",
            body: ["istsos.view.FormContactInfo"]
        },
        "About istSOS": {
            title: "About / About istSOS",
            body: ["istsos.view.FormAboutIstsos"]
        }
    },
    "Settings": {
        "Contacts": {
            title: "Settings / Contacts",
            body: ["istsos.view.FormContacts"]
        },
        "Config": {
            title: "Settings / Config",
            body: ["istsos.view.FormConfig"]
        },
        "Database": {
            title: "Settings / Database",
            body: ["istsos.view.FormDatabase"]
        },
        "Data quality": {
            title: "Settings / Data quality",
            body: ["istsos.view.FormDataQuality"]
        }
    },
    "Data": {
        "Offerings": {
            title: "Data / Offerings",
            body: ""
        },
        "Procedures": {
            title: "Data / Procedures",
            body: ""
        },
        "Observed properties": {
            title: "Data / Observed properties",
            body: ""
        }
    },
    "Observations": {
        "View": {
            title: "Observations / View",
            body: ""
        },
        "Edit": {
            title: "Observations / Edit",
            body: ""
        },
        "Download": {
            title: "Observations / Download",
            body: ""
        }
    },
    "Scheduler": {
        "Validator": {
            title: "Scheduler / Validator",
            body: ""
        },
        "Upload": {
            title: "Scheduler / Upload",
            body: ""
        }
    }
};