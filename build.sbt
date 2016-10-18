name := "Devstream"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.0"
libraryDependencies += ("org.elasticsearch" % "elasticsearch" % "2.3.4")/*.exclude("joda-time", "joda-time")*/
libraryDependencies += "com.github.dasbipulkumar" % "StackexchangeApiScalaClient" % "3d60efafb5"
libraryDependencies += "org.json4s" %% "json4s-ast" % "3.2.10"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.10"


assemblyShadeRules in assembly :=Seq(
  ShadeRule.rename("com.google.**" -> "googlecommona.@1").inAll
)