/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.sample.bikeshed.cookbook.client;

import com.google.gwt.bikeshed.list.shared.MultiSelectionModel;
import com.google.gwt.bikeshed.list.shared.SelectionModel;
import com.google.gwt.bikeshed.list.shared.SelectionModel.SelectionChangeEvent;
import com.google.gwt.bikeshed.tree.client.SideBySideTreeView;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * SideBySideTree Recipe.
 */
public class SideBySideTreeRecipe extends Recipe {

  public SideBySideTreeRecipe() {
    super("Side-by-side Tree");
  }

  @Override
  protected Widget createWidget() {
    FlowPanel p = new FlowPanel();

    final Label label = new Label();
    final MultiSelectionModel<String> selectionModel = new MultiSelectionModel<String>();
    selectionModel.addSelectionChangeHandler(new SelectionModel.SelectionChangeHandler() {
      public void onSelectionChange(SelectionChangeEvent event) {
        label.setText("Selected " + selectionModel.getSelectedSet().toString());
      }
    });

    SideBySideTreeView sstree = new SideBySideTreeView(new MyTreeViewModel(
        selectionModel), "...");
    sstree.setAnimationEnabled(true);
    sstree.setHeight("200px");

    p.add(sstree);
    p.add(new HTML("<hr>"));
    p.add(label);

    return p;
  }
}
