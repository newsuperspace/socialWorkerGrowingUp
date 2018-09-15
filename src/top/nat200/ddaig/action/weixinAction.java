package top.nat200.ddaig.action;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

@Component("weixinAction")
@Scope("prototype")
public class weixinAction extends ActionSupport {

	// ===================属性驱动==================
	private String signature;
	private String nonce;
	private String timestamp;
	private String echostr;
	private String encrypt_type;

	public String getEncrypt_type() {
		return encrypt_type;
	}

	public void setEncrypt_type(String encrypt_type) {
		this.encrypt_type = encrypt_type;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getEchostr() {
		return echostr;
	}

	public void setEchostr(String echostr) {
		this.echostr = echostr;
	}

	// ====================Actions==================
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String recall() throws IOException {
		// 先获得微信操作的关键对象——wxMpService，之后所有与微信公众号平台有关的操作都是通过该对象完成的
		ServletContext servletContext = ServletActionContext.getServletContext();
		WxMpService mpService = (WxMpService) servletContext.getAttribute("mpService");
		WxMpMessageRouter router = (WxMpMessageRouter) servletContext.getAttribute("router");
		if (null == mpService) {
			mpService = this.getWeixinConfig();
			servletContext.setAttribute("mpService", mpService);
		}
		if (null == router) {
			router = this.getRouter(mpService);
			servletContext.setAttribute("router", router);
		}

		// 得到当前这次请求/响应的response对象，用来获取其输出流，向其中写入信息
		HttpServletResponse response = ServletActionContext.getResponse();
		// 由于哦们要直接将反馈信息写入到response的输出流中然后直接返回给微信服务器，因此在返回的时候是不会重新经过Struts的拦截器的，也就不会自动实现字符集设置，所以这里要手动设置以下头部信息
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		// 开始响应从微信端服务器发来的请求
		if (StringUtils.isNotBlank(echostr)) {
			// 只有在初次握手的时候才会发来echostr请求参数，只要向响应正文中回显echostr并返回给微信端就能成功完成握手
			response.getWriter().println(echostr);
			// 然后这里需要开启一个独立线程进行有关公众号的初始化操作（例如菜单设置等）
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 先睡眠5秒 = 5000毫秒，确保微信官方服务器已经与当前第三方服务器建立关系
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 执行初始化设置公众号的逻辑
					InitPlatform();
				}
			}).start();
			return null;
		}
		// 检测是否为合法的微信端请求
		if (!mpService.checkSignature(timestamp, nonce, signature)) {
			response.getWriter().println("这是一个非法请求");
			// 返回null，表示不经过任何结果集处理，而是直接根据response响应正文中的特定格式的文本内容返回给微信端服务器
			return null;
		}
		// 开始处理微信服务器发来的日常请求,先要获取加密类型
		String encryptType = StringUtils.isBlank(this.encrypt_type) ? "raw" : this.encrypt_type;
		// 从微信发来的是未加密类型消息
		if ("raw".equals(encryptType)) {
			// 明文传输的消息，将请求正文中的xml格式字符串封装成特定WxMpXmlMessage类型对象
			WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(ServletActionContext.getRequest().getInputStream());
			// 然后将消息对象交给准备好的router处理，获得返回消息类型消息对象outMessage
			WxMpXmlOutMessage outMessage = router.route(inMessage);
			if (outMessage == null) {
				// 为null，说明路由配置有问题，需要注意
				response.getWriter().write("");
			}
			// 将返回消息类型对象转化成XML格式，然后写入到响应正文中回传给微信服务器即可完成一次处理流程
			response.getWriter().write(outMessage.toXml());
			// 返回null，表示不经过任何结果集处理，而是直接根据response响应正文中的特定格式的文本内容返回给微信端服务器
			return null;
		}
		// 加密类型消息
		if ("aes".equals(encryptType)) {
			// 是aes加密的消息
			String msgSignature = ServletActionContext.getRequest().getParameter("msg_signature");
			WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(
					ServletActionContext.getRequest().getInputStream(), mpService.getWxMpConfigStorage(), timestamp,
					nonce, msgSignature);
			WxMpXmlOutMessage outMessage = router.route(inMessage);
			if (outMessage == null) {
				// 为null，说明路由配置有问题，需要注意
				response.getWriter().write("");
			}
			response.getWriter().write(outMessage.toEncryptedXml(mpService.getWxMpConfigStorage()));
			// 返回null，表示不经过任何结果集处理，而是直接根据response响应正文中的特定格式的文本内容返回给微信端服务器
			return null;
		}

		// 返回null，表示不经过任何结果集处理，而是直接根据response响应正文中的特定格式的文本内容返回给微信端服务器
		response.getWriter().println("不可识别的加密类型");
		return null;
	}


	// ======================内部工具函数============================
	/**
	 * 【一次性】
	 * 得到操作特定微信公众号的WxMpService对象；
	 * 
	 * @return
	 */
	private WxMpService getWeixinConfig() {

		WxMpService mpService = new WxMpServiceImpl();
		WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
		config.setAppId("");
		config.setSecret("");
		config.setToken("");
		config.setAesKey("");

		mpService.setWxMpConfigStorage(config);
		return mpService;
	}

	/**
	 * 【一次性】
	 * 包含所有微信公众号的初始化操作,只在微信端关联本地服务器时调用一次
	 * （1）设置微信菜单
	 * （2）
	 */
	private void InitPlatform() {

	}

	/**
	 * 【一次性】
	 * 获取用来处理从微信服务器发来的所有类型请求
	 * @param mpService
	 * @return
	 */
	private WxMpMessageRouter getRouter(WxMpService mpService) {
		// 这里新创建一个Router，该Router只服务于本Service类的route()方法
		WxMpMessageRouter newRouter = new WxMpMessageRouter(mpService);

		// // 接收客服会话管理"事件"（同步消息）
		// // 消息类型（msgType）是event（事件）类型——WxConsts.XML_MSG_EVENT
		// // 进一步的"事件类型"是———客服服务中的具体事件类型（客服建立、客服关闭和客服转介）
		// newRouter.rule().async(false).msgType(WxConsts.XML_MSG_EVENT)
		// .event(WxMpEventConstants.CustomerService.KF_CREATE_SESSION)
		// .handler(this.kfSessionHandler).end();
		// newRouter.rule().async(false).msgType(WxConsts.XML_MSG_EVENT)
		// .event(WxMpEventConstants.CustomerService.KF_CLOSE_SESSION)
		// .handler(this.kfSessionHandler).end();
		// newRouter.rule().async(false).msgType(WxConsts.XML_MSG_EVENT)
		// .event(WxMpEventConstants.CustomerService.KF_SWITCH_SESSION)
		// .handler(this.kfSessionHandler).end();

		// 菜单"单击事件"（同步消息）
		// 消息类型（msgType）是event（事件）类型——WxConsts.XML_MSG_EVENT
		// 事件类型——BUTTON_CLICK 按钮点击触发的事件
		newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT).event(WxConsts.EventType.CLICK)
				.handler(this.menuClickHandler).end();

		// // 菜单"打开URL事件"（同步消息）
		// // 消息类型（msgType）是event（事件）类型——WxConsts.XML_MSG_EVENT
		// // 事件类型——BUTTON_VIEW 点击按钮是用来打开URL页面的事件
		// newRouter.rule().async(false).msgType(WxConsts.XML_MSG_EVENT)
		// .event(WxConsts.BUTTON_VIEW).handler(this.nullHandler).end();

		// 关注"事件"（同步消息）
		// 消息类型（msgType）是event（事件）类型——WxConsts.XML_MSG_EVENT
		// 事件类型——EVT_SUBSCRIBE 关注公众号触发的事件
		newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT).event(WxConsts.EventType.SUBSCRIBE)
				.handler(this.subscribeHandler).end();

		// 取消关注"事件"（同步消息）
		// 消息类型（msgType）是event（事件）类型——WxConsts.XML_MSG_EVENT
		// 事件类型——EVT_UNSUBSCRIBE 取消关注公众号触发的事件
		newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT).event(WxConsts.EventType.UNSUBSCRIBE)
				.handler(unsubscribeHandler).end();

		// // 用户点击了“上报地理位置”的菜单按钮后，主动上报所触发的"事件"（同步消息）——————主动上报地理信息
		// // 消息类型（msgType）是event（事件）类型——WxConsts.XML_MSG_EVENT
		// // 事件类型——EVT_LOCATION
		// // 接下来就能在handler中得到用户的地理位置了
		// newRouter.rule().async(false).msgType(WxConsts.XML_MSG_EVENT)
		// .event(WxConsts.EVT_LOCATION).handler(this.getLocationHandler()).end();

		// // 微信客户端自动收集用户地理信息，所发送的消息（同步消息）——————被动收集地理信息
		// // 消息类型（msgType）是XML_MSG_LOCATION ，对应微信公众平台文档-消息管理-接收消息-接收普通消息
		// 中关于地理信息的location消息类型
		// // 之后哦们就可以在handler中得到用户的位置数据了
		// newRouter.rule().async(false).msgType(WxConsts.XML_MSG_LOCATION)
		// .handler(this.getLocationHandler()).end();

		// 扫码"事件"（同步消息）
		// 消息类型（msgType）是event(事件)类型——WxConsts.XML_MSG_EVENT
		// 事件类型——EVT_SCANCODE_PUSH
		// 当用户打开QRCODE扫描后得到的信息会回传给哦们的服务器后就会触发该类型的事件，此时哦们就可以在handler中获知用户扫描的QRCODE的字符串内容了
		newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT).event(WxConsts.EventType.SCANCODE_WAITMSG)
				.handler(this.scaneHandler).end();

		// // 默认（不属于以上任何一种消息类型处理的剩余消息都会被这个handler所处理）★★★★ 细粒度最“粗，所以放在最后设置”
		// newRouter.rule().async(false).handler(this.getMsgHandler()).end();

		// 最后，将新创建的router放入到router中备用
		return newRouter;
	}
	
}
