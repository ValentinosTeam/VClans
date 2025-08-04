package gg.valentinos.alexjoo.Data.WarData;

import java.util.HashSet;

public class Wars {

    private HashSet<War> wars;

    public Wars() {
        this.wars = new HashSet<>();
    }

    public HashSet<War> getWars() {
        return wars;
    }
    public void setWars(HashSet<War> wars) {
        this.wars = wars;
    }
}
