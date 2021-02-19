package mb.stratego.build.strincr.message;

public enum MessageSeverity {
    NOTE(0), WARNING(1), ERROR(2);

    public final int value;

    MessageSeverity(int value) {
        this.value = value;
    }
}
