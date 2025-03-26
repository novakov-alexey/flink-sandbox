//> using scala 3.3.5

//> using dep "com.lihaoyi::upickle:4.1.0"
//> using dep org.apache.flink:flink-connector-gcp-pubsub:3.1.0-1.18
//> using dep "org.flinkextended::flink-scala-api:1.18.1_1.2.4"
//> using dep "org.apache.flink:flink-clients:1.18.1"
//> using dep "org.apache.flink:flink-runtime-web:1.18.1"

//> using packaging.packageType assembly
//> using packaging.provided org.apache.flink:flink-clients
//> using packaging.provided org.apache.flink:flink-runtime-web
//> using packaging.provided org.scala-lang:scala-library
//> using packaging.provided org.scala-lang:scala-reflect
//> using packaging.provided org.scala-lang:scala3-library_3

import org.apache.flink.api.common.serialization.{
  AbstractDeserializationSchema,
  SerializationSchema
}
import org.apache.flink.streaming.connectors.gcp.pubsub.{
  PubSubSink,
  PubSubSource
}
import org.apache.flink.configuration.{Configuration, ConfigConstants}
import org.apache.flink.configuration.RestOptions.BIND_PORT
import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*

import upickle.default.*
import models.*

import scala.jdk.CollectionConverters.*

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate}

object models:
  case class CreatedCustomer(fullName: String, birthDate: String)
      derives ReadWriter:
    val firstName = fullName.split(" ").head
    val lastName = fullName.split(" ").last
    val age = ChronoUnit.YEARS
      .between(LocalDate.parse(birthDate), LocalDate.now())
      .toInt

  case class RegisteredCustomer(
      firstName: String,
      lastName: String,
      age: Int,
      isActive: Boolean,
      createdAt: String
  ) derives ReadWriter

  object RegisteredCustomer:
    def apply(
        firstName: String,
        lastName: String,
        age: Int
    ): RegisteredCustomer =
      RegisteredCustomer(firstName, lastName, age, true, Instant.now().toString)

// JSON serializers and deserializers
class CreatedCustomerDeserializer
    extends AbstractDeserializationSchema[CreatedCustomer]:
  override def deserialize(message: Array[Byte]): CreatedCustomer =
    read[CreatedCustomer](new String(message, "UTF-8"))

class RegisteredCustomerSerializer
    extends SerializationSchema[RegisteredCustomer]:
  override def serialize(element: RegisteredCustomer): Array[Byte] =
    write[RegisteredCustomer](element).getBytes("UTF-8")

@main def pubSub(
    projectName: String,
    subscriptionName: String,
    topicName: String,
    localMode: Boolean*
) =

  val pubsubSource = PubSubSource
    .newBuilder()
    .withDeserializationSchema(new CreatedCustomerDeserializer())
    .withProjectName(projectName)
    .withSubscriptionName(subscriptionName)    
    .build()
  val pubsubSink = PubSubSink
    .newBuilder()
    .withSerializationSchema(new RegisteredCustomerSerializer())
    .withProjectName(projectName)
    .withTopicName(topicName)
    .build()

  val env =
    if localMode.headOption.exists(_ == true) then
      val config = Configuration.fromMap(
        Map(
          ConfigConstants.LOCAL_START_WEBSERVER -> "true",
          BIND_PORT.key -> "8081",
          "execution.checkpointing.interval" -> "10 s"
        ).asJava
      )
      StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(config)
    else StreamExecutionEnvironment.getExecutionEnvironment

  env
    .addSource(pubsubSource)
    .map(cc => RegisteredCustomer(cc.firstName, cc.lastName, cc.age))
    .map(rc => if rc.age >= 30 then rc.copy(isActive = false) else rc)
    .addSink(pubsubSink)

  env.execute("customerRegistering")
