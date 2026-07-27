/*
 * JPPF.
 * Copyright (C) 2005-2019 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jppf.admin.web.jobs.maxnodes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.jppf.admin.web.JPPFWebConsoleApplication;
import org.jppf.admin.web.JPPFWebSession;
import org.jppf.admin.web.jobs.JobsConstants;
import org.jppf.admin.web.tabletree.TableTreeData;
import org.jppf.admin.web.utils.AbstractModalLink;
import org.jppf.client.monitoring.jobs.AbstractJobComponent;
import org.jppf.client.monitoring.jobs.Job;
import org.jppf.client.monitoring.jobs.JobDriver;
import org.jppf.client.monitoring.topology.TopologyDriver;
import org.jppf.job.JobSelector;
import org.jppf.job.JobUuidSelector;
import org.jppf.utils.collections.ArrayListHashMap;
import org.jppf.utils.collections.CollectionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Laurent Cohen
 */
public class MaxNodesLink extends AbstractModalLink<MaxNodesForm> {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(MaxNodesLink.class);
  /**
   * Determines whether debug log statements are enabled.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();

  /**
   * @param form .
   */
  public MaxNodesLink(final Form<String> form) {
    super(JobsConstants.UPDATE_MAX_NODES_ACTION, Model.of("Max nodes"), "select_nodes.gif", MaxNodesPanel.class, form);
  }

  @Override
  protected MaxNodesForm createForm() {
    return new MaxNodesForm(modal, () -> doOK());
  }
  
  /**
   * Called when the ok button is clicked.
   */
  private void doOK() {
    final JPPFWebSession session = (JPPFWebSession) getPage().getSession();
    final TableTreeData data = session.getJobsData();
    final List<DefaultMutableTreeNode> selectedNodes = data.getSelectedTreeNodes();
    final CollectionMap<TopologyDriver, String> map = new ArrayListHashMap<>();
    for (final DefaultMutableTreeNode treeNode: selectedNodes) {
      final AbstractJobComponent comp = (AbstractJobComponent) treeNode.getUserObject();
      if ((comp instanceof Job) && (comp.getParent() != null)) {
        final Job job = (Job) comp;
        final List<JobDriver> drivers = JPPFWebConsoleApplication.get().getJobMonitor().getDriversForJob(job.getUuid());
        for (final JobDriver driver: drivers) map.putValue(driver.getTopologyDriver(), job.getUuid());
      }
    }
    final boolean unlimited = modalForm.isUnlimited();
    final int nbNodes = unlimited ? Integer.MAX_VALUE : modalForm.getNbNodes();
    for (final Map.Entry<TopologyDriver, Collection<String>> entry: map.entrySet()) {
      final TopologyDriver driver = entry.getKey();
      final JobSelector selector = new JobUuidSelector(entry.getValue());
      try {
        driver.getJobManager().updateMaxNodes(selector, nbNodes);
      } catch(final Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }
}