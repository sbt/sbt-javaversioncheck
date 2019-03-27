ThisBuild / organization := "com.typesafe.sbt"
ThisBuild / version := "0.1.0"
ThisBuild / description := "sbt plugin to check Java version"
ThisBuild / licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT"))

lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-javaversioncheck",
    scalacOptions := Seq("-deprecation", "-unchecked"),
    publishMavenStyle := false,
    crossSbtVersions := Seq("0.13.17", "1.1.6"),
    bintrayOrganization := Some("sbt"),
    bintrayRepository := "sbt-plugin-releases",
  )
