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
	private JButton send = new JButton("Ⱥ��");
                private JButton logout = new JButton("ǿ������");
    
	private void InitFrame(){
		this.getContentPane().setLayout(new BorderLayout());
		JPanel pf=new JPanel(new FlowLayout());
		JPanel p=new JPanel(new BorderLayout());
		p.add(pf,BorderLayout.NORTH);
		add(p,BorderLayout.NORTH);
		add(userList,BorderLayout.CENTER);
		JPanel p2=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.setTitle("��ӭʹ����Խ�����ķ������ˣ�");
		p2.add(send);		
		p2.add(logout);	
		
		add(p2,BorderLayout.SOUTH);	
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(300,600);
		this.setLocation(300, 200);
		setVisible(true);
	}
	private void addEvent(){
		//�¼���
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
									System.out.println(s.getRemoteSocketAddress());//���ش��׽������ӵĶ˵�ĵ�ַ
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
									String content = " "+msg.getFrom()+" "+"������";
									online++;
									msg.setContent(content);
									msg.setNames(names);
									dlm.removeAllElements();
									for(String s:names){
										dlm.addElement(s);
									}
									SendMessageToAll(msg);//��������
								}else if(type.equals(MessageType.Chat)){
										String content = msg.getFrom()+"\n "+ msg.getContent();
										msg.setContent(content);
										if(msg.getChatState().equals(ChatState.Personal)){//�û�˽��
											SendToPersonal(msg);
										}else{//����Ⱥ��
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
										String content = " "+msg.getFrom()+" "+"������";
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
                    //ͨ��������ת��
					private synchronized void SendMessageToAll(Message msg) throws Exception{//Ⱥ��
						for(UserInfo s : users){
								ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
								oos.writeObject(msg);
								oos.flush();
						}
					}
					
					private synchronized void SendToPersonal(Message msg)throws Exception{//˽��
						int count=0;
						for(UserInfo s:users){
							if(s.getName().equals(msg.getTo())||s.getName().equals(msg.getFrom())){
								ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
								oos.writeObject(msg);
								oos.flush();
								count++;//���������ͻ���
								if(count==2)break;
							}
						}
					}
					
					private synchronized void ServerMessage()throws Exception{
						String content = JOptionPane.showInputDialog("�����뷢���ͻ��˵���Ϣ��");
						System.out.println("������˵��"+ content);
/*						if(content == null || content.trim().equals("")){//ȥ������ո�
							JOptionPane.showMessageDialog(this, "����Ϊ�գ�");
						}*/
						//Message serverMessage=new Message(null, content, MessageType.Chat);
						Message msg = new Message("������", content, MessageType.Chat, ChatState.Group, null);
						for(UserInfo s:users){
							//serverMessage.setFrom(s.getName());
							ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
							oos.writeObject(msg);
							oos.flush();
						}
					}
					
					private void logout() throws Exception{
						logoutName = JOptionPane.showInputDialog("������Ҫǿ�����ߵ��ǳ�:");
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
				if(cmd.equals("Ⱥ��")) {
				if(ss!=null) {
						String content = JOptionPane.showInputDialog("������ҪȺ������Ϣ");
						Message msg = new Message("������", content, MessageType.Chat, ChatState.Group, null);
						for(UserInfo s:users){
							//serverMessage.setFrom(s.getName());
							ObjectOutputStream oos = new ObjectOutputStream(s.getSocket().getOutputStream());
							oos.writeObject(msg);
						oos.flush();
					}

				}
			}else if(cmd.equals("ǿ������")) {	
				stop();
			}
		}catch(Exception ex){
		}
	}
	private void stop() throws Exception{
		if(ss!=null)
		run=false;
		String content ="��������ֹ����!";
		String logout = JOptionPane.showInputDialog("������Ҫ���ߵ��û�");
		Message stopMsg = new Message("������",logout,MessageType.Logout,ChatState.Group,null);
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