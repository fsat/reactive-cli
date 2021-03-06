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

package com.lightbend.rp.reactivecli.argparse.kubernetes

import java.io.PrintStream
import java.nio.file.Path

import com.lightbend.rp.reactivecli.argparse.{ GenerateDeploymentArgs, InputArgs, TargetRuntimeArgs }
import com.lightbend.rp.reactivecli.runtime.kubernetes.Deployment

object KubernetesArgs {
  object Output {
    /**
     * Represents user input to save generated resources into the directory specified by [[dir]].
     */
    case class SaveToFile(dir: Path) extends Output

    /**
     * Represents user input to pipe the generated resources into the stream specified by [[out]].
     * The generated resources will be formatted in the format acceptable to `kubectl` command.
     */
    case class PipeToKubeCtl(out: PrintStream) extends Output
  }

  /**
   * Base trait which indicates the output required for generated kubernetes resources.
   */
  sealed trait Output

  val DefaultNumberOfReplicas: Int = 1
  val DefaultImagePullPolicy: Deployment.ImagePullPolicy.Value = Deployment.ImagePullPolicy.IfNotPresent

  /**
   * Convenience method to set the [[KubernetesArgs]] values when parsing the complete user input.
   * Refer to [[InputArgs.parser()]] for more details.
   */
  def set[T](f: (T, KubernetesArgs) => KubernetesArgs): (T, InputArgs) => InputArgs = { (val1: T, inputArgs: InputArgs) =>
    GenerateDeploymentArgs
      .set { (val2: T, deploymentArgs) =>
        deploymentArgs.targetRuntimeArgs match {
          case Some(v: KubernetesArgs) =>
            deploymentArgs.copy(targetRuntimeArgs = Some(f(val2, v)))
          case _ => deploymentArgs
        }
      }
      .apply(val1, inputArgs)

  }
}

/**
 * Represents user input arguments required to build Kubernetes specific resources.
 */
case class KubernetesArgs(
  kubernetesVersion: Option[Deployment.KubernetesVersion] = None,
  kubernetesNamespace: Option[String] = None,
  deploymentArgs: DeploymentArgs = DeploymentArgs(),
  serviceArgs: ServiceArgs = ServiceArgs(),
  ingressArgs: IngressArgs = IngressArgs(),
  output: KubernetesArgs.Output = KubernetesArgs.Output.PipeToKubeCtl(System.out)) extends TargetRuntimeArgs
