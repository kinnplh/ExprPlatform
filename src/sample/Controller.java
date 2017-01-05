package sample;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
	static String pathRoot;
	public final static String preInst = "您好，感谢您参与本次实验！\n" +
			"您的参与将会帮助我们进一步提升移动触屏设备的用户体验。\n" +
			"请您填写您的个人相关信息：\n" +
			"\n" +
			"在实验中，请先阅读屏幕上的提示，准备好时请点击“开始当前任务”\n" +
			"完成当前任务后，点击“下一步”，跳转到下个任务页面\n" +
			"操作过程中有任何问题，都请向您身旁的工作人员提出，我们需要您的反馈\n" +
			"\n" +
			"现在就点击“下一步”，跳转到第一个任务页面，开始实验吧！";

	public int PREPARING = 0;
	public int PROCESSING = 1;
	Touchevent touchevent = new Touchevent(this);
	CapacityDataThread cdt = new CapacityDataThread(this);
	public int LONGISLAND = 0;
	public int LITTLEV = 1;
	public int typePhone = LITTLEV;
	public boolean isUnix = true;

	@FXML
	public AnchorPane anchorPane;
	@FXML
	public Label inst;
	@FXML
	public JFXToggleButton phoneType;
	@FXML
	public JFXButton reTry;
	@FXML
	public JFXButton startStage;
	@FXML
	public JFXButton nextStage;
	@FXML
	public JFXButton exprChooser;
	@FXML
	public JFXButton connectNet;
	@FXML
	public JFXButton disconnectNet;
	@FXML
	public JFXButton startExpr;
	@FXML
	public Label IPLabel;
	@FXML
	public JFXTextField portInput;
	@FXML
	public JFXComboBox<String> clientIps;

	NetThread netThread;
	DataOutputStream out;
	BufferedReader in;

	int crtStage = -1;
	int crtState = PREPARING;
	List<Stage> stageList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		IPLabel.setText(getLocalHostIP());
		inst.setText(preInst);
		connectNet.setOnMouseClicked(event -> {
			try {
				netThread = new NetThread(Integer.valueOf(portInput.getText()), this);
				netThread.start();
				connectNet.setDisable(true);
				startExpr.setDisable(false);
				portInput.setDisable(true);
			} catch (IOException e) {
				showDraw("端口已被绑定！");
				e.printStackTrace();
			}
		});

		phoneType.setOnMouseClicked(event -> {
			System.out.println("PhoneType Clicked");
			if (phoneType.isSelected()) {
				typePhone = LITTLEV;
			}
      		else {
				typePhone = LONGISLAND;
			}
		});

		startExpr.setOnMouseClicked(event -> {
			boolean success = false;
			if ((clientIps == null) || (clientIps.getValue() == null) || (clientIps.getValue().length() == 0)) {
				showDraw("未选择设备！");
				return;
			}
			if (stageList.size() == 0) {
				showDraw("未选择实验！");
				return;
			}
			System.out.println(clientIps.getValue());
			try {
				out = new DataOutputStream(netThread.ipToSocket.get(clientIps.getValue()).getOutputStream());
				in = new BufferedReader(new InputStreamReader(netThread.ipToSocket.get(clientIps.getValue()).getInputStream()));
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (success) {
				System.out.println("connect to the phone successfully");
				pathRoot = String.format("data/ExprData/%d.txt", System.currentTimeMillis());
				File f = new File(pathRoot);
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				startExpr.setDisable(true);
				nextStage.setDisable(false);
				exprChooser.setDisable(true);
				inst.setText(preInst);
				clientIps.setDisable(true);
				phoneType.setDisable(true);
			}
		});

		startStage.setOnMouseClicked(event -> {
			startStage.setDisable(true);
			nextStage.setDisable(false);
			reTry.setDisable(false);

			startSensor();
			startCapacity();
			startEvent();
		});

		nextStage.setOnMouseClicked(event -> {
			if (crtStage >= 0) {
				endSensor();
				endCapacity();
				endEvent();
			}
			crtStage += 1;
			reTry.setDisable(true);
			if (crtStage >= stageList.size()) {
				crtStage = -1;
				inst.setText("实验结束，感谢您的配合！");
				crtState = PREPARING;
				startExpr.setDisable(false);
				nextStage.setDisable(true);
				exprChooser.setDisable(false);
				phoneType.setDisable(false);
				clientIps.setDisable(false);
			}
			else {
				inst.setText(stageList.get(crtStage).inst);
				nextStage.setDisable(true);
				startStage.setDisable(false);
			}
		});

		exprChooser.setOnMouseClicked(event -> {
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("选择实验序列文件");
			File f = fileChooser.showOpenDialog(startStage.getScene().getWindow());
			if (f != null) {
				stageList = null;
				stageList = new ArrayList<>();
				try {
					Scanner scanner = new Scanner(f);
					while (scanner.hasNextLine()) {
						String newStageInfo = scanner.nextLine();
						System.out.print(newStageInfo + "\n=====\n");
						String[] res = newStageInfo.split("\\|");
						for(int i = 0; i < res.length; ++i) {
							System.out.println(res[i]);
						}
						if(res.length <= 1) {
							continue;
						}
						res[0] = res[0].replace('$', '\n');
						stageList.add(new Stage(res[0], res[1]));
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		reTry.setOnMouseClicked(event -> {
			endEvent();
			endSensor();
			endCapacity();

			startEvent();
			startSensor();
			startCapacity();
		});

		stageList = new ArrayList<>();
		disconnectNet.setDisable(true);
		startExpr.setDisable(true);
		startStage.setDisable(true);
		nextStage.setDisable(true);
		reTry.setDisable(true);
		phoneType.setSelected(true);
		String osName = System.getProperties().getProperty("os.name");
		if (osName.startsWith("Windows")) {
			System.out.println("System: Windows");
			isUnix = false;
		}
		else {
			System.out.println("System: Unix");
			isUnix = true;
		}
	}

	public static String getLocalHostIP() {
		String ip;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		} catch (Exception e) {
			ip = "";
		}
		return ip;
	}

	public void addWaitingClient(String str){
		clientIps.getItems().add(str);
	}

	void startCapacity()	{
		if (typePhone == LITTLEV) {
			try {
				Runtime.getRuntime().exec("adb shell setenforce 0");
				Process p = Runtime.getRuntime().exec("adb shell aptouch_daemon_debug logtofile on");
				BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String stdOut = stdout.readLine();
				System.out.println(stdOut);
				if ((stdOut == null) || (!stdOut.equals("start logtofile success."))) {
					showDraw("电容数据采集开始异常！");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (typePhone == LONGISLAND) {
			cdt.start("Task_" + stageList.get(crtStage).tag + "_" + System.currentTimeMillis());
		}
		else {
			System.out.println("Unknown phone type");
		}
	}

	void endCapacity() {
		if (typePhone == LITTLEV) {
			try {
				Process p = Runtime.getRuntime().exec("adb shell aptouch_daemon_debug logtofile off");
				BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String stdOut = stdout.readLine();
				System.out.println(stdOut);
				if (!stdOut.equals("APTOUCH_SET_LOGTOFILE_MODE excuted done")) {
					showDraw("电容数据采集结束异常！");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (typePhone == LONGISLAND) {
			cdt.finish();
		}
		else {
			System.out.println("Unknown phone type");
		}
	}

	void startEvent() {
		touchevent.start();
	}

	void endEvent() {
		touchevent.stop();
	}

	void startSensor() {
		try {
			out.flush();
			out.writeUTF("Task_" + stageList.get(crtStage).tag.replace(' ', '_') + "_" + System.currentTimeMillis() + '\n');
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void endSensor() {
		try {
			out.flush();
			out.writeUTF("end\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void showDraw (String info) {
		JFXPopup popup = new JFXPopup();
		AnchorPane pane = new AnchorPane();
		Label infoL = new Label(String.format(info));
		infoL.setTextFill(Color.web("#eeeeee"));
		infoL.setLayoutX(80);
		infoL.setLayoutY(40);
		pane.getChildren().add(infoL);
		pane.setPrefWidth(200);
		pane.setPrefHeight(100);
		pane.setStyle("-fx-background-color: #202023");
//		pane.setStyle("-fx-opacity: 0.4");
		pane.setEffect(new DropShadow(2d, 0d, +2d, Color.BLACK));
		popup.setContent(pane);

		popup.setPopupContainer(anchorPane);
		popup.setSource(nextStage);
		Platform.runLater(() -> popup.show(JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.LEFT, 100, -20));
	}
}

class NetThread extends Thread {
	Controller c;
	ServerSocket servSock;
	Map<String, Socket> ipToSocket;

	public NetThread(int portNum, Controller controller) throws IOException {
		super();
		c = controller;
		ipToSocket = new HashMap<>();
		servSock = new ServerSocket(portNum);
	}

	public void run() {
		for(;;) {
			try {
				Socket clntSock = servSock.accept();
				System.out.println("New connect!");
				int lastSize = ipToSocket.size();
				ipToSocket.put(clntSock.getInetAddress().getHostAddress(), clntSock);
				if (lastSize != ipToSocket.size()) {
					Platform.runLater(() -> c.addWaitingClient(clntSock.getInetAddress().getHostAddress()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class Stage {
	final static int START = 0;
	final static int STOP = 1;
	final static int CONTINUE = 2;
	final static int END = 3;

	String inst;
	String tag;

	public Stage(String _inst, String _tag) {
		inst = _inst;
		tag = _tag;
	}

	public void tagToFile(int state) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Controller.pathRoot, true)));
			out.write(String.format("%d %s ", System.currentTimeMillis(), tag));
			switch (state) {
				case START:
					out.write("start\n");
					break;
				case STOP:
					out.write("stop\n");
					break;
				case CONTINUE:
					out.write("continue\n");
					break;
				case END:
					out.write("end\n");
					break;
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
