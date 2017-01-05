package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CapacityDataThread implements Runnable {
	private volatile Thread thread;
	private String dirName;
	private Process ps = null;
	private Controller c;

	public CapacityDataThread(Controller c) {
		this.c = c;
	}

	@Override
	public void run() {
		try {
			File path = new File("data/cmd/");
			if (!path.exists()) {
				path.mkdir();
			}
			FileOutputStream fos = new FileOutputStream(new File("data/cmd/" + dirName + ".txt"));
			String strcmd = "cd sdcard\nsh capacity.sh " + dirName;
			fos.write(strcmd.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (c.isUnix) {
				ps = Runtime.getRuntime().exec("adb shell < data/cmd/" + dirName + ".txt");
			}
			else {
				ps = Runtime.getRuntime().exec("cmd /c adb shell < data\\cmd\\" + dirName + ".txt");
			}
			ps.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("CapacityDataThread - successful interrupt");
			return;
		}
		System.out.println("CapacityDataThread - unexcepted finish");
	}

	public void start(String dirName) {
		if (thread == null) {
			dirName = dirName.replace(' ', '_');
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
			if (c.isUnix) {
				ps = Runtime.getRuntime().exec("adb shell kill -9 `cat /sdcard/CapacityData/toBeKilled.pid`");
			}
			else {
				ps = Runtime.getRuntime().exec("cmd /c adb shell kill -9 `cat /sdcard/CapacityData/toBeKilled.pid`");
			}
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
