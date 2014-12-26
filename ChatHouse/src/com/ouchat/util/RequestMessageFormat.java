package com.ouchat.util;

/**
 * @ClassName: RequestMessageFormat
 * @Description: TODO 添加好友或者请求加入群的请求信息
 * @author ou
 * @date 2014年12月6日 下午9:44:22
 * 
 */
public class RequestMessageFormat {
	/**
	 * 消息体 formJid:1|type:1|state:1|source:1
	 */
	public StringBuffer message = new StringBuffer();
	private String formJid;
	private String type;// 1：个人 2：聚会的
	private String state;// 状态 1：已经同意 2：未同意
	private String source;// 状态 1：我请求的 2：对方请求我

	public RequestMessageFormat() {

	}
	public RequestMessageFormat(String messageString) {
		if (messageString == null || "".equals(messageString.trim())) {
			return;
		}
		String stringArray[] = messageString.split("|");

		for (int i = 0; i < stringArray.length; i++) {
			String tempArray[] = stringArray[i].split(":");
			if (tempArray != null && tempArray.length == 2) {
				if (tempArray[0].startsWith(formJid)) {
					formJid = tempArray[1];
				} else if (tempArray[0].startsWith(type)) {
					type = tempArray[1];
				} else if (tempArray[0].startsWith(state)) {
					state = tempArray[1];
				} else if (tempArray[0].startsWith(source)) {
					source = tempArray[1];
				}
			}
		}

	}

	/**
	 * 获取完整消息体
	 * 
	 * @return
	 */
	public String getMessage() {
		return message.toString();
	}

	public String getJid() {
		return formJid;
	}

	public String getType() {
		return type;
	}

	public String getState() {
		return state;
	}

	public String getSource() {
		return source;
	}

	public void setJid(String formJid) {
		message.append("formJid:").append(formJid).append("|");
	}

	public void setType(String type) {
		message.append("type:").append(type);
	}

	public void setState(String state) {
		message.append("state:").append(state);
	}

	public void setSource(String source) {
		message.append("source:").append(source);
	}
}
