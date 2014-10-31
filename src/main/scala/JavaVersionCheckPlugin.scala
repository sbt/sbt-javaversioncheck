package com.typesafe.sbt

import sbt._
import Keys._
import compiler.JavaTool
import plugins.JvmPlugin

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
      val javac = (compileInputs in (Compile, compile)).value.compilers.javac
      JavaVersionCheck((javaVersionPrefix in javaVersionCheck ).value, javac, log)
    },
    // we hook onto deliverConfiguration to run the version check as early as possible,
    // before we actually do anything. But we don't want to require the version check
    // just for compile.
    deliverConfiguration := {
      val log = streams.value.log
      log.info("will publish with javac version " + javaVersionCheck.value)
      deliverConfiguration.value
    },
    deliverLocalConfiguration := {
      val log = streams.value.log
      log.info("will publish locally with javac version " + javaVersionCheck.value)
      deliverLocalConfiguration.value
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
    javac(sources = Nil,
      classpath = Nil,
      outputDirectory = file("."),
      options = Seq("-version"))(captureVersionLog)
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
