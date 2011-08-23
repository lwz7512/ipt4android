package com.pintu.data;

public class StoryInfo {

	// 品图（故事）唯一标识
	public String id;
	// 贴图ID，对应于TPicItem.id
	public String follow;
	// 品图作者ID
	public String owner;
	//品图作者名称
	public String author;
	// 发表时间
	public String publishTime;
	// 品图内容
	public String content;
	// 是否被投票为经典
	public int classical;
	
	//鲜花数目
	public int flower;
	//爱心数目
	public int heart;
	//鸡蛋数目
	public int egg;
	//经典数目
	public int star;

}
