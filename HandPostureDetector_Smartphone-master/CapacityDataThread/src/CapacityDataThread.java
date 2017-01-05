import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CapacityDataThread implements Runnable {
	
	private volatile Thread thread;
	private String dirName;
	private Process ps = null;
	
	@Override
	public void run() {
		try {
			try {
				File path = new File("cmd/");
				if (!path.exists())
					path.mkdir();
				FileOutputStream fos = new FileOutputStream(new File("cmd/" + dirName + ".txt"));
				String strcmd = "cd sdcard\nsh capacity.sh " + dirName;
				fos.write(strcmd.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				ps = Runtime.getRuntime().exec("cmd /c adb shell < cmd\\" + dirName + ".txt");
				ps.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			System.out.println("CapacityDataThread - successful interrupt");
			return;
		}
		System.out.println("CapacityDataThread - unexcepted finish");
	}
	
	public void start(String dirName) {
		if (thread == null) {
			this.dirName = dirName;
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void finish() {
		ps.destroy();
		thread.interrupt();
		thread = null;
		try {
			ps = Runtime.getRuntime().exec("cmd /c adb shell kill -9 `cat /sdcard/CapacityData/toBeKilled.pid`");
			ps.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ps.destroy();
		ps = null;
	}

}
