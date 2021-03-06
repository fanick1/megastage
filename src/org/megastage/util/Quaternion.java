package org.megastage.util;

/**
 * ***********************************************************************
 * Compilation: javac Quaternion.java Execution: java Quaternion
 *
 * Data type for quaternions.
 *
 * http://mathworld.wolfram.com/Quaternion.html
 *
 * The data type is "immutable" so once you create and initialize a Quaternion,
 * you cannot change it.
 *
 * % java Quaternion
 *
 ************************************************************************
 */
public class Quaternion {

    public final double w, x, y, z;

    public Quaternion() {
        this.w = 1.0d;
        this.x = 0.0d;
        this.y = 0.0d;
        this.z = 0.0d;
    }

    // create a new object with the given components
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion(Vector3d v) {
        this.w = 0.0d;
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Quaternion(com.jme3.math.Quaternion q) {
        this.w = q.getW();
        this.x = q.getX();
        this.y = q.getY();
        this.z = q.getZ();
    }

    public Quaternion(Vector3d axis, double angle) {
        Vector3d v = axis.normalize();
        if (axis.x == 0 && axis.y == 0 && axis.z == 0) {
            this.w = 1.0d;
            this.x = 0.0d;
            this.y = 0.0d;
            this.z = 0.0d;
        } else {
            angle /= 2.0d;
            w = Math.cos(angle);

            double sin = Math.sin(angle);
            x = sin * axis.x;
            y = sin * axis.y;
            z = sin * axis.z;
        }
    }

    // return a string representation of the invoking object
    public String toString() {
        return w + " + " + x + "i + " + y + "j + " + z + "k";
    }

    public double getAngle() {
        return 2.0d * Math.acos(w);
    }

    // return the quaternion norm
    public double norm() {
        return Math.sqrt(w * w + x * x + y * y + z * z);
    }

    public Quaternion normalize(double tolerance) {
        double mag2 = w * w + x * x + y * y + z * z;
        if (Math.abs(mag2 - 1.0) > tolerance) {
            double mag = Math.sqrt(mag2);
            return new Quaternion(w / mag, x / mag, y / mag, z / mag);
        }
        return this;
    }

    public Quaternion normalize() {
        return normalize(0.00001d);
    }

    // return the quaternion conjugate
    public Quaternion conjugate() {
        //Quaternion q = normalize();
        Quaternion q = this;
        return new Quaternion(q.w, -q.x, -q.y, -q.z);
    }

    // return a new Quaternion whose value is (this + b)
    public Quaternion plus(Quaternion b) {
        Quaternion a = this;
        return new Quaternion(a.w + b.w, a.x + b.x, a.y + b.y, a.z + b.z);
    }

    // return a new Quaternion whose value is (this * b)
    public Quaternion multiply(Quaternion b) {
        Quaternion a = this;
        double w1 = a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z;
        double x1 = a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y;
        double y1 = a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x;
        double z1 = a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w;
        return new Quaternion(w1, x1, y1, z1);
    }

    // return a new Quaternion whose value is the inverse of this
    public Quaternion inverse() {
        double d = w * w + x * x + y * y + z * z;
        return new Quaternion(w / d, -x / d, -y / d, -z / d);
    }

    // return a / b
    public Quaternion divide(Quaternion b) {
        Quaternion a = this;
        return a.inverse().multiply(b);
    }

    public Quaternion localRotation(Vector3d axis, double radians_angle) {
        Vector3d globalAxis = axis.multiply(this);
        Quaternion rotation = new Quaternion(globalAxis, radians_angle);
        return rotation.multiply(this);
    }

    public double[] toAngles() {
        double[] angles = new double[3];

        double sqw = w * w;
        double sqx = x * x;
        double sqy = y * y;
        double sqz = z * z;
        double unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
        
        // is correction factor
        double test = x * y + z * w;
        if (test > 0.499 * unit) { // singularity at north pole
            angles[1] = 2 * Math.atan2(x, w);
            angles[2] = Math.PI / 2.0;
            angles[0] = 0;
        } else if (test < -0.499 * unit) { // singularity at south pole
            angles[1] = -2 * Math.atan2(x, w);
            angles[2] = -Math.PI / 2.0;
            angles[0] = 0;
        } else {
            angles[1] = Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading 
            angles[2] = Math.asin(2 * test / unit); // pitch or attitude
            angles[0] = Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
        }
        return angles;
    }
}
