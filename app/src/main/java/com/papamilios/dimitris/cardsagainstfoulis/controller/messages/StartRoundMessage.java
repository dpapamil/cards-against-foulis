package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

import java.nio.charset.Charset;

public class StartRoundMessage extends GameMessage {

    private final static String msgSeparator = "@|@";

    // Member variables
    private String mBlackCardText;
    private String mCzarId;


    public StartRoundMessage(byte[] buf) {
        super(buf);
        String msgStr = new String(MessageUtils.getActualMsg(buf), Charset.defaultCharset());
        String [] strArr = msgStr.split(msgSeparator);
        mBlackCardText = strArr[0];
        mCzarId = strArr[1];
    }

    public static StartRoundMessage create(String nextCzarId, String blackCardText) {
        String msgStr = nextCzarId + msgSeparator + nextCzarId;
        byte[] msg = msgStr.getBytes(Charset.defaultCharset());
        // Prepend the message type to the actual message
        byte[] msgBuf = MessageUtils.prependType(MessageType.START_ROUND, msg);
        return new StartRoundMessage(msgBuf);
    }

    public String blackCardText() {
        return mBlackCardText;
    }

    public String czarId() {
        return mCzarId;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
