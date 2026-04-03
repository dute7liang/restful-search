/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dute7liang.restful.navigator;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.dute7liang.restful.common.ServiceHelper;

import java.util.List;

public class RestServiceProjectsManager {
  private final Project myProject;

  public static RestServiceProjectsManager getInstance(Project p) {
    return p.getService(RestServiceProjectsManager.class);
  }

  public RestServiceProjectsManager(Project project) {
      myProject = project;
  }

  public List<RestServiceProject> getServiceProjects() {
    List<RestServiceProject> list = DumbService.getInstance(myProject).runReadActionInSmartMode(() -> ServiceHelper.buildRestServiceProjectListUsingResolver(myProject));
    return list;
  }
/*
  public boolean hasProjects() {
    if (! myProject.isInitialized()) {
      return false;
    }
    System.out.println("======hasProjects=====");
    return getServiceProjects().size() > 0;
  }*/

  public void forceUpdateAllProjects() {

  }
}
