package com.sk89q.worldedit.math.transform;

import com.sk89q.worldedit.math.Vector3;

public interface Transform {
    boolean isIdentity();
    Vector3 apply(Vector3 input);
}
