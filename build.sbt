sbtPlugin := true

name := "sbt-javaversioncheck"

organization := "com.typesafe.sbt"

version := "0.1.0"

description := "sbt plugin to check Java version"

licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT"))

scalacOptions := Seq("-deprecation", "-unchecked")

publishMavenStyle := false

publishTo := {
  if (isSnapshot.value) Some(Resolver.sbtPluginRepo("snapshots"))
  else Some(Resolver.sbtPluginRepo("releases"))
}

credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")
