package com.snowgears.chatterbox;

public enum ChatType {


    DEFAULT(0),
    
    WHISPER(1),

    YELL(2),

    WORLD(3),
    
    SERVER(4),
    
    GLOBAL(5),
    
    CHANNEL(6);
    
    private final int slot;

    private ChatType(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
