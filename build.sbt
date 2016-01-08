name := "ALDataLab"

version := "1.2"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.5.2" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-hive" % "1.5.2" % "provided"
libraryDependencies += "com.databricks" %% "spark-csv" % "1.2.0"
libraryDependencies += "joda-time" % "joda-time" % "2.8"

enablePlugins(UniversalPlugin)

mappings in Universal <+= (assemblyPackageScala in assembly, assemblyJarName in assembly) map { (jar,name) =>
  jar -> ("lib/" + name)
}
