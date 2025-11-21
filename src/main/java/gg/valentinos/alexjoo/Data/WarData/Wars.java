package gg.valentinos.alexjoo.Data.WarData;

import java.util.HashSet;

public class Wars {

    private HashSet<War> wars;

    public Wars() {
        this.wars = new HashSet<>();
    }

    private void pruneWars() {
        // No need to keep ended wars
        wars.removeIf(war -> war.getState() == WarState.ENDED);
    }

    public HashSet<War> getWars() {
        pruneWars();
        return wars;
    }
    public void setWars(HashSet<War> wars) {
        this.wars = wars;
    }
}
