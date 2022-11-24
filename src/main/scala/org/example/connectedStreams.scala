package org.example

import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.walkthrough.common.entity.Transaction
import org.apache.flink.walkthrough.common.source.TransactionSource
import org.apache.flink.util.Collector
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.state.ValueStateDescriptor

import io.findify.flink.api._
import io.findify.flinkadt.api._

@main def ConnectedStreams =
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  given tranTypeInfo: TypeInformation[Transaction] =
    TypeInformation.of(classOf[Transaction])

  val control = env
    .addSource(TransactionSource())
    .keyBy(_.getAccountId)

  val streamOfWords = env
    .addSource(TransactionSource())
    .keyBy(_.getAccountId)

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
        t.setAmount(t.getAmount + v) // mutation, bad!
        out.collect(t)
        state.clear()
      case None =>
        state.update(t.getAmount)
