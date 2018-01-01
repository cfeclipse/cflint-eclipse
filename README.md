org.cfeclipse.cflint
========

To build use:
mvn clean install

To update the version number prior to a build, run:
`mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion==1.3.1-SNAPSHOT` 

#Release example
(we do our final commit for 1.3.0-SNAPSHOT on the develop branch, we're ready to release it)
[develop]$`git checkout master`
 [master]$`git merge --no-ff develop`
 [master]$`mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=1.3.0`
 [master]$`mvn clean verify`
 [master]$`git commit -am 'Release version 1.3.0'`
 [master]$`git tag -a 1.3.0`
 [master]$`git push origin 1.3.0`
 [master]$`git checkout develop`
[develop]$`git merge --no-ff master`
[develop]$`mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion==1.3.1-SNAPSHOT`
[develop]$`git commit -am 'Setup version 1.3.1 for development'`
