package org.example

import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.util.Collector
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.state.ValueStateDescriptor

import org.example.TransactionsSource

import org.apache.flink.api._
import org.apache.flink.api.serializers._

@main def ConnectedStreams =
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  given tranTypeInfo: TypeInformation[Transaction] =
    TypeInformation.of(classOf[Transaction])

  val control = env
    .addSource(TransactionsSource.iterator)
    .keyBy(_.accountId)

  val streamOfWords = env
    .addSource(TransactionsSource.iterator)
    .keyBy(_.accountId)
  
  control
    .connect(streamOfWords)
    .flatMap(ControlFunction())
    .print()

  env.execute()

class ControlFunction
    extends RichCoFlatMapFunction[Transaction, Transaction, Transaction]:

  @transient lazy val state = getRuntimeContext.getState(
    new ValueStateDescriptor(
      "joined-transaction",
      classOf[Double]
    )
  )

  override def flatMap1(
      t: Transaction,
      out: Collector[Transaction]
  ): Unit =
    sumUp(t, out)

  override def flatMap2(
      t: Transaction,
      out: Collector[Transaction]
  ): Unit =
    sumUp(t, out)

  private def sumUp(t: Transaction, out: Collector[Transaction]) =
    Option(state.value()) match
      case Some(v) =>
        out.collect(t.copy(amount = t.amount + v))
        state.clear()
      case None =>
        state.update(t.amount)
