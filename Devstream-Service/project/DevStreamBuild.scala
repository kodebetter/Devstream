import play.PlayImport.PlayKeys
import sbt._
import Keys._
import play.Play.autoImport._


object DevStreamBuild extends Build {

  val appName = "devstream"
  val appVersion = "1.0"

  val scalaVersion = "2.10.5"

  val appDependencies = Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.4",
    "org.apache.cassandra" % "cassandra-all" % "2.0.9",
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