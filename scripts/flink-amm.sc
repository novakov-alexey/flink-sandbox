import $ivy.`io.findify::flink-scala-api:1.15-2`
import $ivy.`org.apache.flink:flink-queryable-state-client-java:1.15.2`

import $ivy.`org.apache.flink:flink-clients:1.15.2`
import $ivy.`org.apache.flink:flink-streaming-scala_2.12:1.15.2` // It contains one factory class to support Scala Products. Rest Scala code is not really required
import $ivy.`org.apache.flink:flink-connector-kafka:1.15.2`
import $ivy.`org.apache.flink:flink-csv:1.15.2`

import $ivy.`org.apache.flink:flink-table-api-java:1.15.2`
import $ivy.`org.apache.flink:flink-table-api-java-bridge:1.15.2`
import $ivy.`org.apache.flink:flink-table-runtime:1.15.2`
import $ivy.`org.apache.flink:flink-table-planner-loader:1.15.2`

import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
import org.apache.flink.connector.datagen.table.DataGenConnectorOptions

import io.findify.flink.api._
import io.findify.flinkadt.api._

import java.lang.{Long => JLong}
import org.apache.flink.queryablestate.client.QueryableStateClient

val env = StreamExecutionEnvironment.getExecutionEnvironment
val tEnv = StreamTableEnvironment.create(env.getJavaEnv)

val settings = EnvironmentSettings.newInstance().inStreamingMode().build()

val table = TableEnvironment.create(settings)

table.createTemporaryTable(
  "SourceTable",
  TableDescriptor
    .forConnector("datagen")
    .schema(
      Schema.newBuilder
        .column("BookId", DataTypes.INT())
        .build
    )
    .option(DataGenConnectorOptions.ROWS_PER_SECOND, new JLong(1))
    .build
)

table.executeSql(
  "CREATE TEMPORARY TABLE SinkTable WITH ('connector' = 'print') LIKE SourceTable (EXCLUDING OPTIONS) "
)

val sourceTable = table.from("SourceTable")
val sourceTableAll = table.sqlQuery("SELECT * FROM SourceTable")
