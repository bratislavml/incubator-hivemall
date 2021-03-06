/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package hivemall.classifier.multiclass;

import hivemall.model.FeatureValue;
import hivemall.model.PredictionResult;

import javax.annotation.Nonnull;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;

@Description(
        name = "train_multiclass_perceptron",
        value = "_FUNC_(list<string|int|bigint> features, {int|string} label [, const string options])"
                + " - Returns a relation consists of <{int|string} label, {string|int|bigint} feature, float weight>",
        extended = "Build a prediction model by Perceptron multiclass classifier")
public final class MulticlassPerceptronUDTF extends MulticlassOnlineClassifierUDTF {

    @Override
    public StructObjectInspector initialize(ObjectInspector[] argOIs) throws UDFArgumentException {
        final int numArgs = argOIs.length;
        if (numArgs != 2 && numArgs != 3) {
            throw new UDFArgumentException(
                "MulticlassPerceptronUDTF takes 2 or 3 arguments: List<Text|Int|BitInt> features, {Int|Text} label [, constant text options]");
        }

        return super.initialize(argOIs);
    }

    @Override
    protected void train(@Nonnull final FeatureValue[] features, @Nonnull final Object actual_label) {
        PredictionResult predicted = classify(features);
        Object predicted_label = predicted.getLabel();

        if (!actual_label.equals(predicted_label)) {
            update(features, 1.f, actual_label, predicted_label);
        }
    }
}
