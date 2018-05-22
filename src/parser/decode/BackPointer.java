package decode;

public class BackPointer {
    public int getK() {
        return k;
    }

    public String getNonT1() {
        return nonT1;
    }

    public String getNonT2() {
        return nonT2;
    }

    private int k;
    private String nonT1;
    private String nonT2;

    public BackPointer(int k, String nonT1, String nonT2) {
        this.k = k;
        this.nonT1 = nonT1;
        this.nonT2 = nonT2;

    }
}