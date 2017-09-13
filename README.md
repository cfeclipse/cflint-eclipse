org.cfeclipse.cflint
========

To build use:
mvn clean install

To update the version number prior to a build, run:
`mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion==2.2.14-SNAPSHOT` 

#Release example
(we do our final commit for 2.2.13-SNAPSHOT on the develop branch, we're ready to release it)
[develop]$`git checkout master`
 [master]$`git merge --no-ff develop`
 [master]$`mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=2.4.13-SNAPSHOT`
 [master]$`mvn clean verify`
 [master]$`git commit -am 'Release version 2.2.13'`
 [master]$`git tag -a 2.2.13`
 [master]$`git push origin 2.2.13`
 [master]$`git checkout develop`
[develop]$`git merge --no-ff master`
[develop]$`mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion==2.2.14-SNAPSHOT`
[develop]$`git commit -am 'Setup version 2.2.14 for development'`
