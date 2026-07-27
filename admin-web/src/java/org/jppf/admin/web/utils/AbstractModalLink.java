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

package org.jppf.admin.web.utils;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <F> the type of form displayed in the modal dialog.
 * @author Laurent Cohen
 */
public abstract class AbstractModalLink<F extends AbstractModalForm> extends AbstractActionLink {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(AbstractModalLink.class);
  /**
   * Determines whether debug log statements are enabled.
   */
  private static final boolean debugEnabled = log.isDebugEnabled();
  /**
   * The modal dialog opened upon click on the button.
   */
  protected transient ModalDialog modal;
  /**
   * The form displayed inside the modal dialog.
   */
  protected F modalForm;
  /**
   * The class of the panel displayed inside the modal dialog.
   */
  protected Class<? extends Panel> panelClass;

  /**
   * 
   * @param id id of this component.
   * @param model model of this component.
   * @param imageName name of the associated icon, if any.
   * @param panelClass class of the associated modal panel.
   * @param form the form to which the modal dialog is added.
   */
  public AbstractModalLink(final String id, final IModel<String> model, final String imageName, final Class<? extends Panel> panelClass, final Form<String> form) {
    super(id, model);
    this.imageName = imageName;
    this.panelClass = panelClass;
    
    modal = new ModalDialog(id + ".dialog") {
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
  protected void onInitialize() {
    super.onInitialize();
    modalForm = createForm();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onClick(final AjaxRequestTarget target) {
    if (debugEnabled) log.debug("clicked on {}, target page = {}, target = {}", getDefaultModelObject(), target.getPage(), target.getComponents());
    addTableTreeToTarget(target);
    stopRefreshTimer(target);
    
    final ModalPanelCreator<F, Panel> creator = new ModalPanelCreator<>(modalForm, (Class<Panel>) panelClass);
    modal.open(creator.createPanel(ModalDialog.CONTENT_ID), target);
  }

  /**
   * Create a new form added to the panel in the modal dialog.
   * @return a new form.
   */
  protected abstract F createForm();
}