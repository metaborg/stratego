import benchmark.stratego2.CompilationBenchmark;

public class Main {

    public static void main(String args[]) {
        System.out.println("Instantiating benchmark...");
        CompilationBenchmark b1 = new CompilationBenchmark();

        System.out.println("Setting up...");
        try {
            b1.setup();
        } catch (Exception e) {
            System.out.println("Exception during setup:");
            e.printStackTrace();
            return;
        }

        System.out.println("Compiling...");
        try {
            b1.compile();
        } catch (Exception e) {
            System.out.println("Exception during compilation:");
            e.printStackTrace();
            return;
        }

        try {
            b1.program.run();
        } catch (Exception e) {
            System.out.println("Exception during running:");
            e.printStackTrace();
            return;
        }

//        try {
//            b1.program.cleanup();
//        } catch (Exception e) {
//            System.out.println("Exception during cleanup:");
//            e.printStackTrace();
//            return;
//        }

        System.out.println("Finished successfully!");
    }
}
