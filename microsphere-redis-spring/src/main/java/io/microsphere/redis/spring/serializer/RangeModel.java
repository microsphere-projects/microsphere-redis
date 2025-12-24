/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.redis.spring.serializer;

import io.microsphere.annotation.Nullable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.redis.connection.RedisZSetCommands;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static org.springframework.data.domain.Range.Bound.exclusive;
import static org.springframework.data.domain.Range.Bound.inclusive;
import static org.springframework.data.domain.Range.Bound.unbounded;
import static org.springframework.data.domain.Range.of;

/**
 * The model class to adapter {@link org.springframework.data.redis.connection.RedisZSetCommands.Range} or {@link Range}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Range
 * @see org.springframework.data.redis.connection.RedisZSetCommands.Range
 * @since 1.0.0
 */
public class RangeModel implements Externalizable {

    @Nullable
    Object lowerValue;

    boolean lowerIncluding;

    @Nullable
    Object upperValue;

    boolean upperIncluding;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(lowerValue);
        out.writeBoolean(lowerIncluding);
        out.writeObject(upperValue);
        out.writeBoolean(upperIncluding);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.lowerValue = in.readObject();
        this.lowerIncluding = in.readBoolean();
        this.upperValue = in.readObject();
        this.upperIncluding = in.readBoolean();
    }

    Range toRange() {
        Comparable lowerBoundValue = (Comparable) this.lowerValue;
        Comparable upperBoundValue = (Comparable) this.upperValue;
        boolean isLowerInclusive = this.lowerIncluding;
        boolean isUpperInclusive = this.upperIncluding;

        Bound lowerBound = lowerBoundValue == null ? unbounded() : isLowerInclusive ?
                inclusive(lowerBoundValue) : exclusive(lowerBoundValue);

        Bound upperBound = upperBoundValue == null ? unbounded() : isUpperInclusive ?
                inclusive(upperBoundValue) : exclusive(upperBoundValue);

        return of(lowerBound, upperBound);
    }

    static RangeModel from(Range range) {
        RangeModel rangeModel = new RangeModel();

        Bound lowerBound = range.getLowerBound();
        Bound upperBound = range.getUpperBound();
        Object lowerBoundValue = lowerBound.getValue().orElse(null);
        Object upperBoundValue = upperBound.getValue().orElse(null);
        boolean isLowerInclusive = lowerBound.isInclusive();
        boolean isUpperInclusive = upperBound.isInclusive();

        rangeModel.lowerValue = lowerBoundValue;
        rangeModel.lowerIncluding = isLowerInclusive;
        rangeModel.upperValue = upperBoundValue;
        rangeModel.upperIncluding = isUpperInclusive;

        return rangeModel;
    }

    static RangeModel from(RedisZSetCommands.Range range) {
        RangeModel rangeModel = new RangeModel();
        RedisZSetCommands.Range.Boundary min = range.getMin();
        RedisZSetCommands.Range.Boundary max = range.getMax();

        if (min != null) {
            rangeModel.lowerIncluding = min.isIncluding();
            rangeModel.lowerValue = min.getValue();
        }

        if (max != null) {
            rangeModel.upperIncluding = max.isIncluding();
            rangeModel.upperValue = max.getValue();
        }

        return rangeModel;
    }
}