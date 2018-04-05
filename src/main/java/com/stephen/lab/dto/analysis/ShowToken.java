package com.stephen.lab.dto.analysis;

/**
 * Created by stephen on 2018/1/6.
 */
public class ShowToken extends Token {
    private boolean isNew;

    public ShowToken(Token token, boolean isNew) {
        setWeight(token.getWeight());
        setWord(token.getWord());
        setNew(isNew);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShowToken) {
            if (obj != null) {
                if (((Token) obj).getWord().equals(getWord())) {
                    return true;
                }
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getWord().hashCode();
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
