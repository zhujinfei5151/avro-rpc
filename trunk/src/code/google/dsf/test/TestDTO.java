package code.google.dsf.test;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO
 * 
 * @author taohuifei
 * 
 */
public class TestDTO implements Serializable {

  private static final long serialVersionUID = 755382441769743659L;

  /**
	 * 流水号
	 */
	private int itemid;

	/**
	 * '客户端生成的日志唯一ID
	 */
	private String logid;

	/**
	 * 用户ID
	 */
	private int userid;

	/**
	 * 产生流量的终端ID标识
	 */
	private String imei;

	/**
	 * 产生流量的运营商网络编号
	 */
	private String sid;

	/**
	 * 产生流量的多少
	 */
	private double flowSize;

	/**
	 * 开始时间
	 */
	private Date beginTime;

	/**
	 * 结束时间
	 */
	private Date endTime;

	/**
	 * 来源国家
	 */
	private Integer homecountry;

	/**
	 * 拜访国家
	 */
	private Integer visitcountry;

  public int getItemid() {
    return itemid;
  }

  public void setItemid(int itemid) {
    this.itemid = itemid;
  }

  public String getLogid() {
    return logid;
  }

  public void setLogid(String logid) {
    this.logid = logid;
  }

  public int getUserid() {
    return userid;
  }

  public void setUserid(int userid) {
    this.userid = userid;
  }

  public String getImei() {
    return imei;
  }

  public void setImei(String imei) {
    this.imei = imei;
  }

  public String getSid() {
    return sid;
  }

  public void setSid(String sid) {
    this.sid = sid;
  }

  public double getFlowSize() {
    return flowSize;
  }

  public void setFlowSize(double flowSize) {
    this.flowSize = flowSize;
  }

  public Date getBeginTime() {
    return beginTime;
  }

  public void setBeginTime(Date beginTime) {
    this.beginTime = beginTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Integer getHomecountry() {
    return homecountry;
  }

  public void setHomecountry(Integer homecountry) {
    this.homecountry = homecountry;
  }

  public Integer getVisitcountry() {
    return visitcountry;
  }

  public void setVisitcountry(Integer visitcountry) {
    this.visitcountry = visitcountry;
  }



}
