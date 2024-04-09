package com.jones.tank.object.dataapi;


public enum OperationType {
	EQ(" =%s ", "equals"),
	GT(" >%s ","greater than"),
	LT(" <%s ","less than"),
	GTE(" >=%s ","greater than or equals"),
	LTE(" <=%s ","less than or equals"),
	IN(" in (%s) ","in"),
	NIN(" not in (%s) ","not in"),
	LK(" like concat('%%',%s,'%%') ","like");


	public final String symbol;
	public final String description;

	OperationType(String symbol, String description) {
		this.symbol = symbol;
		this.description = description;
	}

	public boolean needCollectionParam(){
		return OperationType.IN.equals(this) || OperationType.NIN.equals(this);
	}

	public String formatClause(String[] value){
		if(OperationType.IN.equals(this) || OperationType.NIN.equals(this)){
			return String.format(this.symbol, String.join("','", value));
		}
		return String.format(this.symbol, value[0]);
	}

}
