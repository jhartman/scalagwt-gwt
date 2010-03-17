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
package com.google.gwt.sample.expenses.shared;

import com.google.gwt.requestfactory.shared.EntityRef;

/**
 * Implemented by entities of the {@link ExpensesRequestFactory}.
 * 
 * @param <E> the type of this entity
 */
public interface ExpensesEntity<E extends ExpensesEntity<E>>
    extends EntityRef<E> {
  
  void accept(ExpensesEntityVisitor visitor);

  <T> T accept(ExpensesEntityFilter<T> filter);
}
