In the last few years, here at the Institute of Earth Science (SUPSI), we begin using OGC services for map generation (WMS, WFS), geodata processing (WPS) and sensor data interaction (SOS).

Until our services were publicly accessible there weren't many problems, but when external bodies (such as the Government) asked for some geo-application where data-confidentiality through the web was a requirement, then it became our main issue. Such a kind of project generally contains sensitive data, and obviously it cannot be accessible to everyone.
Searching around the web for a simple solution that suite our need we didn't find anything really simple, so we decided to develop ourself a security solution to guarantee a strong protection for our geo-services. The first thing was to decide the project name: GeoShield.

GeoShield is a project born to offer a centralized way to define security access-control to geo-services. It acts like a proxy, intercepting all the communications between clients and OGC compliant services (WMS, WFS, WPS, SOS).
GeoShield is able to manage users and groups, it handles authentication and privileges settings among groups and registered services. It is capable to analyse requests applying the filters set to the user and manipulating the response.