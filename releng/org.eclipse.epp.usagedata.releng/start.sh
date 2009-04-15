#!/bin/sh

JAVA=/opt/ibm/java2-ppc-50/bin/java
BUILD_ROOT=/shared/technology/epp/udc_build
ECLIPSE_ROOT=${BUILD_ROOT}/bases/eclipse-SDK-3.5M6-linux-gtk/eclipse
UPDATE_SITE=/home/data/httpd/download.eclipse.org/technology/epp/updates/testing/

BUILD_DATE=`date +%Y%m%d`
BUILD_TIME=`date +%H%M`
TIMESTAMP=${BUILD_DATE}${BUILD_TIME}

rm -r ${BUILD_ROOT}/workspace

cd ${BUILD_ROOT}
cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/technology co org.eclipse.epp/releng/org.eclipse.epp.usagedata.releng/

# Find the launcher JAR and PDE Build Plugin directory for the current platform.
LAUNCHER_JAR=`find ${ECLIPSE_ROOT} -type f -name 'org.eclipse.equinox.launcher_*.jar' -print0`
PDE_BUILD_PLUGIN=`find ${ECLIPSE_ROOT} -type d -name 'org.eclipse.pde.build_*' -print0`

${JAVA} -jar ${LAUNCHER_JAR} \
        -application org.eclipse.ant.core.antRunner \
        -buildfile ${PDE_BUILD_PLUGIN}/scripts/build.xml \
        -Dbuilder=${BUILD_ROOT}/org.eclipse.epp/releng/org.eclipse.epp.usagedata.releng/ \
        -DbuildDirectory=${BUILD_ROOT}/workspace/ \
        -Dbase=${BUILD_ROOT} \
        -DbuildId=${TIMESTAMP} \
        -Dtimestamp=${TIMESTAMP} \
        -DupdateSite=${UPDATE_SITE} \
        -DbuildType=R \
        -Dudc.pack200=true \
        -Dudc.signJars=true
