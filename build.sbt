ThisBuild / version := "1.0.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += Resolver.defaultLocal

val echopraxiaVersion = "3.0.0-SNAPSHOT"
val echopraxiaPlusScalaVersion = "2.0.0-SNAPSHOT"

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
    
    // specialized field builders and loggers
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "nameof" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "diff" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,
    
    // specialized conditions
    libraryDependencies += "com.tersesystems.echopraxia" % "scripting" % echopraxiaVersion,
    libraryDependencies += "com.tersesystems.echopraxia" % "filewatch" % echopraxiaVersion,

    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % Test
  )
