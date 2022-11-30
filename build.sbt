Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "flink-sandbox"
version := "0.1-SNAPSHOT"
organization := "org.example"

ThisBuild / scalaVersion := "3.1.2"

val flinkVersion = "1.15.2"
val log4jVersion = "2.17.1"

val flinkDependencies = Seq(
  // Allows to use Scala 2.13 or 3.x
  ("io.findify" %% "flink-scala-api" % "1.15-2")
    .excludeAll(
      ExclusionRule(organization = "org.apache.flink")
    ),
  "org.apache.flink" % "flink-runtime-web" % flinkVersion % Provided,
  "org.apache.flink" % "flink-clients" % flinkVersion % Provided
  // "org.apache.flink" % "flink-streaming-scala_2.12" % flinkVersion, // It contains Factory class for Scala Products
)

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= flinkDependencies ++ Seq(
      "ch.qos.logback" % "logback-classic" % "1.3.0-alpha10" % Provided,
      "io.bullet" %% "borer-core" % "1.10.0"
    ),
    assembly / mainClass := Some("org.example.Job"),
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      cp
        .filter { _.data.getName == "scala-collection-compat_2.12-2.8.1.jar" }
        .filter { _.data.getName == "scala-collection-compat_3-2.7.0.jar" }
        .filter { _.data.getName == "scala-compiler.jar" }
        .filter { _.data.getName == "scala-library-2.13.8.jar" }
        .filter { _.data.getName == "scala-library.jar" }
    }
  )

// make run command include the provided dependencies
Compile / run := Defaults
  .runTask(
    Compile / fullClasspath,
    Compile / run / mainClass,
    Compile / run / runner
  )
  .evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true
