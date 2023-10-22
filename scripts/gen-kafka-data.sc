//> using dep "org.flinkextended::flink-scala-api:1.17.1_1.0.0"
//> using dep "org.apache.flink:flink-clients:1.17.1"
//> using dep "org.apache.flink:flink-csv:1.17.1"
//> using dep "org.apache.flink:flink-connector-files:1.17.1"
//> using dep "org.apache.flink:flink-connector-kafka:1.17.1"
//> using dep "org.apache.flink:flink-table-runtime:1.17.1"
//> using dep "org.apache.flink:flink-table-planner-loader:1.17.1"

import org.apache.flink.table.api._
import org.apache.flink.connector.datagen.table.DataGenConnectorOptions
import org.apache.flink.api._
import org.apache.flink.api.serializers._

import _root_.java.lang.{Long => JLong}

val env = StreamExecutionEnvironment.getExecutionEnvironment
val settings = EnvironmentSettings.newInstance.inStreamingMode.build
val table = TableEnvironment.create(settings)
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
    .option(DataGenConnectorOptions.NUMBER_OF_ROWS, JLong(1000))
    .option("fields.id.kind", "sequence")
    .option("fields.id.start", "10001")
    .option("fields.id.end",   "20000")
    .build
)

val brokers = "confluentkafka-cp-kafka:9092"

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

table.executeSql("insert into SinkTable select * from SourceTable").print
