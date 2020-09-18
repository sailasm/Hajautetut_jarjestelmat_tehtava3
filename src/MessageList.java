import java.util.ArrayList;

public class MessageList{
	
	public ArrayList<Message> messageList;

	
//	public MessageList(ArrayList<VSPMessage> messageList) 
//	{
//		_messageList = messageList;
//	}
	
	public MessageList copy()
	{
		ArrayList<Message> copyList = new ArrayList<Message>();
		MessageList copy;
		
		for(Message msg: messageList)
		{
			copyList.add(msg.copy());
		}
		
		copy = new MessageList();
		copy.messageList = copyList;
		
		return copy;
	}
	
	public synchronized MessageList accessMessageList(String whatDo, Message msg)
	{
		
		MessageList messageList = new MessageList();

		
		if(whatDo == "ADD" )
		{
			messageList.messageList.add(msg);
			return null;
		}
		else if(whatDo == "GET")
		{
			return messageList.copy(); //_receivedMessagesThisFrame.copy();
		}
		else
		{
			messageList.messageList.clear();
			return null;
		}
			
	}
	
}
