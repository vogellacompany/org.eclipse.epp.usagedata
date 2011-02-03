#!/bin/bash
root=:pserver:anonymous@dev.eclipse.org:/cvsroot/technology
path=org.eclipse.epp
target=workspace
tag=${1:-HEAD}
echo $tag
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
cd $path/org.eclipse.epp.usagedata.releng.tycho
mvn clean install

