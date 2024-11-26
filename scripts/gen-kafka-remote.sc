//> using scala 3.5.1

//> using dep "org.flinkextended::flink-scala-api:1.19.1_1.2.0"
//> using dep "org.apache.flink:flink-clients:1.19.1"

//> using dep "org.apache.flink:flink-csv:1.19.1"
//> using dep "org.apache.flink:flink-connector-files:1.19.1"
//> using dep "org.apache.flink:flink-connector-kafka:3.2.0-1.19"
//> using dep "org.apache.flink:flink-table-runtime:1.19.1"
//> using dep "org.apache.flink:flink-table-planner-loader:1.19.1"

import org.apache.flink.table.api.*
import org.apache.flink.connector.datagen.table.DataGenConnectorOptions
import org.apache.flink.streaming.api.environment.{
  StreamExecutionEnvironment => JavaEnv
}
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
import org.apache.flink.client.deployment.executors.RemoteExecutor
import org.apache.flink.configuration.{
  Configuration,
  DeploymentOptions,
  JobManagerOptions,
  RestOptions
}

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*

import java.lang.{Long => JLong}

val restPort = 8081
val jmHost = "sessioncluster-7e217a55-4ac7-466c-a6c9-0d48eea73204-jobmanager"

val cfg = Configuration()
cfg.setString(JobManagerOptions.ADDRESS, jmHost)
cfg.setInteger(JobManagerOptions.PORT, restPort)
cfg.setString(DeploymentOptions.TARGET, RemoteExecutor.NAME)
cfg.setBoolean(DeploymentOptions.ATTACHED, true)
cfg.setString(RestOptions.ADDRESS, jmHost)
cfg.setInteger(RestOptions.PORT, restPort)

val localMavenPath =
  s"${sys.props("user.home")}/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2"
val jars = List(
  s"$localMavenPath/org/apache/flink/flink-connector-kafka/3.2.0-1.19/flink-connector-kafka-3.2.0-1.19.jar",
  s"$localMavenPath/org/apache/kafka/kafka-clients/3.4.0/kafka-clients-3.4.0.jar"
)

val env = StreamExecutionEnvironment(
  JavaEnv.createRemoteEnvironment(jmHost, restPort, cfg, jars*)
)

val table = StreamTableEnvironment.create(env.getJavaEnv)
val schema = Schema.newBuilder
  .column("id", DataTypes.INT())
  .column("bid_price", DataTypes.DOUBLE())
  .column("order_time", DataTypes.TIMESTAMP(2))
  .build

table.createTemporaryTable(
  "SourceTable",
  TableDescriptor
    .forConnector("datagen")
    .schema(schema)
    .option(DataGenConnectorOptions.ROWS_PER_SECOND, JLong(1))
    .build
)

// arg 1
val brokers = "confluentkafka-cp-kafka.kafka:9092"

table.createTemporaryTable(
  "SinkTable",
  TableDescriptor
    .forConnector("kafka")
    .schema(schema)
    .option("properties.bootstrap.servers", brokers)
    .option("topic", "bids")
    .option("format", "csv")
    .option("value.format", "csv")
    .build
)

val result = table.executeSql("insert into SinkTable select * from SourceTable")

result.getJobClient().ifPresent { c =>
  sys.addShutdownHook {
    println("Stopping Job.")
    c.cancel().join()
    println("Stopped.")
  }
}
scala.io.StdIn.readLine()
println("Shutting down.")
