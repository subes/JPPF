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

package org.jppf.admin.web.health.threaddump;

import java.util.List;
import java.util.Locale;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.jppf.admin.web.JPPFWebSession;
import org.jppf.admin.web.health.HealthConstants;
import org.jppf.admin.web.tabletree.TableTreeData;
import org.jppf.admin.web.utils.AbstractActionLink;
import org.jppf.client.monitoring.topology.AbstractTopologyComponent;
import org.jppf.management.diagnostics.HTMLThreadDumpWriter;
import org.jppf.management.diagnostics.ThreadDump;
import org.jppf.ui.utils.HealthUtils;
import org.jppf.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Laurent Cohen
 */
public class ThreadDumpLink extends AbstractActionLink {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(ThreadDumpLink.class);
  /**
   * Determines whether debug log statements are enabled.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();
  /**
   * Upgraded modal dialog instance.
   */
  private transient ModalDialog modal;

  /**
   * @param form .
   */
  public ThreadDumpLink(final Form<String> form) {
    super(HealthConstants.THREAD_DUMP_ACTION, Model.of("Thread dump"));
    imageName = "thread_dump.gif";
    setEnabled(false);
    
    modal = new ModalDialog("health.threaddump.dialog") {
      @Override
      public ModalDialog close(final AjaxRequestTarget target) {
        final ModalDialog result = super.close(target);
        if (target != null) {
          restartRefreshTimer(target);
        }
        return result;
      }
    };
    modal.add(new DefaultTheme());
    modal.closeOnEscape();
    modal.closeOnClick();
    form.add(modal);
  }

  @Override
  public void onClick(final AjaxRequestTarget target) {
    if (debugEnabled) log.debug("clicked on thread dump");
    final JPPFWebSession session = JPPFWebSession.get();
    final TableTreeData data = session.getHealthData();
    final List<DefaultMutableTreeNode> selectedNodes = data.getSelectedTreeNodes();
    if (!selectedNodes.isEmpty()) {
      final DefaultMutableTreeNode treeNode = selectedNodes.get(0);
      final AbstractTopologyComponent comp = (AbstractTopologyComponent) treeNode.getUserObject();
      final Locale locale = Session.get().getLocale();
      final String title = HealthUtils.getThreadDumpTitle(comp, locale);
      final StringBuilder html = new StringBuilder();
      try {
        final ThreadDump info = HealthUtils.retrieveThreadDump(comp);
        if (info == null) html.append(HealthUtils.localizeThreadDumpInfo("threaddump.info_not_found", locale));
        else html.append(HTMLThreadDumpWriter.printToString(info, title, false, 10));
      } catch(final Exception e) {
        html.append(ExceptionUtils.getStackTrace(e).replace("\n", "<br>"));
      }

      final ThreadDumpPanel content = new ThreadDumpPanel(ModalDialog.CONTENT_ID, html.toString());

      stopRefreshTimer(target);
      addTableTreeToTarget(target);
      modal.open(content, target);
    }
  }
}