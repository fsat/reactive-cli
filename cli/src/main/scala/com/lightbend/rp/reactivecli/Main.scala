/*
 * Copyright 2017 Lightbend, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.rp.reactivecli

import java.nio.file.{Files, Paths, StandardOpenOption}

import libhttpsimple.LibHttpSimple
import scopt.OptionParser
import argonaut._
import Argonaut._
import scala.collection.mutable
import scala.collection.JavaConverters._

object Main {
  case class InputArgs(foo: Option[String] = None)

  val defaultInputArgs = InputArgs()

  implicit def inputArgsCodecJson = casecodec1(InputArgs.apply, InputArgs.unapply)("foo")

  val parser = new OptionParser[InputArgs]("reactive-cli") {
    head("reactive-cli", "0.1.0")

    help("help").text("Print this help text")

    opt[String]('f', "foo")
      .text("test switch called foo")
      .action((v, c) => c.copy(foo = Some(v)))
  }

  def run(inputArgs: InputArgs): Unit = {
    LibHttpSimple.globalInit()

    println(s"Got input args: $inputArgs")

    val inputArgsJson = inputArgs.asJson
    println(s"Got input args as Argonaut JSON:")
    println(inputArgsJson)

    val inputArgsJsonString = inputArgsJson.spaces2
    println(s"Got input args as JSON string:")
    println(inputArgsJsonString)

    val response = LibHttpSimple.get("https://www.example.org")
    println(s"Got HTTP response:")
    println(response)

    LibHttpSimple.globalCleanup()

    println("File I/O")
    val testDir = Paths.get("/tmp/bar")
    val testFile = testDir.resolve("test.txt")

    println(s"File I/O - delete file if exists: ${testDir.toAbsolutePath}")
    Files.deleteIfExists(testFile)

    println(s"File I/O - delete dir if exists: ${testDir.toAbsolutePath}")
    Files.deleteIfExists(testDir)

    println(s"File I/O - create dir: ${testDir.toAbsolutePath}")
    Files.createDirectories(testDir)

    println(s"File I/O - write file: ${testFile.toAbsolutePath}")
    val textContent =
      """this is a test
         |and this
         |and also this
         |""".stripMargin
    Files.write(testFile, textContent.getBytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND)

    val writtenLine = Files.readAllLines(testFile).asScala
    println(s"File I/O - content: ${writtenLine.mkString("\n")}")
  }

  def main(args: Array[String]): Unit = {
    parser.parse(args, defaultInputArgs).foreach(run)
  }
}
