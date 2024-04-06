package com.jones.tank.object;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Created by jones on 18-11-15.
 */
@Getter
@Data
@Accessors(chain = true)
public class BaseObject<T> {
    protected Long id;
}
