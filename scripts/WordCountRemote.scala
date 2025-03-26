//> using scala 3

//> using packaging.packageType assembly
//> using packaging.provided org.apache.flink:flink-clients
//> using packaging.provided org.scala-lang:scala-library
//> using packaging.provided org.scala-lang:scala-reflect
//> using packaging.provided org.scala-lang:scala3-library_3
//> using packaging.provided org.scalameta::mdoc

//> using dep "org.flinkextended::flink-scala-api:1.18.1_1.2.0"
//> using dep "org.apache.flink:flink-clients:1.18.1"

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*

import org.apache.flink.streaming.api.environment.{
  StreamExecutionEnvironment => JavaEnv
}
import org.apache.flink.client.deployment.executors.RemoteExecutor
import org.apache.flink.configuration.{
  Configuration,
  DeploymentOptions,
  JobManagerOptions,
  RestOptions
}

/**
 * Word Cound example running job remotely (session cluster)
 * 
 * @jmHost - Job Manager hostname to submit job remotely
 * sessioncluster-7e217a55-4ac7-466c-a6c9-0d48eea73204-jobmanager
 */
@main def wordCountRemote(jmHost: String) =    
  val restPort = 8081

  val cfg = Configuration()
  cfg.setString(JobManagerOptions.ADDRESS, jmHost)
  cfg.setInteger(JobManagerOptions.PORT, restPort)
  cfg.setString(DeploymentOptions.TARGET, RemoteExecutor.NAME)
  cfg.setBoolean(DeploymentOptions.ATTACHED, true)
  cfg.setString(RestOptions.ADDRESS, jmHost)
  cfg.setInteger(RestOptions.PORT, restPort)

  val localMavenPath = s"${sys.props("user.home")}/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2"
  val jars = List(
    "./WordCountApp.jar",
    s"$localMavenPath/org/scala-lang/scala3-library_3/3.6.1/scala3-library_3-3.6.1.jar",
    s"$localMavenPath/org/scala-lang/scala-library/2.13.15/scala-library-2.13.15.jar"
  )
  val javaEnv = JavaEnv.createRemoteEnvironment(jmHost, restPort, cfg, jars*)
  val env = StreamExecutionEnvironment(javaEnv)

  val text = env.fromElements(
    "To be, or not to be,--that is the question:--",
    "Whether 'tis nobler in the mind to suffer",
    "The slings and arrows of outrageous fortune",
    "Or to take arms against a sea of troubles,"
  )

  text
    .flatMap(_.toLowerCase.split("\\W+"))
    .map((_, 1))
    .keyBy(_._1)
    .sum(1)
    .print()

  env.execute("wordCount")
