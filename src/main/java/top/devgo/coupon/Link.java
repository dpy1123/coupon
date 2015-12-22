package top.devgo.coupon;

import top.devgo.coupon.ZDMItem.TaobaoUrl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "taobao_url" })
public class Link {
	@JsonProperty("name")
	private String name;
	@JsonProperty("link_nofollow")
	private String link_nofollow;
	@JsonProperty("link")
	private String link;
	@JsonProperty("taobao_url")
	private TaobaoUrl taobao_url;
	@JsonProperty("buy_btn_domain")
	private String buy_btn_domain;
}