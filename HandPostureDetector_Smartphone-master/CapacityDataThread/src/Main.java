import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		CapacityDataThread cthr = new CapacityDataThread();
		System.out.println("Begin");
		cthr.start("Task1");
		System.out.println("Pass");
		Scanner sc = new Scanner(System.in);
		sc.next();
		cthr.finish();
		System.out.println("Finish");
		
		sc = new Scanner(System.in);
		sc.next();
		System.out.println("Begin");
		cthr.start("Task2");
		System.out.println("Pass");
		sc = new Scanner(System.in);
		sc.next();
		cthr.finish();
		System.out.println("Finish");
	}
}
