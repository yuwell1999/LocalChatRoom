import java.net.Socket;

public class UserInfo{
	private Socket socket;
	private String name;
	public UserInfo() {
		super();
	}
	public UserInfo(Socket socket, String name) {
		super();
		this.socket = socket;
		this.name = name;
	}
	public Socket getSocket() {
		return socket;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserInfo other = (UserInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
