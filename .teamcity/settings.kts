/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
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

import builds.BwcChecks
import builds.OssChecks
import builds.SanityCheck
import builds.XpackChecks
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildTypeSettings.Type.COMPOSITE
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import templates.DefaultTemplate

version = "2020.1"

project {
    vcsRoot(DefaultRoot)
    template(DefaultTemplate)

    defaultTemplate = DefaultTemplate

    params {
        param("teamcity.internal.webhooks.enable", "true")
        param("teamcity.internal.webhooks.events", "BUILD_STARTED;BUILD_FINISHED")
        param("teamcity.internal.webhooks.url", "https://homer.app.elstc.co/webhook/teamcity")
    }

    buildType {
        id("Intake")
        name = "Intake"
        type = COMPOSITE

        dependsOn(OssChecks, XpackChecks, BwcChecks) {
            onDependencyFailure = FailureAction.ADD_PROBLEM
            onDependencyCancel = FailureAction.ADD_PROBLEM
        }

        triggers {
            vcs {
                perCheckinTriggering = true
                branchFilter = "+:<default>"
            }
        }
    }

    buildType {
        id("PullRequest")
        name = "Pull Request"
        type = COMPOSITE

        dependsOn(OssChecks, XpackChecks, BwcChecks) {
            onDependencyFailure = FailureAction.ADD_PROBLEM
            onDependencyCancel = FailureAction.ADD_PROBLEM
        }

        triggers {
            vcs {
                branchFilter = "+:pull/*"
            }
        }
    }

    subProject {
        id("Checks")
        name = "Checks"

        buildType(SanityCheck)
        buildType(OssChecks)
        buildType(XpackChecks)
        buildType(BwcChecks)
    }

}
