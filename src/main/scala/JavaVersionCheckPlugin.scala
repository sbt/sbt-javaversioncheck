package com.typesafe.sbt

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object JavaVersionCheckPlugin extends sbt.AutoPlugin {
  val defaultJavaVersionPrefix: Option[String] = Some("1.7")

  object autoImport {
    lazy val javaVersionPrefix = settingKey[Option[String]]("java version prefix required by javaVersionCheck")
    lazy val javaVersionCheck = taskKey[String]("checks the Java version vs. javaVersionPrefix, returns actual version")
  }

  import autoImport._
  override def trigger = allRequirements
  override def requires = JvmPlugin
  override lazy val projectSettings = javaVersionCheckSettings

  def javaVersionCheckSettings: Seq[Setting[_]] = Seq(
    javaVersionPrefix in javaVersionCheck := defaultJavaVersionPrefix,
    javaVersionCheck := {
      JavaVersionCheck((javaVersionPrefix in javaVersionCheck ).value)
    },
    // we hook onto publishConfiguration to run the version check as early as possible,
    // before we actually do anything. But we don't want to require the version check
    // just for compile.
    publishConfiguration := {
      val log = streams.value.log
      log.info("will publish with javac version " + javaVersionCheck.value)
      publishConfiguration.value
    },
    publishLocalConfiguration := {
      val log = streams.value.log
      log.info("will publish locally with javac version " + javaVersionCheck.value)
      publishLocalConfiguration.value
    }
  )
}

object JavaVersionCheck {
  def apply(javaVersionPrefix: Option[String]): String = {
    val version = sys.props.get("java.version") getOrElse {sys.error("failed to get system property java.version")}

    javaVersionPrefix match {
      case Some(prefix) =>
        if (!version.startsWith(prefix)) {
          sys.error(s"javac version ${version} may not be used to publish, it has to start with ${prefix} due to javaVersionPrefix setting")
        }
      case None =>
    }
    version
  }
}
