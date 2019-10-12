import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class PersonalFrame extends JFrame implements ActionListener{
	//Frame Name
	private String From;
	private String name;
	//PersonalFrame
	private JPanel PCenter = new JPanel();
	private JTextArea PShowMsg = new JTextArea(10,20);
	private JScrollPane PShowPane = new JScrollPane(PShowMsg);
	private JPanel PoperPane = new JPanel();
	private JTextField PmsgInput = new JTextField(28);
	private JButton Psend = new JButton("发送");	
	public PersonalFrame(String From,String name){

		this.From=From;
		this.name = name;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initPersonalFrame();
		initEvent();
	}
	private void initPersonalFrame(){
		PmsgInput.setActionCommand("send");
		PShowMsg.setEditable(false);
		BorderLayout bl3 = new BorderLayout();
		PCenter.setLayout(bl3);
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		PoperPane.setLayout(fl);
		PoperPane.add(PmsgInput);
		PoperPane.add(Psend);
		PCenter.add(PShowPane,BorderLayout.CENTER);
		PCenter.add(PoperPane,BorderLayout.SOUTH);
		add(PCenter,BorderLayout.CENTER);
		add(PCenter);
		setSize(500,400);
		setLocation(500,200);
		setResizable(false);
		setTitle("与"+name+"私聊");
		setVisible(true);
	}
	private void initEvent(){
		 Psend.addActionListener(this);
		 PmsgInput.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		try{
			String cmd=e.getActionCommand();
			if(cmd.equals("发送")){
				SendMessage();
			}
		}catch(Exception ex)
		{}
		
	}
	private void SendMessage()throws Exception{
		//判断是否连接了服务器
		if(Client.socket == null){
			JOptionPane.showMessageDialog(this, "没有连接服务器或已经断开服务器");
			return;
		}
		//发送消息
		String content = PmsgInput.getText();
		if(content == null || content.trim().equals("")){
			JOptionPane.showMessageDialog(this, "发送为空！");
			return;
		}else{
			//清空文本输入框
			PmsgInput.setText("");
		}
		Message msg = new Message(From, content, MessageType.Chat, ChatState.Personal, name);
		Client.oos.writeObject(msg);
		Client.oos.flush();
	}
	public void showMsg(Message msg){
		String content = msg.getContent();
		String old = PShowMsg.getText();
		if(old == null || old.trim().equals("")){
			PShowMsg.setText(content);
		}else{
			String temp = old+"\n"+content;
			PShowMsg.setText(temp);
		}
		PShowMsg.setCaretPosition(PShowMsg.getText().length());
	}
	public String getName(){
		return name;
	}
	@Override
	public void dispose(){
		Client.personalFrames.remove(this);
		super.dispose();
	}
}