public class HashingDemo {
    public static void main(String[] args) {
        String text = "Hello, World!";
        int hash = text.hashCode();

        System.out.println("Hash of " + text + " is " + hash);
        System.out.println("\"Aa\" hashCode = " + "Aa".hashCode());
        System.out.println("\"BB\" hashCode = " + "BB".hashCode());
    }
}
