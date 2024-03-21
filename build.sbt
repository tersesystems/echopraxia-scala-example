ThisBuild / version := "1.3.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

//ThisBuild / resolvers += Resolver.mavenLocal
//ThisBuild / resolvers += Resolver.defaultLocal

val echopraxiaVersion = "3.1.2"
val echopraxiaPlusScalaVersion = "1.3.1-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "echopraxia-scala-example",

    // different styles of logger
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "field-logger" % echopraxiaPlusScalaVersion,
    //libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,

    // logger implementation
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.2",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test
  )
