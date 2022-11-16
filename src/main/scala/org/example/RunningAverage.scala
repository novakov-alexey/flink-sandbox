package org.example

import org.apache.flink.api.common.functions.RichMapFunction
//import org.apache.flink.api.scala.typeutils.Types
import io.findify.flink.api._
import io.findify.flinkadt.api._
import org.apache.flink.walkthrough.common.entity.Transaction
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.configuration.Configuration
import org.apache.flink.api.common.typeinfo.TypeInformation

class RunningAverage
    extends RichMapFunction[Transaction, (Transaction, Double)] {

  implicit val tranTypeInfo: TypeInformation[Transaction] =
    TypeInformation.of(classOf[Transaction])

  @transient lazy val runningAvg = getRuntimeContext.getState(
    new ValueStateDescriptor(
      "running-average",
      classOf[Double],
      0d
    )
  )

  @transient lazy val count = getRuntimeContext.getState(
    new ValueStateDescriptor("count", classOf[Int], 0)
  )
  // @transient lazy val count = getRuntimeContext.getMetricGroup().counter("counter")

  private def threadName = Thread.currentThread.getName
  override def open(config: Configuration): Unit =
    println(s"open map: $threadName")

  override def map(t: Transaction): (Transaction, Double) = {
    Option(count.value) match {
      case Some(cnt) => count.update(cnt + 1)
      case _         => ()
    }

    Option(runningAvg.value) match {
      case Some(avg) => runningAvg.update((avg + t.getAmount) / count.value)
      case _         => ()
    }

    (t, runningAvg.value)
  }

}
