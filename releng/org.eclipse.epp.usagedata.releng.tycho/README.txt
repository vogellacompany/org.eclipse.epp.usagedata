
Build, when run with the -Pbuild-server option, drops its output into /shared/technology/epp/updates/

After the build has completed, from build.eclipse.org, run:

rsync -av /shared/technology/epp/updates/ /home/data/httpd/download.eclipse.org/technology/epp/updates/