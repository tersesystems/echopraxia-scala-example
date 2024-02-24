ThisBuild / version := "1.3.0"

ThisBuild / scalaVersion := "2.13.12"

ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += Resolver.defaultLocal

lazy val root = (project in file("."))
  .settings(
    name := "echopraxia-scala-example",

    addCompilerPlugin("io.tryp" % "splain" % "1.1.0-RC1" cross CrossVersion.patch),

    scalacOptions += "-P:splain:enabled",
    scalacOptions += "-Vimplicits",
    scalacOptions += "-Vimplicit-conversions",
    scalacOptions += "-Xlog-implicits",

    // different styles of logger
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion,
    // libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,

    // logger implementation
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.14",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test
  )
val echopraxiaVersion = "3.1.2"
val echopraxiaPlusScalaVersion = "1.3.0"
