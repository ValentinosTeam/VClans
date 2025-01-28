package gg.valentinos.alexjoo.Data;

import gg.valentinos.alexjoo.Commands.CommandAction;

public class ConfirmationEntry {

    private final CommandAction commandAction;
    private final long timestamp;
    private final long duration;

    public ConfirmationEntry(CommandAction commandAction, long duration) {
        this.commandAction = commandAction;
        this.duration = duration * 1000;
        this.timestamp = System.currentTimeMillis();
    }

    public void execute(){
        commandAction.execute();
    }

    public boolean isExpired(){
        return System.currentTimeMillis() - timestamp > duration;
    }


}
