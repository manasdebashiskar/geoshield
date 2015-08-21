# geoshield #

### Official site ###
[here](http://sites.google.com/site/geoshieldproject).


### Quick installation guide: ###

1. The database postgresql

> - Install the latest version of PostgreSQL
> > http://www.postgresql.org/download/


> - Create an empty database

> - initialize the database using the sql file: database.sql


2. The application with the application server Tomcat

> - Install the 6.x version of Apache Tomcat
> > http://tomcat.apache.org/


> - In the conf directory of tomcat find the tomcat-users.xml file
> > add the geoshield role and user.
> > example:
```
     <role rolename="geoshield"/>
```
> > and the user admin:
```
     <user password="admin" roles="geoshield" username="admin"/>
```

> - In the webapps directory of tomcat copy
> > the geoshield.war file


> - When geoshield.war is unpacked by tomcat in the geoshield directory
> > go to WEB-INF/classes/META-INF/persistence.xml.


> Modify:
```
      <property name="javax.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432/databasename"/>
```
> where databasename is the name of the database that you have created
```
      <property name="javax.persistence.jdbc.user" value="postgres"/>
```
> where user is the name of the database user/owner
```
      <property name="javax.persistence.jdbc.password" value="1234"/>
```
> where 1234 is the password of the database user/owner
> - Restart tomcat


3. Install GeoServer (optional):
> go to http://geoserver.org/
> if you install a fresh copy of GeoServer, it is possible to see some examples in action,
> using the demo layers that cames with GeoServer.


4. Open your browser and type: http://localhost:8080/geoshield

5. Check the catalina.out log file of tomcat for errors


If you have troubles you can ask in the GeoShield group:
> http://groups.google.com/group/geoshield-project

Better documentation will come..

> The GeoShield Team


### Geoserver plug-in Quick installation guide ###


This plug-in is compatible with GeoShield 0.3.0 and GeoServer 2.1.2 (or 2.1.3)

1. Install GeoShield 0.3.0

2. Install GeoServer 2.1.2 or 2.1.3

3. Copy the geoshield-1.0.jar into the WEB-INF/lib directory of the upacked GeoServer

4. Edit the WEB-INF/web.xml file of GeoServer as in the example example-web.xml adding this 2 configuration tags:

- Just before the "Spring Security Filter Chain Proxy" filter definition:
```
    <filter>
        <filter-name>GeoShieldFilter</filter-name> 
        <filter-class>ch.supsi.ist.geoshield.GeoShieldFilter</filter-class>
        <init-param>
            <param-name>GEOSHIELD_LIMITS_RESYNC</param-name>
            <param-value>30</param-value> 
            <description>
                Define the re-synchronization interval between GeoServer and GeoShield in seconds.
            </description>
        </init-param>
        <init-param>
            <param-name>GEOSHIELD_URL</param-name>
	        <param-value>http://127.0.0.1:8080/geoshield</param-value>
            <description>
                The GeoShield address
            </description>
        </init-param>
        <init-param>
            <param-name>GEOSERVER_URL</param-name>
            <param-value>http://127.0.0.1:8080/geoserver/wms</param-value>
            <description>
                The GeoServer url as defined in GeoShield permission definition (in db, the 'services_urls' table)
            </description>
        </init-param>
        <init-param>
            <param-name>GEOSHIELD_USER</param-name>
            <param-value>geoshield</param-value>
            <description>
                The user created in GeoServer users.properties file.
            </description>
        </init-param>
    </filter>
```
- just before the 'Spring Security Filter Chain Proxy' filter-mapping definition:
```
    <filter-mapping>
        <filter-name>GeoShieldFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```
5. Edit GEOSERVER\_DATA\_DIR/data/security/users.properties adding a row defining the geoshield user:
```
    geoshield=geoshield,GEOSHIELD_USER,enabled
```

6. Restart tomcat

7. Check the catalina.out log file of tomcat for errors

To set users permissions, you should configure GeoShield at http://127.0.0.1:8080/geoshield
