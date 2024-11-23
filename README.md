Maven model reader plugin
==================

Maven plugin for resolve project model.  
Plugin contains mojos:
- resolve (get project model with all resolved dependencies)
- read (get project model only without dependencies)
- tree (get dependency tree data, like as mvn dependency:tree plugin)

Result save in file in JSON format. Published the artifact in [Maven Central](https://mvnrepository.com/artifact/io.github.grisha9/maven-model-reader-plugin).   

Example for usages:
```
mvn -f /project/path/pom.xml io.github.grisha9:maven-model-reader-plugin:0.4:resolve -DresultFilePath=/project/path/.gmaven.pom.json
```


#### Articles about GMaven: 
- [dev.to](https://dev.to/grisha9/my-intellij-idea-plugin-for-maven-support-gmaven-cn9)
