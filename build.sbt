Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / resolvers ++= Seq(
  Resolver.mavenCentral,
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "flink-sandbox"
version := "0.1"
organization := "org.example"

ThisBuild / scalaVersion := "3.3.0"
ThisBuild / scalacOptions ++= Seq("-new-syntax", "-rewrite")

val flinkVersion = "1.15.4"
val log4jVersion = "2.17.1"

val flinkDependencies = Seq(
  ("org.flinkextended" %% "flink-scala-api" % s"${flinkVersion}_1.1.0")
    .excludeAll(
      ExclusionRule(organization = "org.apache.flink")
    ),
  "org.apache.flink" % "flink-runtime-web" % flinkVersion % Provided,
  "org.apache.flink" % "flink-clients" % flinkVersion % Provided,  
  "org.apache.flink" % "flink-state-processor-api" % flinkVersion,
  "org.apache.flink" % "flink-csv" % flinkVersion % Provided,
  "org.apache.flink" % "flink-connector-kafka" % flinkVersion % Provided,
  "org.apache.flink" % "flink-connector-files" % flinkVersion % Provided,
  "org.apache.flink" % "flink-test-utils" % flinkVersion % Test,
  "org.apache.flink" % "flink-streaming-java" % flinkVersion % Test classifier ("tests"),
  "org.scalatest" %% "scalatest" % "3.2.13" % Test
)

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= flinkDependencies ++ Seq(
      "ch.qos.logback" % "logback-classic" % "1.3.0-alpha10" % Provided,
      "io.bullet" %% "borer-core" % "1.10.0"
    ),
    assembly / mainClass := Some("org.example.fraud.FraudDetectionJob"),
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      cp filter { f =>
        Set(
          "scala-asm-9.3.0-scala-1.jar",
          "interface-1.0.4.jar",
          "scala-compiler-2.13.6.jar"
        ).contains(
          f.data.getName
        )
      }
    },
    Test / fork := true
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
