ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += Resolver.defaultLocal

val echopraxiaVersion = "2.1.0"
val echopraxiaPlusScalaVersion = "1.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "echopraxia-scala-example",
    //scalacOptions += "-Ymacro-debug-verbose",
    scalacOptions ++= Seq("-Xexperimental"),
    Compile / scalacOptions ++= Seq("-Ywarn-macros:after"),
    Compile / scalacOptions --= Seq("-Ywarn-unused:params"),
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "async-logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "flow-logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "trace-logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value, // needed for auto derivation
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
    libraryDependencies += "com.tersesystems.echopraxia" % "scripting" % echopraxiaVersion,
    libraryDependencies += "com.tersesystems.echopraxia" % "filewatch" % echopraxiaVersion
  )
