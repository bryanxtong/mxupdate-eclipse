# mxupdate-eclipse

This is the mxupdate eclipse plug-in which is used for eplm.

1. we generate these jars for eclipse from the source code version 0.10.0

      put the org.mxupdate.eclipse_0.10.0.jar to the eclipse plugins folder 
      put org.mxupdate.eclipse_0.10.0-feature.jar to the features folder
2. right click your PLM project and configure the mxupdate setting

     eg "MX Jar Library"-> C:\enoviaV6R2013x\server\distrib\enovia\WEB-INF\lib\eMatrixServletRMI.jar;C:\enoviaV6R2013x\server\distrib\enovia\WEB-INF\lib\commons-codec-1.12.jar;

As the eclipse plugin jar version is not the same as mxupdate jar version in eplm, so there are some error messages as below. 
but please ignore this and you can change this plugin-in version to 0.90.0 to remove this error messages

[INFO] Connect to MX server 'http://localhost:9090/nsncr'.
[ERROR] This Plug-In may not work together with MxUpdate Update deployment tool. Please update to newest Plug-In version.
[ERROR]     found Plug-In Version 0.10.0
[ERROR]     found Update Version 0.90.0
