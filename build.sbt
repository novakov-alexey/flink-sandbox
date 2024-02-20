Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / resolvers ++= Seq(
  Resolver.mavenCentral,
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "flink-sandbox"
version := "0.1"
organization := "org.example"

ThisBuild / scalaVersion := "3.3.1"
ThisBuild / scalacOptions ++= Seq("-new-syntax", "-rewrite")

val flinkVersion = "1.18.0"
val flinkMajorAndMinorVersion =
  flinkVersion.split("\\.").toList.take(2).mkString(".")

val log4jVersion = "2.17.1"

val flinkDependencies = Seq(
  ("org.flinkextended" %% "flink-scala-api" % s"${flinkVersion}_1.1.2")
    .excludeAll(
      ExclusionRule(organization = "org.apache.flink"),
      ExclusionRule(organization = "org.scalameta"),
      ExclusionRule(organization = "com.google.code.findbugs")
    ),
  "org.apache.flink" % "flink-runtime-web" % flinkVersion % Provided,
  "org.apache.flink" % "flink-clients" % flinkVersion % Provided,
  "org.apache.flink" % "flink-state-processor-api" % flinkVersion,
  "org.apache.flink" % "flink-csv" % flinkVersion % Provided,
  "org.apache.flink" % "flink-connector-kafka" % s"3.0.2-$flinkMajorAndMinorVersion" % Provided,
  "org.apache.flink" % "flink-connector-files" % flinkVersion % Provided,
  "org.apache.flink" % "flink-table-runtime" % flinkVersion % Provided,
  "org.apache.flink" % "flink-table-planner-loader" % flinkVersion % Provided,
  "org.apache.flink" % "flink-test-utils" % flinkVersion % Test,
  "org.apache.flink" % "flink-streaming-java" % flinkVersion % Test classifier "tests",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)

lazy val commonSettings = Seq(
  Compile / run := Defaults
    .runTask(
      Compile / fullClasspath,
      Compile / run / mainClass,
      Compile / run / runner
    )
    .evaluated,
  Compile / run / fork := true
)

lazy val root = (project in file("."))
  .aggregate(`core`, `iceberg`)
  .settings(
    publish / skip := true
  )

def excludeJars(cp: Classpath) =
  cp filter { f =>
    Set(
      "scala-asm-9.4.0-scala-1.jar",
      "interface-1.3.5.jar",
      "interface-1.0.12.jar",
      "jline-terminal-3.19.0.jar",
      "jline-reader-3.19.0.jar",
      "jline-3.21.0.jar",
      "scala-compiler-2.13.10.jar",
      "scala3-compiler_3-3.3.1.jar",
      "flink-shaded-zookeeper-3-3.7.1-16.1.jar"
    ).contains(
      f.data.getName
    )
  }

lazy val `core` = (project in file("modules/core"))
  .settings(
    libraryDependencies ++= flinkDependencies ++ Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.7" % Provided,
      "io.bullet" %% "borer-core" % "1.10.2" % Provided
    ),
    assemblyPackageScala / assembleArtifact := false,
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      excludeJars(cp)
    },
    Test / fork := false
  )
  .settings(commonSettings)

lazy val `iceberg` = (project in file("modules/iceberg"))
  .settings(
    libraryDependencies ++= flinkDependencies ++ Seq(
      "org.apache.iceberg" % "iceberg-flink-runtime-1.17" % "1.4.2" % Provided,
      "org.apache.hadoop" % "hadoop-common" % "3.3.4" % Provided,
      "org.apache.hadoop" % "hadoop-aws" % "3.3.4" % Provided,
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.4" % Provided
    ),
    assemblyPackageScala / assembleArtifact := false,
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      excludeJars(cp)
    }
  )
  .settings(commonSettings)

Global / cancelable := true
