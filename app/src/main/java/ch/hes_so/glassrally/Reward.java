package ch.hes_so.glassrally;

public class Reward {

    private String mName; // The name of the checkpoint reached
    private String mContent; // The corresponding reward

    public Reward(String name, String content) {
        mName = name;
        mContent = content;
    }

    public String getName() {
        return mName;
    }

    public String getContent() {
        return mContent;
    }
}
