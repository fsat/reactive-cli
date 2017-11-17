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

import com.lightbend.rp.reactivecli.argparse.kubernetes.KubernetesArgs
import com.lightbend.rp.reactivecli.argparse.{ GenerateDeploymentArgs, InputArgs }
import com.lightbend.rp.reactivecli.docker.{ Config, DockerRegistry }
import com.lightbend.rp.reactivecli.runtime.kubernetes
import libhttpsimple.{ HttpRequest, LibHttpSimple }
import libhttpsimple.LibHttpSimple.HttpExchange
import slogging._

import scala.annotation.tailrec
import scala.util.Try

/**
 * This is the main entry of the Reactive CLI.
 */
object Main extends LazyLogging {
  val CliName = "reactive-cli"
  val ParserVersion = "0.1.0" // TODO: ParserVersion should come from build

  val parser = InputArgs.parser(CliName, ParserVersion)

  @tailrec
  private def run(args: Array[String]): Unit = {
    if (args.nonEmpty) {
      parser.parse(args, InputArgs.default).foreach { inputArgs =>
        val inputArgsMerged = InputArgs.Envs.mergeWithEnvs(inputArgs, sys.env)

        inputArgsMerged.commandArgs
          .collect {
            case generateDeploymentArgs @ GenerateDeploymentArgs(_, _, _, _, _, Some(kubernetesArgs: KubernetesArgs), _, _, _, _) =>
              implicit val httpSettings: LibHttpSimple.Settings =
                inputArgs.tlsCacertsPath.fold(LibHttpSimple.defaultSettings)(v => LibHttpSimple.defaultSettings.copy(tlsCacertsPath = Some(v)))

              val http: HttpExchange = LibHttpSimple.http

              val dockerRegistryAuth =
                for {
                  username <- generateDeploymentArgs.registryUsername
                  password <- generateDeploymentArgs.registryPassword
                } yield HttpRequest.BasicAuth(username, password)

              def getDockerConfig(imageName: String): Try[Config] =
                DockerRegistry.getConfig(
                  http,
                  dockerRegistryAuth,
                  generateDeploymentArgs.registryUseHttps,
                  generateDeploymentArgs.registryValidateTls)(imageName, token = None).map(_._1)

              val output = kubernetes.handleGeneratedResources(kubernetesArgs.output)
              kubernetes.generateResources(getDockerConfig, output)(generateDeploymentArgs, kubernetesArgs)
                .recover {
                  case error =>
                    logger.error(s"Failure generating kubernetes resources for docker image ${generateDeploymentArgs.dockerImage.get}", error)
                }
          }
      }
    } else {
      run(Array("--help"))
    }
  }

  def main(args: Array[String]): Unit = {
    LibHttpSimple.globalInit()
    LoggerConfig.factory = TerminalLoggerFactory
    try {
      run(args)
    } finally {
      LibHttpSimple.globalCleanup()
    }
  }
}
