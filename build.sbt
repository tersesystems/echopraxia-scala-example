ThisBuild / version := "1.3.0"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += Resolver.defaultLocal

val echopraxiaVersion = "3.0.2"
val echopraxiaPlusScalaVersion = "1.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "echopraxia-scala-example",

    // different styles of logger
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "async-logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "flow-logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "trace-logger" % echopraxiaPlusScalaVersion,

    // logger implementation
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.8",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.4",

    // specialized field builders and loggers
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "nameof" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "diff" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,
    
    // specialized conditions
    libraryDependencies += "com.tersesystems.echopraxia" % "scripting" % echopraxiaVersion,
    libraryDependencies += "com.tersesystems.echopraxia" % "filewatch" % echopraxiaVersion,

    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test
  )
