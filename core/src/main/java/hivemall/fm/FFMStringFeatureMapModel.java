/*
 * Hivemall: Hive scalable Machine Learning Library
 *
 * Copyright (C) 2015 Makoto YUI
 * Copyright (C) 2013-2015 National Institute of Advanced Industrial Science and Technology (AIST)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hivemall.fm;

import hivemall.common.EtaEstimator;
import hivemall.utils.collections.IMapIterator;
import hivemall.utils.collections.OpenHashTable;

import javax.annotation.Nonnull;

public final class FFMStringFeatureMapModel extends FieldAwareFactorizationMachineModel {
    private static final int DEFAULT_MAPSIZE = 4096;

    // LEARNING PARAMS
    private float _w0;
    private final OpenHashTable<String, Entry> _map;

    public FFMStringFeatureMapModel(boolean classification, int factor, float lambda0,
            double sigma, long seed, double minTarget, double maxTarget, @Nonnull EtaEstimator eta,
            @Nonnull VInitScheme vInit, boolean useAdaGrad, float eta0_V, float eps, float scaling) {
        super(classification, factor, lambda0, sigma, seed, minTarget, maxTarget, eta, vInit, useAdaGrad, eta0_V, eps, scaling);
        this._w0 = 0.f;
        this._map = new OpenHashTable<String, FFMStringFeatureMapModel.Entry>(DEFAULT_MAPSIZE);
    }

    @Nonnull
    FFMPredictionModel toPredictionModel() {
        return new FFMPredictionModel(_map, _w0, _factor);
    }

    @Override
    public int getSize() {
        return _map.size();
    }

    IMapIterator<String, Entry> entries() {
        return _map.entries();
    }

    @Override
    public float getW0() {
        return _w0;
    }

    @Override
    protected void setW0(float nextW0) {
        this._w0 = nextW0;
    }

    @Override
    public float getW(@Nonnull final Feature x) {
        String j = x.getFeature();
        assert (j != null);

        Entry entry = _map.get(j);
        if (entry == null) {
            return 0.f;
        }
        return entry.W;
    }

    @Override
    protected void setW(@Nonnull final Feature x, final float nextWi) {
        String j = x.getFeature();
        assert (j != null);

        Entry entry = _map.get(j);
        if (entry == null) {
            float[] Vf = initV();
            entry = new Entry(nextWi, Vf);
            _map.put(j, entry);
        } else {
            entry.W = nextWi;
        }
    }

    /**
     * @return V_x,yField,f
     */
    @Override
    public float getV(@Nonnull final Feature x, @Nonnull final String yField, final int f) {
        String j = StringFeature.getFeatureOfField(x, yField);

        final float[] V;
        Entry entry = _map.get(j);
        if (entry == null) {
            V = initV();
            entry = newEntry(V);
            _map.put(j, entry);
        } else {
            V = entry.Vf;
            assert (V != null);
        }
        return V[f];
    }

    @Override
    protected void setV(@Nonnull final Feature x, @Nonnull final String yField, final int f, final float nextVif) {
        String j = StringFeature.getFeatureOfField(x, yField);

        final float[] V;
        Entry entry = _map.get(j);
        if (entry == null) {
            V = initV();
            entry = new Entry(0.f, V);
            _map.put(j, entry);
        } else {
            V = entry.Vf;
            assert (V != null);
        }
        V[f] = nextVif;
    }

    @Override
    protected Entry getEntry(@Nonnull final Feature x, @Nonnull final String yField) {
        String j = StringFeature.getFeatureOfField(x, yField);
        return _map.get(j);
    }

}