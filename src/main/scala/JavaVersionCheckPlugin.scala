package com.typesafe.sbt

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import xsbti.compile.{IncToolOptions, JavaTool}

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
      val log = streams.value.log
      val javac = (compileInputs in (Compile, compile)).value.compilers.javaTools().javac()
      JavaVersionCheck((javaVersionPrefix in javaVersionCheck ).value, javac, log)
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
  def apply(javaVersionPrefix: Option[String], javac: JavaTool, realLog: Logger): String = {
    val captureVersionLog = new Logger() {
      var captured: Option[String] = None
       def log(level: Level.Value, message: => String): Unit = {
         val m = message
         if (level == Level.Warn && m.startsWith("javac ")) {
           captured = Some(m.substring("javac ".length).trim)
         } else {
           realLog.log(level, m)
         }
       }
      def success(message: => String): Unit = realLog.success(message)
      def trace(t: => Throwable): Unit = realLog.trace(t)
    }
    javac.run(
      Array.empty,
      Array("-version"),
      IncToolOptions.create(java.util.Optional.empty(), true),
      null,
      captureVersionLog)
    val version: String = captureVersionLog.captured getOrElse {sys.error("failed to get or parse the output of javac -version")}
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
