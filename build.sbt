name := "Devstream"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5"
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.0.0"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.dasbipulkumar" % "StackexchangeApiScalaClient" % "3d60efafb5"