ThisBuild / version := "2.0.0"

ThisBuild / scalaVersion := "3.6.2"

//ThisBuild / resolvers += Resolver.mavenLocal
//ThisBuild / resolvers += Resolver.defaultLocal

val echopraxiaVersion = "4.0.0"
val echopraxiaPlusScalaVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "echopraxia-scala-example",
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "simple" % echopraxiaPlusScalaVersion,
    libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion,

    // logger implementation
    libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.15",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "8.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )
