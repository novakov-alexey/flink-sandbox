//> using dep "org.flinkextended::flink-scala-api:1.17.1_1.0.0"
//> using dep "org.apache.flink:flink-clients:1.17.1"
//> using dep "org.apache.flink:flink-csv:1.17.1"
//> using dep "org.apache.flink:flink-connector-files:1.17.1"
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
    .option("fields.id.start", "1")
    .option("fields.id.end", "10000")
    .build
)

val currentDirectory = _root_.java.io.File(".").getCanonicalPath

table.createTemporaryTable(
  "SinkTable",
  TableDescriptor
    .forConnector("filesystem")
    .schema(schema)
    .option("format", "csv")
    .option("sink.rolling-policy.file-size", "124 kb")
    .option("path", s"file://$currentDirectory/sink-table")
    .build
)

table.executeSql("insert into SinkTable select * from SourceTable").print
