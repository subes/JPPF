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

package org.jppf.admin.web.topology.serverstop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.jppf.admin.web.JPPFWebSession;
import org.jppf.admin.web.topology.TopologyConstants;
import org.jppf.admin.web.topology.TopologyTreeData;
import org.jppf.admin.web.utils.AbstractModalLink;
import org.jppf.client.monitoring.topology.TopologyDriver;
import org.jppf.management.JMXDriverConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Laurent Cohen
 */
public class DriverStopRestartLink extends AbstractModalLink<DriverStopRestartForm> {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(DriverStopRestartLink.class);
  /**
   * Determines whether debug log statements are enabled.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();

  /**
   * @param form .
   */
  public DriverStopRestartLink(final Form<String> form) {
    super(TopologyConstants.SERVER_STOP_RESTART_ACTION, Model.of("Server stop/restart"), "server_restart.gif", DriverStopRestartPanel.class, form);
  }

  @Override
  protected DriverStopRestartForm createForm() {
    return new DriverStopRestartForm(modal, () -> doOK());
  }

  /**
   * Called when the ok button is closed.
   */
  private void doOK() {
    final JPPFWebSession session = (JPPFWebSession) getPage().getSession();
    final TopologyTreeData data = session.getTopologyData();
    final List<TopologyDriver> selectedDrivers = TopologyTreeData.getSelectedDrivers(data.getSelectedTreeNodes());
    if (!selectedDrivers.isEmpty()) {
      final long shutdownDelay = modalForm.getShutdownDelay();
      final long restartDelay = modalForm.isRestart() ? modalForm.getRestartDelay() : -1L;
      final Set<String> uuids = new HashSet<>();
      final List<JMXDriverConnectionWrapper> list = new ArrayList<>();
      for (final TopologyDriver driver: selectedDrivers) {
        if (!uuids.contains(driver.getUuid())) {
          uuids.add(driver.getUuid());
          list.add(driver.getJmx());
        }
      }
      for (final JMXDriverConnectionWrapper jmx: list) {
        try {
          jmx.restartShutdown(shutdownDelay, restartDelay);
        } catch(final Exception e) {
          log.error(e.getMessage(), e);
        }
      }
    }
  }
}