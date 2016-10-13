import play.sbt.Play.autoImport._
import sbt.Keys._
import sbt._


object DevStreamBuild extends Build {

  val appName = "devstream"
  val appVersion = "1.0"

  val scalaVersion = "2.11.7"

  val appDependencies = Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.4",
    "org.apache.cassandra" % "cassandra-all" % "2.0.9",
    "org.json4s" %% "json4s-ast" % "3.2.10",
    "org.json4s" %% "json4s-jackson" % "3.2.10",
    "org.elasticsearch" % "elasticsearch" % "2.3.4",
      ws
  )

  val main = Project(appName, file(".")).enablePlugins(play.sbt.Play).settings(
    version := appVersion,
    organization := "com.devstream",
    libraryDependencies ++= appDependencies,
    libraryDependencies += jdbc,
    resolvers += (
      "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
      )
  )
}