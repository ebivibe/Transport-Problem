import java.util.Scanner;
public class Main{
    public static void main(String[] args) throws Exception{
        String filename;
        if(args.length==0){
            Scanner in = new Scanner(System.in);
            System.out.println("No file name entered. Please enter the filename: ");
            filename = in.nextLine();
        }
        else{
            filename = args[0];
        }

        FindOptimalTransport system = new FindOptimalTransport();
        system.findOptimalTransport(filename);
    }
}