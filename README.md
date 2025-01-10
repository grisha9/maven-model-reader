Maven model reader plugin
==================

Maven plugin for resolve project model.
Published in [Maven Central](https://mvnrepository.com/artifact/io.github.grisha9/maven-model-reader-plugin).
Plugin contains mojos:
- resolve (get project model with all resolved dependencies)
- read (get project model only without dependencies)
- tree (get dependency tree data, like as mvn dependency:tree plugin)

Result save in file in JSON format.  
Model Dependency - DTO for project model classes - [Maven Central](https://mvnrepository.com/artifact/io.github.grisha9/maven-project-model).


Example for usages:
```
mvn -f /project/path/pom.xml io.github.grisha9:maven-model-reader-plugin:0.4:resolve -DresultFilePath=/project/path/.gmaven.pom.json
```

#### Parameters description:  
 - resultFilePath - absolut path to result file with project model 
 - resultAsTree (default value: false) - if true then return result as tree of maven projects for multy module projects, 
  else if false then as flatten projects list
 - processingPluginGAIds - plugins DA for which extract configurations 
 (example: -DprocessingPluginGAIds=org.apache.maven.plugins:maven-compiler-plugin,org.codehaus.mojo:build-helper-maven-plugin)
 - jsonPrettyPrinting(default value: false) - pretty print for result JSON

#### Articles about GMaven: 
- [dev.to](https://dev.to/grisha9/my-intellij-idea-plugin-for-maven-support-gmaven-cn9)
