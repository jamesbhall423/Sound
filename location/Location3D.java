package location;

public class Location3D implements SoundLocation<Location3D> {
    public double xSeconds;
    public double ySeconds;
    public double zSeconds;
    @Override
    public double soundTraversalTime(Location3D other) {
        return Math.hypot(zSeconds-other.zSeconds, Math.hypot(xSeconds-other.xSeconds, ySeconds-other.ySeconds));
    }
    public Location3D(double xSeconds, double ySeconds, double zSeconds) {
        this.xSeconds = xSeconds;
        this.ySeconds = ySeconds;
        this.zSeconds = zSeconds;
    }
}
