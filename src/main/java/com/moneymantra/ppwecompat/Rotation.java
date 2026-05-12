package com.moneymantra.ppwecompat;

import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import net.minecraft.core.Direction;

import java.util.Locale;

/** Horizontal WorldEdit clipboard rotation around the Y axis. */
enum Rotation {
    NONE(0),
    CLOCKWISE_90(90),
    CLOCKWISE_180(180),
    CLOCKWISE_270(270);

    private final int degrees;

    Rotation(int degrees) {
        this.degrees = degrees;
    }

    int degrees() {
        return this.degrees;
    }

    boolean rotatesDirections() {
        return this != NONE;
    }

    Direction rotate(Direction direction) {
        if (direction == Direction.UP || direction == Direction.DOWN || this == NONE) {
            return direction;
        }
        return switch (this) {
            case CLOCKWISE_90 -> switch (direction) {
                case NORTH -> Direction.EAST;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.WEST;
                case WEST -> Direction.NORTH;
                default -> direction;
            };
            case CLOCKWISE_180 -> direction.getOpposite();
            case CLOCKWISE_270 -> switch (direction) {
                case NORTH -> Direction.WEST;
                case WEST -> Direction.SOUTH;
                case SOUTH -> Direction.EAST;
                case EAST -> Direction.NORTH;
                default -> direction;
            };
            case NONE -> direction;
        };
    }

    static Rotation fromClipboardTransform(Transform transform) {
        if (transform == null || transform.isIdentity()) {
            return NONE;
        }

        Direction transformedNorth = transformDirection(transform, Direction.NORTH);
        Direction transformedEast = transformDirection(transform, Direction.EAST);
        if (transformedNorth == null || transformedEast == null) {
            return NONE;
        }

        if (transformedNorth == Direction.NORTH && transformedEast == Direction.EAST) {
            return NONE;
        }
        if (transformedNorth == Direction.EAST && transformedEast == Direction.SOUTH) {
            return CLOCKWISE_90;
        }
        if (transformedNorth == Direction.SOUTH && transformedEast == Direction.WEST) {
            return CLOCKWISE_180;
        }
        if (transformedNorth == Direction.WEST && transformedEast == Direction.NORTH) {
            return CLOCKWISE_270;
        }
        return NONE;
    }

    private static Direction transformDirection(Transform transform, Direction direction) {
        Vector3 origin = transform.apply(Vector3.ZERO);
        Vector3 target = transform.apply(Vector3.at(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
        double dx = target.x() - origin.x();
        double dy = target.y() - origin.y();
        double dz = target.z() - origin.z();

        int rx = snap(dx);
        int ry = snap(dy);
        int rz = snap(dz);
        if (ry != 0 || Math.abs(rx) + Math.abs(rz) != 1) {
            return null;
        }

        for (Direction candidate : Direction.values()) {
            if (candidate.getStepX() == rx && candidate.getStepY() == ry && candidate.getStepZ() == rz) {
                return candidate;
            }
        }
        return null;
    }

    private static int snap(double value) {
        if (Math.abs(value - 1.0D) < 0.001D) {
            return 1;
        }
        if (Math.abs(value + 1.0D) < 0.001D) {
            return -1;
        }
        if (Math.abs(value) < 0.001D) {
            return 0;
        }
        return Integer.MIN_VALUE;
    }

    String label() {
        return this == NONE ? "none" : this.degrees + "°";
    }

    static Rotation fromDegrees(int degrees) {
        int normalized = Math.floorMod(degrees, 360);
        return switch (normalized) {
            case 90 -> CLOCKWISE_90;
            case 180 -> CLOCKWISE_180;
            case 270 -> CLOCKWISE_270;
            default -> NONE;
        };
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
