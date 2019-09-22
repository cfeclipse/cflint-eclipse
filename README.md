org.cfeclipse.cflint
========

To build use:

	mvn clean install

To update the version number prior to a build, run:

	mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion==1.3.1-SNAPSHOT

#Release example
(we do our final commit for 1.3.0-SNAPSHOT on the develop branch, we're ready to release it)

**Develop**

	$ git checkout master
	
**Master**

	$ git merge --no-ff develop
	$ mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=1.3.0
	$ mvn clean verify
	$ git commit -am 'Release version 1.3.0'
	$ git tag -a 1.3.0
	$ git push origin 1.3.0
	$ git checkout develop

**Develop** 

	$ git merge --no-ff master
	$ mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion==1.3.1-SNAPSHOT
	$ git commit -am 'Setup version 1.3.1 for development'
