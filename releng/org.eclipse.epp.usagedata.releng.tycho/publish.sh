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
eclipse=~/Eclipse/eclipse-rcp-helios-SR1-linux-gtk/eclipse/

for arg in $*
do
	case $arg in
    	--eclipse=*)
		user=`echo $arg | sed 's/[-a-zA-Z0-9]*=//'`
		;;
		--help)
		echo -e "./publish.sh [--eclipse={path}]\n Where {path} points to an Eclipse install (default ~/Eclipse/eclipse-rcp-helios-SR1-linux-gtk/eclipse/)"
		exit
		;;
  	esac
done

target=`pwd`/org.eclipse.epp/releng/org.eclipse.epp.usagedata.repository/target
launcher=`find ${eclipse} -type f -name 'org.eclipse.equinox.launcher_*.jar' -print0`
java -jar ${launcher} \
 -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher \
 -metadataRepository file:${target}/publish \
 -artifactRepository file:${target}/publish \
 -source ${target}/site \
 -compress \
 -publishArtifacts
 