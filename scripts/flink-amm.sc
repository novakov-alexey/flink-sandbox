import $ivy.`io.findify::flink-scala-api:1.15-2`

import $ivy.`org.apache.flink:flink-clients:1.15.2`
import $ivy.`org.apache.flink:flink-streaming-scala_2.12:1.15.2` // It contains one factory class to support Scala Products. Rest Scala code is not really required

import $ivy.`org.apache.flink:flink-table-api-java:1.15.2`
import $ivy.`org.apache.flink:flink-table-api-java-bridge:1.15.2`
import $ivy.`org.apache.flink:flink-table-runtime:1.15.2`
import $ivy.`org.apache.flink:flink-table-planner-loader:1.15.2`

import org.apache.flink.table.api._
import org.apache.flink.connector.datagen.table.DataGenConnectorOptions

import io.findify.flink.api._
import io.findify.flinkadt.api._

//val env = StreamExecutionEnvironment.getExecutionEnvironment

import org.apache.flink.table.api.{EnvironmentSettings, TableEnvironment}

val settings = EnvironmentSettings
  .newInstance()
  .inStreamingMode()
  .build()

val tableEnv = TableEnvironment.create(settings)

tableEnv.createTemporaryTable(
  "SourceTable",
  TableDescriptor
    .forConnector("datagen")
    .schema(
      Schema
        .newBuilder()
        .column("BookId", DataTypes.INT())
        .build()
    )
    .option(DataGenConnectorOptions.ROWS_PER_SECOND, new java.lang.Long(1))
    .build()
)

tableEnv.executeSql(
  "CREATE TEMPORARY TABLE SinkTable WITH ('connector' = 'print') LIKE SourceTable (EXCLUDING OPTIONS) "
)

val table1 = tableEnv.from("SourceTable")
val table2 = tableEnv.sqlQuery("SELECT * FROM SourceTable")
