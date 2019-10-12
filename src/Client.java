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
	JMenu m1 = new JMenu("ѡ��");
	JMenuItem disServer = new JMenuItem("�Ͽ�������");
    JMenuItem exit = new JMenuItem("�˳�");
    JPanel north = new JPanel();
    JPanel west = new JPanel();
    DefaultListModel dlm = new DefaultListModel();//DefaultListModel������JList��ʾ�����ݡ�JList������ʾ��DefaultListModel�������ݡ�
    JList userList = new JList(dlm);
    JScrollPane listPane = new JScrollPane(userList);
    JPanel center = new JPanel();
    JTextArea showMsg = new JTextArea(10,20);
    JScrollPane showPane = new JScrollPane(showMsg);
    JPanel operPane = new JPanel();
    JTextField msgInput = new JTextField(28);
    JButton send = new JButton("����");
    
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
		//�¼��İ�
		send.addActionListener(this);
		msgInput.addActionListener(this);
		disServer.addActionListener(this);
		SymMouse picker= new SymMouse();
		userList.addMouseListener(picker);
	}
	public void initFrame(){
		setResizable(true);
		setTitle("��Խ�����Ŀͻ���");
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
		//�ж��Ƿ������˷�����
		if(socket == null){
			JOptionPane.showMessageDialog(this, "û�����ӷ��������Ѿ��Ͽ�������");
			return;
		}
		//������Ϣ
		String content = msgInput.getText();
		if(content == null || content.trim().equals("")){//ȥ������ո�
			JOptionPane.showMessageDialog(this, "����Ϊ�գ�");
		}else{
			//����ı������
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
			
			name = JOptionPane.showInputDialog("�����������ǳ�:");
			if(name==null) {
				JOptionPane.showMessageDialog(this, "����������");
			}
			
			OutputStream os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
			Message msg = new Message(name,null,MessageType.Login);//��¼��Ϣ���
			oos.writeObject(msg);
			oos.flush();
		}while(socket == null);
		
		//�ɹ��������Ӻ�����һ�����߳̽�����Ϣ
		new Thread(){
			@Override
			public void run() {
				try {
					boolean flag=true;
					while(flag){
						InputStream is = socket.getInputStream();
						ObjectInputStream ois = new ObjectInputStream(is);
						Message msg = (Message)(ois.readObject());
						MessageType type = msg.getMsgType();//�� ObjectInputStream ��ȡ����
						if(type.equals(MessageType.Login)){//
							String content = msg.getContent();
							String old = showMsg.getText();//
							if(old == null || old.trim().equals("")){
								showMsg.setText(content);
							}else{
								String temp = old+"\n"+content;//
								showMsg.setText(temp);
							}
							showMsg.setCaretPosition(showMsg.getText().length());//�����ƶ����λ��
							if(dlm.size() == 0){
								List<String> names = msg.getNames();
								for(String s : names){
									if(!s.equals(name)){//û�и��û�
										dlm.addElement(s);
										System.out.println("�û�"+s+"����");
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
									PersonalFrame myFrame=new PersonalFrame(name, msg.getFrom());//�½�˽��
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
						}else if(type.equals(MessageType.Logout)){//�û���������
							String content = msg.getContent();
							String old = showMsg.getText();
							if(old == null || old.trim().equals("")){
								showMsg.setText(content);
							}else{
								String temp = old+"\n��"+content;
								showMsg.setText(temp);
							}
							showMsg.setCaretPosition(showMsg.getText().length());
							if(!msg.getFrom().equals(name))
								dlm.removeElement(msg.getFrom());
							if(msg.getFrom().equals(name))
								flag=false;
						}else if(type.equals(MessageType.Logout)) {//����ǿ������
							String logoutName = msg.getContent();
							List<String> names = msg.getNames();
							for(String s : names){
								if(s.equals(name)){//Ҫ�رոÿͻ���
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
		setTitle(name+"�Ŀͻ���");
	}
	public void BreakServer()throws Exception{//�Ͽ�����
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
			flag = JOptionPane.showConfirmDialog(this, "ȷ��Ҫ�˳���");
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
		if(comm.equals("����") || comm.equals("enterKey")||comm.equals("sendPersonalMessage")){
			SendMessage();
		}else if(comm.equals("�Ͽ�������")){
			BreakServer();
		}else{
			
		}
		}catch(Exception e1){
			//��������
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
        	PersonalFrame myFrame=new PersonalFrame(name,userList.getSelectedValue().toString());//�½�˽��
        	personalFrames.add(myFrame);
        } 
    } 
	public static void main(String[] args)throws Exception {
		new Client();
	}
}
