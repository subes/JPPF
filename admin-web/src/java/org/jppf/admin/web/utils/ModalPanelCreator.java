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

import java.lang.reflect.Constructor;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Helper class to dynamically instantiate modal panels.
 * 
 * @param <F> the type of the form to use.
 * @param <P> the type of modal panel.
 * @author Laurent Cohen
 */
public class ModalPanelCreator<F extends Form<String>, P extends Panel> {
  /**
   * The form to add to the panel.
   */
  private final F form;
  /**
   * The class of the panel to instantiate.
   */
  private final Class<P> panelClass;

  /**
   * 
   * @param form the form to add to the panel.
   * @param panelClass the class of the panel to instantiate.
   */
  public ModalPanelCreator(final F form, final Class<P> panelClass) {
    if (form == null) throw new IllegalArgumentException("the form cannot be null");
    if (panelClass == null) throw new IllegalArgumentException("the panel class cannot be null");
    this.form = form;
    this.panelClass = panelClass;
  }
  
  /**
   * Instantiates the panel class using a (String id, Form form) constructor.
   * @param id component id (typically ModalDialog.CONTENT_ID).
   * @return the created panel instance.
   */
  public P createPanel(final String id) {
    try {
      final Constructor<P> c = panelClass.getConstructor(String.class, form.getClass());
      return c.newInstance(id, form);
    } catch (final Exception e) {
      throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
    }
  }
}