package gg.valentinos.alexjoo.Data;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.VClans;

public class ConfirmationEntry {

    private CommandAction commandAction;
    private long timestamp;
    private long duration;

    public ConfirmationEntry(CommandAction commandAction, long duration){
        this.commandAction = commandAction;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
    }

    public void execute(){
        if (!isExpired())
            commandAction.execute();
    }

    private boolean isExpired(){
        return System.currentTimeMillis() - timestamp < duration;
    }


}
