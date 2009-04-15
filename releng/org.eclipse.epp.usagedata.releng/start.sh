#!/bin/sh
#====================================================================================
#  Copyright (c) 2008 The Eclipse Foundation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#====================================================================================

#====================================================================================#
#This script builds the EPP Usage Data Collector feature and accompanying
# plug-ins. The output of the build is an update site. This script is
# designed to work on the Eclipse Foundation's build server, or on Wayne's
# laptop (Ubuntu Linux 8.10); it sorts out it's environment itself.
#
# The output will be compressed using pack200. If the script is running
# on the foundation build server, the output will also be signed using the
# Foundation's certificate.
#====================================================================================

if [ -f /opt/ibm/java2-ppc-50/bin/java ]; then
	JAVA=/opt/ibm/java2-ppc-50/bin/java
	BUILD_ROOT=/shared/technology/epp/udc_build
	ECLIPSE_BASES=${BUILD_ROOT}/bases
	UPDATE_ROOT=/home/data/httpd/download.eclipse.org/technology/epp/updates/testing
else
	# Assume that we're running on Wayne's Laptop.
	JAVA=java
	BUILD_ROOT=${HOME}/epp/build
	ECLIPSE_BASES=${HOME}/Eclipse
	UPDATE_ROOT=${HOME}/epp/updates
fi

# Take build type from the command-line; assume 'N' if not specified.
BUILD_TYPE=${1:-N} 

ECLIPSE_ROOT=${ECLIPSE_BASES}/eclipse-SDK-3.5M6-linux-gtk/eclipse
UPDATE_SITE=${UPDATE_ROOT}/${BUILD_TYPE}

BUILD_DATE=`date +%Y%m%d`
BUILD_TIME=`date +%H%M`
TIMESTAMP=${BUILD_DATE}${BUILD_TIME}

echo "Starting build..."
echo "Build Type: ${BUILD_TYPE}, timestamp: ${TIMESTAMP}"

rm -rf ${BUILD_ROOT}/workspace

mkdir ${BUILD_ROOT}
cd ${BUILD_ROOT}
cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/technology co org.eclipse.epp/releng/org.eclipse.epp.usagedata.releng/

# Find the launcher JAR and PDE Build Plugin directory for the current platform.
LAUNCHER_JAR=`find ${ECLIPSE_ROOT} -type f -name 'org.eclipse.equinox.launcher_*.jar' -print0`
PDE_BUILD_PLUGIN=`find ${ECLIPSE_ROOT} -type d -name 'org.eclipse.pde.build_*' -print0`

${JAVA} -jar ${LAUNCHER_JAR} \
        -application org.eclipse.ant.core.antRunner \
        -buildfile ${PDE_BUILD_PLUGIN}/scripts/build.xml \
        -Dbuilder=${BUILD_ROOT}/org.eclipse.epp/releng/org.eclipse.epp.usagedata.releng/ \
        -DbaseLocation=${ECLIPSE_ROOT} \
        -DbuildDirectory=${BUILD_ROOT}/workspace/ \
        -Dbase=${BUILD_ROOT} \
        -DbuildId=${TIMESTAMP} \
        -Dtimestamp=${TIMESTAMP} \
        -DupdateSite=${UPDATE_SITE} \
        -DbuildType=${BUILD_TYPE} \
        -Dudc.pack200=true \
        -DlauncherJar=${LAUNCHER_JAR} 
        
${JAVA} -jar ${LAUNCHER_JAR} \
   -application org.eclipse.equinox.p2.metadata.generator.EclipseGenerator \
   -updateSite ${UPDATE_SITE} \
   -site file:${UPDATE_SITE}/site.xml \
   -metadataRepository file:{UPDATE_SITE} \
   -metadataRepositoryName "UDC Update Site" \
   -artifactRepository file:{UPDATE_SITE} \
   -artifactRepositoryName "UDC Artifacts" \
   -compress \
   -append \
   -reusePack200Files \
   -noDefaultIUs \
   -vmargs -Xmx256m 

