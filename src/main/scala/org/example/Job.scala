package org.example

/** Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */

//import org.apache.flink.api.scala._
import io.findify.flink.api._
import io.findify.flinkadt.api._
import org.apache.flink.api.java.ExecutionEnvironment

/** Skeleton for a Flink Job.
  *
  * For a full example of a Flink Job, see the WordCountJob.scala file in the
  * same package/directory or have a look at the website.
  *
  * You can also generate a .jar file that you can submit on your Flink cluster.
  * Just type
  * {{{
  *   sbt clean assembly
  * }}}
  * in the projects root directory. You will find the jar in
  * target/scala-2.12/flink-sandbox-assembly-0.1-SNAPSHOT.jar
  */
object Job extends App {
  val env = ExecutionEnvironment.getExecutionEnvironment
  env.fromElements(1, 2, 3, 4, 5, 6).filter(_ % 2 == 1).map(i => i * i).print
}
