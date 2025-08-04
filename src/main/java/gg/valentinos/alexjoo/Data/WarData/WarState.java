package gg.valentinos.alexjoo.Data.WarData;

public enum WarState {
    DECLARED,
    IN_PROGRESS,
    ENDED;

    public String getDisplayName() {
        return switch (this) {
            case DECLARED -> "Declared";
            case IN_PROGRESS -> "In Progress";
            case ENDED -> "Ended";
        };
    }
}
