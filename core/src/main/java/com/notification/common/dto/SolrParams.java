package com.notification.common.dto;

import java.util.List;

public class SolrParams {
	
	private List<String> fq;
	
	private String sortField;
	
	private String order;
	
	private Integer rows;
	
	private List<String> fl;
	
	private Double lat;
	
	private Double lon;
	
	private Integer radius;

	public List<String> getFq() {
		return fq;
	}

	public void setFq(List<String> fq) {
		this.fq = fq;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public List<String> getFl() {
		return fl;
	}

	public void setFl(List<String> fl) {
		this.fl = fl;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}

	public Integer getRadius() {
		return radius;
	}

	public void setRadius(Integer radius) {
		this.radius = radius;
	}

}
