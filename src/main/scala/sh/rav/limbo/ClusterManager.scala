/*
 * Copyright 2016 Rafal Wojdyla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sh.rav.limbo

import com.google.api.services.dataproc.model.Cluster

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Random

/**
 * Manages Dataproc clusters:
 *  * create new cluster
 *  * destroy cluster
 *  * TODO: resize cluster based on usage
 *  * TODO: monitor cluster failures (recreate if needed)
 */
object ClusterManager {

  sys.addShutdownHook({
    import scala.concurrent.ExecutionContext.Implicits.global
    clusters.values.foreach(
      _.onSuccess{ case c => DataprocClient.delete(c.getClusterName, c.getProjectId) })
  })

  private val clusters = mutable.Map.empty[(String, String), Future[Cluster]]

  private def genClusterName(name: String): String = {
    val username = sys.props("user.name")
    s"$username-$name-" + Random.nextInt(100000)
  }

  private def createNewCluster(project: String, zone: String, name: String = "limbo")
  : Future[Cluster] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val fullname = genClusterName(name)

    val createOp = DataprocClient.create(fullname, project, zone)
    val cluster = createOp.metadata.future.map { _ =>
      val cluster = DataprocClient.describe(fullname, project)
      clusters((project, zone)) = Future.successful(cluster)
      cluster
    }
    cluster
  }

  /** Get or create a new Dataproc cluster for given project and zone */
  def getDataprocCluster(project: String, zone: String): Future[Cluster] = {
    // There is possible race condition here
    clusters.getOrElse((project, zone), createNewCluster(project, zone))
  }
}

