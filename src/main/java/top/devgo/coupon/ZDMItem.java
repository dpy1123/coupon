package top.devgo.coupon;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "article_category", "article_tese_tags","article_love_rating","nice_probreport","article_user_avatar",
"pb_pic_url","pb_pic_style","display_none"	//众测
})

public class ZDMItem {
	@JsonProperty("article_id")
	private String Id;
	@JsonProperty("article_title")
	private String title;
	@JsonProperty("article_price")
	private String price;
	@JsonProperty("article_content")
	private String content;
	@JsonProperty("article_content_all")
	private String contentAll;
	@JsonProperty("article_referrals")
	private String referrals;
	@JsonProperty("article_user_url")
	private String article_user_url;
	@JsonProperty("article_date")
	private String date;
	@JsonProperty("article_real_date")
	private String article_real_date;
	@JsonProperty("article_is_sold_out")
	private String isSoldOut;
	
	@JsonProperty("article_is_timeout")
	private String article_is_timeout;
	@JsonProperty("article_mall")
	private String article_mall;
	@JsonProperty("article_worthy")
	private String article_worthy;
	@JsonProperty("article_unworthy")
	private String article_unworthy;
	@JsonProperty("article_item_title_mode")
	private String article_item_title_mode;
	@JsonProperty("article_comment")
	private String article_comment;
	@JsonProperty("article_collection")
	private String article_collection;
	@JsonProperty("article_pic")
	private String article_pic;
	@JsonProperty("article_pic_style")
	private String article_pic_style;
	@JsonProperty("article_link")
	private String article_link;
	@JsonProperty("taobao_url")
	private TaobaoUrl taobao_url;
	@JsonProperty("article_link_domain")
	private String article_link_domain;
	@JsonProperty("link_nofollow")
	private String link_nofollow;
	@JsonProperty("article_link_list")
	private Link[] article_link_list;
	@JsonProperty("article_url")
	private String article_url;
	@JsonProperty("article_mall_url")
	private String article_mall_url;
	@JsonProperty("article_channel")
	private String article_channel;
	@JsonProperty("article_channel_url")
	private String article_channel_url;
	@JsonProperty("article_channel_id")
	private String article_channel_id;
	@JsonProperty("is_out")
	private String is_out;
	@JsonProperty("top_category")
	private String top_category;
	
	@JsonProperty("is_black_five")
	private String is_black_five;
	@JsonProperty("article_tese_tags")
	private String article_tese_tags;
	
	@JsonProperty("article_author")
	private String article_author;
	@JsonProperty("timesort")
	private String timesort;
	
	
	public class TaobaoUrl {
		@JsonProperty("is_taobao")
		private String is_taobao;
		@JsonProperty("product_id")
		private String product_id;
		@JsonProperty("taobao_url")
		private String taobao_url;
	}
	

}