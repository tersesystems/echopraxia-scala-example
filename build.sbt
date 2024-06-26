ThisBuild / version := "1.4.0"

ThisBuild / scalaVersion := "3.4.1"

//ThisBuild / resolvers += Resolver.mavenLocal
//ThisBuild / resolvers += Resolver.defaultLocal

val echopraxiaVersion = "3.2.0"
val echopraxiaPlusScalaVersion = "1.4.0"

lazy val root = (project in file("."))
  .settings(
    name := "echopraxia-scala-example",
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,

    // logger implementation
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.6",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
  )
