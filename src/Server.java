import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;


public class Server extends JFrame implements ActionListener {
	ServerSocket ss = null;
	List<UserInfo> users=new ArrayList<UserInfo>();
	List<String> names = new ArrayList<String>();
	static int count=0;
	private boolean run=true;
	private String logoutName = null;
	private int online = 0;
	private DefaultListModel dlm = new DefaultListModel();
	private JList userList=new JList(dlm);
	private JButton send = new JButton("群发");
                private JButton logout = new JButton("强制下线");
    
	private void InitFrame(){
		this.getContentPane().setLayout(new BorderLayout());
		JPanel pf=new JPanel(new FlowLayout());
		JPanel p=new JPanel(new BorderLayout());
		p.add(pf,BorderLayout.NORTH);
		add(p,BorderLayout.NORTH);
		add(userList,BorderLayout.CENTER);
		JPanel p2=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.setTitle("欢迎使用余越开发的服务器端！");
		p2.add(send);		
		p2.add(logout);	
		
		add(p2,BorderLayout.SOUTH);	
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(300,600);
		this.setLocation(300, 200);
		setVisible(true);
	}
	private void addEvent(){
		//事件绑定
		send.addActionListener(this);
		logout.addActionListener(this);
	}
	public Server() throws Exception{
		InitFrame();
		addEvent();
		go();
	}
	
	public void dispose(){
		try {
			stop();
		} catch (Exception e) {
		}
		System.exit(0);
	}
	private  void go() throws Exception  {
		ss = new ServerSocket(9999);
		while(run){
				Socket socket = ss.accept();
				final Socket s = socket;
				
				new Thread(){
					UserInfo user=null;
					InputStream is=null;
					ObjectInputStream ois=null;
					@Override
					public void run() {
						try {
							boolean flag=true;
							is = s.getInputStream();
							ois = new ObjectInputStream(is);
							while(flag){
								Message msg = (Message)(ois.readObject());
								if(user==null){
									user=new UserInfo(s, msg.getFrom());
									System.out.println(s.getRemoteSocketAddress());//返回此套接字连接的端点的地址
								}
								synchronized (this){//
									if(!users.contains(user)){
										users.add(user);
										if(!names.contains(user.getName())){
											names.add(user.getName());
										}
									}
								}
								MessageType type = msg.getMsgType();
								if(type.equals(MessageType.Login)){
									//
									String content = " "+msg.getFrom()+" "+"已上线";
									online++;
									msg.setContent(content);
									msg.setNames(names);
									dlm.removeAllElements();
									for(String s:names){
										dlm.addElement(s);
									}
									SendMessageToAll(msg);//给所有人
								}else if(type.equals(MessageType.Chat)){
										String content = msg.getFrom()+"\n "+ msg.getContent();
										msg.setContent(content);
										if(msg.getChatState().equals(ChatState.Personal)){//用户私聊
											SendToPersonal(msg);
										}else{//否则群发
											SendMessageToAll(msg);
										}
								}else if(type.equals(MessageType.Logout)){
									synchronized (this) {
										names.remove(user.getName());
										users.remove(user);
										dlm.removeAllElements();
										for(String s:names){
											dlm.addElement(s);
										}
										String content = " "+msg.getFrom()+" "+"已下线";
										msg.setContent(content);
										msg.setNames(names);
										SendMessageToAll(msg);
										flag=false;
									}					
								}
							}
							
						} catch (Exception e) {
						}
					
					}
                    //通过服务器转发
					private synchronized void SendMessageToAll(Message msg) throws Exception{//群发
						for(UserInfo s : users){
								ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
								oos.writeObject(msg);
								oos.flush();
						}
					}
					
					private synchronized void SendToPersonal(Message msg)throws Exception{//私聊
						int count=0;
						for(UserInfo s:users){
							if(s.getName().equals(msg.getTo())||s.getName().equals(msg.getFrom())){
								ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
								oos.writeObject(msg);
								oos.flush();
								count++;//发给两个客户端
								if(count==2)break;
							}
						}
					}
					
					private synchronized void ServerMessage()throws Exception{
						String content = JOptionPane.showInputDialog("请输入发给客户端的信息：");
						System.out.println("服务器说："+ content);
/*						if(content == null || content.trim().equals("")){//去掉多余空格
							JOptionPane.showMessageDialog(this, "发送为空！");
						}*/
						//Message serverMessage=new Message(null, content, MessageType.Chat);
						Message msg = new Message("服务器", content, MessageType.Chat, ChatState.Group, null);
						for(UserInfo s:users){
							//serverMessage.setFrom(s.getName());
							ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
							oos.writeObject(msg);
							oos.flush();
						}
					}
					
					private void logout() throws Exception{
						logoutName = JOptionPane.showInputDialog("请输入要强制下线的昵称:");
						Message logout = new Message(null,logoutName,MessageType.Logout);
						for(UserInfo s:users){
							logout.setFrom(s.getName());
							ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
							oos.writeObject(logout);
							oos.flush();
						}
					}
					
				}.start();
		}
	}

	public void actionPerformed(ActionEvent e) {
		try{
			String cmd=e.getActionCommand();
				if(cmd.equals("群发")) {
				if(ss!=null) {
						String content = JOptionPane.showInputDialog("请输入要群发的信息");
						Message msg = new Message("服务器", content, MessageType.Chat, ChatState.Group, null);
						for(UserInfo s:users){
							//serverMessage.setFrom(s.getName());
							ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
							oos.writeObject(msg);
						oos.flush();
					}

				}
			}else if(cmd.equals("强制下线")) {	
				stop();
			}
		}catch(Exception ex){
		}
	}
	private void stop() throws Exception{
		if(ss!=null)
		run=false;
		String content ="服务器终止服务!";
		String logout = JOptionPane.showInputDialog("请输入要下线的用户");
		Message stopMsg = new Message("服务器",logout,MessageType.Logout,ChatState.Group,null);
		for(UserInfo s:users){
				stopMsg.setFrom(s.getName());
				ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
				oos.writeObject(stopMsg);
				oos.flush();
		}
	}

	public static void main(String[] args) throws Exception {
		new Server();
	}
}