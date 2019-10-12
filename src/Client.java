import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Client extends JFrame implements ActionListener {
	JMenuBar bar = new JMenuBar();
	JMenu m1 = new JMenu("选项");
	JMenuItem disServer = new JMenuItem("断开服务器");
    JMenuItem exit = new JMenuItem("退出");
    JPanel north = new JPanel();
    JPanel west = new JPanel();
    DefaultListModel dlm = new DefaultListModel();//DefaultListModel保存由JList显示的数据。JList管理显示，DefaultListModel管理数据。
    JList userList = new JList(dlm);
    JScrollPane listPane = new JScrollPane(userList);
    JPanel center = new JPanel();
    JTextArea showMsg = new JTextArea(10,20);
    JScrollPane showPane = new JScrollPane(showMsg);
    JPanel operPane = new JPanel();
    JTextField msgInput = new JTextField(28);
    JButton send = new JButton("发送");
    
    static Socket socket;
    static String name;
    static ObjectOutputStream oos;
    static List<PersonalFrame> personalFrames = new ArrayList<PersonalFrame>();
	
    public Client() throws Exception{
		init();
		addEvent();
		initFrame();
		ConnectServer();
	}
    
	public void init(){
		m1.add(disServer);
		m1.addSeparator();
		bar.add(m1);
		
		BorderLayout bl = new BorderLayout();
		north.setLayout(bl);
		north.add(bar,BorderLayout.NORTH);
		add(north,BorderLayout.NORTH);
		
		Dimension dim = new Dimension(120,150);
		west.setPreferredSize(dim);
		Dimension dim2 = new Dimension(200,300);
		listPane.setPreferredSize(dim2);
		BorderLayout bl2 = new BorderLayout();
		west.setLayout(bl2);
		
		west.add(listPane,BorderLayout.CENTER);
		add(west,BorderLayout.EAST);
		showMsg.setEditable(true);
		msgInput.setActionCommand("enterKey");
		showMsg.setEditable(true);
		BorderLayout bl3 = new BorderLayout();
		center.setLayout(bl3);
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		operPane.setLayout(fl);
		operPane.add(msgInput);
		operPane.add(send);
		center.add(showPane,BorderLayout.CENTER);
		center.add(operPane,BorderLayout.SOUTH);
		add(center,BorderLayout.CENTER);
		
	}
	public void addEvent(){
		//事件的绑定
		send.addActionListener(this);
		msgInput.addActionListener(this);
		disServer.addActionListener(this);
		SymMouse picker= new SymMouse();
		userList.addMouseListener(picker);
	}
	public void initFrame(){
		setResizable(true);
		setTitle("余越开发的客户端");
		setSize(500,400);
		setLocation(200,50);
		setVisible(true);
	}
	
	public void dispose(){
		try {
			ExitProgram();
		} catch (Exception e) {
		}
	}
	public void SendMessage()throws Exception{
		//判断是否连接了服务器
		if(socket == null){
			JOptionPane.showMessageDialog(this, "没有连接服务器或已经断开服务器");
			return;
		}
		//发送消息
		String content = msgInput.getText();
		if(content == null || content.trim().equals("")){//去掉多余空格
			JOptionPane.showMessageDialog(this, "发送为空！");
		}else{
			//清空文本输入框
			msgInput.setText("");
		}
		Message msg = new Message(name, content, MessageType.Chat, ChatState.Group, null);//
			oos.writeObject(msg);
			oos.flush();
	}
	public void ConnectServer()throws Exception{
		int cnt = 0;
		do{
			String ip = "127.0.0.1";
			socket = new Socket("127.0.0.1",9999);
			
			name = JOptionPane.showInputDialog("请输入您的昵称:");
			if(name==null) {
				JOptionPane.showMessageDialog(this, "请重新输入");
			}
			
			OutputStream os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
			Message msg = new Message(name,null,MessageType.Login);//登录信息打包
			oos.writeObject(msg);
			oos.flush();
		}while(socket == null);
		
		//成功创建连接后启动一个新线程接收消息
		new Thread(){
			@Override
			public void run() {
				try {
					boolean flag=true;
					while(flag){
						InputStream is = socket.getInputStream();
						ObjectInputStream ois = new ObjectInputStream(is);
						Message msg = (Message)(ois.readObject());
						MessageType type = msg.getMsgType();//从 ObjectInputStream 读取对象
						if(type.equals(MessageType.Login)){//
							String content = msg.getContent();
							String old = showMsg.getText();//
							if(old == null || old.trim().equals("")){
								showMsg.setText(content);
							}else{
								String temp = old+"\n"+content;//
								showMsg.setText(temp);
							}
							showMsg.setCaretPosition(showMsg.getText().length());//设置移动光标位置
							if(dlm.size() == 0){
								List<String> names = msg.getNames();
								for(String s : names){
									if(!s.equals(name)){//没有该用户
										dlm.addElement(s);
										System.out.println("用户"+s+"上线");
									}
								}
							}else{
								if(!msg.getFrom().equals(name))
									dlm.addElement(msg.getFrom());
							}
						}else if(type.equals(MessageType.Chat)){
							if(msg.getChatState().equals(ChatState.Personal)){
								boolean FindIt=false;
								for(PersonalFrame pf:personalFrames){
									if(pf.getName().equals(msg.getFrom())||pf.getName().equals(msg.getTo())){//								
										pf.showMsg(msg);
										FindIt=true;
										break;
									}									
								}
								if(FindIt==false){
									PersonalFrame myFrame=new PersonalFrame(name, msg.getFrom());//新建私聊
									personalFrames.add(myFrame);
									myFrame.showMsg(msg);
								}
							}else{
									String content = msg.getContent();
									String old = showMsg.getText();
									if(old == null || old.trim().equals("")){
										showMsg.setText(content);
									}else{
										String temp = old+"\n"+content;
										showMsg.setText(temp);
									}
									showMsg.setCaretPosition(showMsg.getText().length());
							}
						}else if(type.equals(MessageType.Logout)){//用户主动下线
							String content = msg.getContent();
							String old = showMsg.getText();
							if(old == null || old.trim().equals("")){
								showMsg.setText(content);
							}else{
								String temp = old+"\n："+content;
								showMsg.setText(temp);
							}
							showMsg.setCaretPosition(showMsg.getText().length());
							if(!msg.getFrom().equals(name))
								dlm.removeElement(msg.getFrom());
							if(msg.getFrom().equals(name))
								flag=false;
						}else if(type.equals(MessageType.Logout)) {//接收强制下线
							String logoutName = msg.getContent();
							List<String> names = msg.getNames();
							for(String s : names){
								if(s.equals(name)){//要关闭该客户端
								                this.dispose(this);
									System.exit(0);
									dlm.removeElement(s);
								}else {
								
							    }
							}
						}
					}
				} catch (Exception e) {
				}
			}
		}.start();
		setTitle(name+"的客户端");
	}
	public void BreakServer()throws Exception{//断开连接
		if(socket!=null){
			Message msg=new Message(name, null, MessageType.Logout);//
			oos.writeObject(msg);
			oos.flush();
			dlm.removeAllElements();
			socket=null;
		}
	}
	public void ExitProgram()throws Exception{//
		int flag;
		if(socket!=null){
			flag = JOptionPane.showConfirmDialog(this, "确定要退出吗？");
		}else{
			flag=0;
		}
		if(flag == 0){
			if(socket!=null){
				Message msg=new Message(name, null, MessageType.Logout);
				oos.writeObject(msg);
				oos.flush();
				socket=null;
				dlm.removeAllElements();
			}
			System.exit(0);
		}
	}
	public void actionPerformed(ActionEvent e) {
		try{
		String comm = e.getActionCommand();
		System.out.println(comm);
		if(comm.equals("发送") || comm.equals("enterKey")||comm.equals("sendPersonalMessage")){
			SendMessage();
		}else if(comm.equals("断开服务器")){
			BreakServer();
		}else{
			
		}
		}catch(Exception e1){
			//不作处理
		}
	}
	class SymMouse extends MouseAdapter 
    { 
        public   void   mouseClicked(java.awt.event.MouseEvent e){ 
            Object object = e.getSource(); 
            if(object==userList) 
                userList_mouseClicked(e); 
        } 
    } 

  void userList_mouseClicked(java.awt.event.MouseEvent event) 
    { 
	    if(event.getClickCount()==2)
        {
        	PersonalFrame myFrame=new PersonalFrame(name,userList.getSelectedValue().toString());//新建私聊
        	personalFrames.add(myFrame);
        } 
    } 
	public static void main(String[] args)throws Exception {
		new Client();
	}
}
