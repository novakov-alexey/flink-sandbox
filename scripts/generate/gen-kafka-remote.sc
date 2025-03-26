//> using scala 3.5.1

//> using dep "org.flinkextended::flink-scala-api:1.19.1_1.2.0"
//> using dep "org.apache.flink:flink-clients:1.19.1"

//> using dep "org.apache.flink:flink-csv:1.19.1"
//> using dep "org.apache.flink:flink-connector-files:1.19.1"
//> using dep "org.apache.flink:flink-connector-kafka:3.2.0-1.19"
//> using dep "org.apache.flink:flink-table-runtime:1.19.1"
//> using dep "org.apache.flink:flink-table-planner-loader:1.19.1"
//> using dep "ch.qos.logback:logback-classic:1.5.12"

import org.apache.flink.table.api.*
import org.apache.flink.connector.datagen.table.DataGenConnectorOptions
import org.apache.flink.runtime.jobgraph.{SavepointRestoreSettings, RestoreMode}
import org.apache.flink.streaming.api.environment.{
  StreamExecutionEnvironment => JavaEnv,
  RemoteStreamEnvironment
}
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
import org.apache.flink.client.deployment.executors.RemoteExecutor
import org.apache.flink.configuration.{
  Configuration,
  DeploymentOptions,
  JobManagerOptions,
  RestOptions,
  StateBackendOptions
}

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*
import org.slf4j.LoggerFactory
import java.lang.{Long => JLong}

System.setProperty("logback.configurationFile", "./scripts/logback.xml")
val logger = LoggerFactory.getLogger(this.getClass())

val restPort = 8081
val jmHost =
  if args.isEmpty then
    sys.error("Pass Job Manager hostname as the first argument")
  else args(0)
logger.info(s"using Job Manager hostname: $jmHost")

val cfg = Configuration()
cfg.setString(JobManagerOptions.ADDRESS, jmHost)
cfg.setInteger(JobManagerOptions.PORT, restPort)
cfg.setString(DeploymentOptions.TARGET, RemoteExecutor.NAME)
cfg.setBoolean(DeploymentOptions.ATTACHED, true)
cfg.setString(RestOptions.ADDRESS, jmHost)
cfg.setInteger(RestOptions.PORT, restPort)
cfg.set(StateBackendOptions.STATE_BACKEND, "filesystem")

val localMavenPath =
  s"${sys.props("user.home")}/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2"
val jars = Array(
  s"$localMavenPath/org/apache/flink/flink-connector-kafka/3.2.0-1.19/flink-connector-kafka-3.2.0-1.19.jar",
  s"$localMavenPath/org/apache/kafka/kafka-clients/3.4.0/kafka-clients-3.4.0.jar"
)

val savepointDir = "s3://vvp/flink-savepoints/remote"
val restoreSettings =
  SavepointRestoreSettings.forPath(
    s"$savepointDir/savepoint-37bc1a-1d0100144fa0",
    false,
    RestoreMode.NO_CLAIM
  )

val env = StreamExecutionEnvironment(
  RemoteStreamEnvironment(jmHost, restPort, cfg, jars, null, null)
)

val table = StreamTableEnvironment.create(env.getJavaEnv)
val schema = Schema.newBuilder
  .column("id", DataTypes.INT())
  .column("bid_price", DataTypes.DOUBLE())
  .column("ask_price", DataTypes.DOUBLE())
  .column("order_time", DataTypes.TIMESTAMP(2))
  .build
val outSchema = Schema.newBuilder
  .column("id", DataTypes.INT().notNull())
  .column("bid_sum", DataTypes.DOUBLE())
  .column("ask_sum", DataTypes.DOUBLE())
  .primaryKey("id")
  .build
table.createTemporaryTable(
  "orders",
  TableDescriptor
    .forConnector("datagen")
    .schema(schema)
    .option(DataGenConnectorOptions.ROWS_PER_SECOND, JLong(1))
    .build
)

table.createTemporaryTable(
  "orders_print",
  TableDescriptor
    .forConnector("print")
    .schema(outSchema)
    .build
)

val brokers = "confluentkafka-cp-kafka.kafka:9092"

table.createTemporaryTable(
  "orders_kafka",
  TableDescriptor
    .forConnector("kafka")
    .schema(schema)
    .option("properties.bootstrap.servers", brokers)
    .option("properties.group.id", "orders.consumer3")
    .option("scan.startup.mode", "earliest-offset")
    .option("topic", "bids")
    .option("format", "csv")
    .option("value.format", "csv")
    .build
)

table.createTemporaryTable(
  "orders_output",
  TableDescriptor
    .forConnector("upsert-kafka")
    .schema(outSchema)
    .option("properties.bootstrap.servers", brokers)
    .option("topic", "orders_output")
    .option("key.format", "csv")
    .option("value.format", "csv")
    .option("value.fields-include", "ALL")
    .build
)

val result =
  table.executeSql(
    """
    insert into orders_output 
    select n.id, SUM(n.bid_sum) as bid_sum, SUM(n.ask_sum) as ask_sum from (
      select id, SUM(bid_price) as bid_sum, SUM(ask_price) as ask_sum from orders_kafka group by id
      union all
      select id, bid_sum, ask_sum from orders_output
    ) n group by n.id
    """
  )
  // table.executeSql(
  //   "insert into orders_kafka values (-1056269922, 1, 1, now())"
  // )

result.getJobClient().ifPresent { c =>
  sys.addShutdownHook {
    logger.info("Stopping Job.")
    val path = c.stopWithSavepoint(false, savepointDir).join()
    logger.info(s"Stopped. Savepoint path: $path")
  }
}
logger.info("Job submitted. Press any button to stop it.")
scala.io.StdIn.readLine()
logger.info("Shutting down.")
