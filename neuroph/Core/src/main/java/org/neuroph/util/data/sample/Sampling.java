/**
 * Copyright 2013 Neuroph Project http://neuroph.sourceforge.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neuroph.util.data.sample;

import org.neuroph.core.data.DataSet;

import java.util.List;

/**
 * Interface for data set sampling  methods.
 *
 * TODO: enable in-place sampling or returning different datasets for all sampling methods.
 *
 * @author Zoran Sevarac <sevarac@gmail.com>
 */
public interface Sampling {

    public DataSet[] sample(DataSet dataSet);

}
