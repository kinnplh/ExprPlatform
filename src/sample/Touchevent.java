package sample;

import java.io.*;

/**
 * Created by kinnplh on 2017/1/5.
 */
class TouchInput implements Runnable {
  InputStream is;
  String type;
  File f;
  BufferedWriter out;
  TouchInput(InputStream is, String type, File f) {
    this.is = is;
    this.type = type;
    this.f = f;
  }

  public void run() {

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      String s = null;
      out = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(f.getAbsolutePath(), true)));
      while ((s = in.readLine()) != null) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        //System.out.println(s);
        out.write(s + '\n');
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

public class Touchevent implements Runnable {
  Thread threadEvent;
  Thread threadInput;
  Thread threadError;
  Controller c;
  File f;

  Touchevent(Controller c) {
    threadEvent = null;
    threadInput = null;
    threadError = null;
    this.c = c;
  }

  public void run() {
    try {
      Process process = Runtime.getRuntime().exec("adb shell getevent -lt");
      threadInput = new Thread(new TouchInput(process.getInputStream(), "Info", f));
      threadError = new Thread(new TouchInput(process.getErrorStream(), "Error", f));
      threadInput.start();
      threadError.start();
      int value = process.waitFor();
      // adb disconnect
      c.showDraw("adb连接已断开！");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      // interrupted
    } finally {
      if (threadInput != null) {
        threadInput.interrupt();
        threadInput = null;
      }
      if (threadError != null) {
        threadError.interrupt();
        threadError = null;
      }
    }
  }

  public void start() {
    if (threadEvent == null) {
      f = new File("./TouchData/" + "Task_" +
              c.stageList.get(c.crtStage).tag +
              "_" + System.currentTimeMillis() + ".txt");
      try {
        f.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }

      threadEvent = new Thread(this, "Touchevent");
      threadEvent.start();
    }
  }

  public void stop() {
    if (threadEvent != null) {
      threadEvent.interrupt();
      threadEvent = null;
    }
  }
}