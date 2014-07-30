sbt-javaversioncheck
====================

sbt-javaversioncheck is an sbt plugin to check current Java version.
This is taken from activator's `JavaVersionCheck` written by Havoc in [typesafehub/activator@f3a445](https://github.com/typesafehub/activator/commit/f3a445f4aeffd35ba5e5d3d15238e10c942bf041).

setup
-----

This is an auto plugin, so you need sbt 0.13.5+. Put this in `project/javaversion.sbt`:

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-javaversioncheck" % "0.1.0")
```

usage
-----

sbt-javaversioncheck is a triggered plugin that is enabled automatically for all projects that has `JvmPlugin`.
Next rewire `javaVersionPrefix in javaVersionCheck` to `Some("1.6")` for Java 6, `Some("1.7")` for Java 7, and so on.

```scala
lazy val fooProj = (project in file("foo")).
  settings(
    javaVersionPrefix in javaVersionCheck := Some("1.6")
  )
```
