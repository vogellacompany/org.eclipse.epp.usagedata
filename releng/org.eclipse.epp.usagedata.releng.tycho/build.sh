#!/bin/bash
#/*******************************************************************************
# * Copyright (c) 2011 Eclipse Foundation and others.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *    Wayne Beaton (Eclipse Foundation) - Initial implementation
# *******************************************************************************/
#
# This script will build the UDC 
root=:pserver:anonymous@dev.eclipse.org:/cvsroot/technology
path=org.eclipse.epp
releng=$path/releng/org.eclipse.epp.usagedata.releng.tycho
repository=$path/releng/org.eclipse.epp.usagedata.repository
target=workspace
#defaults
tag=HEAD
eclipse=~/Eclipse/eclipse-rcp-helios-SR1-linux-gtk/eclipse/

for arg in $*
do
	case $arg in
    	--tag=*)
		tag=`echo $arg | sed 's/[-a-zA-Z0-9]*=//'`
		;;
    	--eclipse=*)
		user=`echo $arg | sed 's/[-a-zA-Z0-9]*=//'`
		;;
		--help)
		echo -e "./build.sh [--tag={tag}} [--eclipse={path}]\n Where {tag} is the CVS tag to build (default: HEAD) and\n {path} points to an Eclipse install (default ~/Eclipse/eclipse-rcp-helios-SR1-linux-gtk/eclipse/)"
		exit
		;;
  	esac
done

echo "Extracting ${tag} from CVS..."
cvs -d $root checkout -r $tag $path/features/org.eclipse.epp.usagedata.feature
cvs -d $root checkout -r $tag $path/plugins/org.eclipse.epp.usagedata.gathering
cvs -d $root checkout -r $tag $path/plugins/org.eclipse.epp.usagedata.recording
cvs -d $root checkout -r $tag $path/plugins/org.eclipse.epp.usagedata.ui
cvs -d $root checkout -r $tag $path/test/org.eclipse.epp.usagedata.tests
cvs -d $root checkout -r $tag $path/test/org.eclipse.epp.usagedata.gathering.tests
cvs -d $root checkout -r $tag $path/test/org.eclipse.epp.usagedata.recording.tests
cvs -d $root checkout -r $tag $path/test/org.eclipse.epp.usagedata.ui.tests
cvs -d $root checkout -r $tag $path/releng/org.eclipse.epp.usagedata.repository
cvs -d $root checkout -r $tag $path/releng/org.eclipse.epp.usagedata.releng.tycho

echo "Starting Maven build..."
mvn -B --file ${releng}/pom.xml clean install

echo "Starting Pack200"
#jarprocessor=`find ${eclipse} -type f -name 'org.eclipse.equinox.p2.jarprocessor_*.jar' -print0`
#echo "- Found JAR Processor at ${jarprocessor}"
#java -jar ${jarprocessor} -verbose -outputDir ${repository}/target/pack200/ -processAll -repack -pack ${repository}/target/site_assembly.zip 
    
launcher=`find ${eclipse} -type f -name 'org.eclipse.equinox.launcher_*.jar' -print0`
output=${repository}/target/optimized
java -jar ${launcher} -application org.eclipse.update.core.siteOptimizer -digestBuilder \
  -jarProcessor -verbose -outputDir ${optimized} -processAll -repack -pack ${repository}/target/site_assembly.zip
