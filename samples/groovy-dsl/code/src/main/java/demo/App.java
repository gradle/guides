package demo;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {  // <1>
        System.out.println(new App().getGreeting());
    }
}
