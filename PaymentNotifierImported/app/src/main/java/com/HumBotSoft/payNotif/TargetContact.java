package com.HumBotSoft.payNotif;

//Class used to hold contact information for potential notification

public class TargetContact {
	
	private String contactName;
	private String contactNumber;
	private String contactEmail;

	public TargetContact()
	{
		contactName="";
		contactNumber="";
		contactEmail="";
		
	}
	
	public void setName(String incomingName)
	{
	  this.contactName=incomingName;
	}
	
	public void setNumber(String incomingNumber)
	{
		  this.contactNumber=incomingNumber;
	}
	
	public void setEmail(String incomingEmail)
	{
	  this.contactEmail=incomingEmail;
	}
	
	public String getName()
	{
	  return this.contactName;
	}
	
	public String getNumber()
	{
		return this.contactNumber;
	}
	
	public String getEmail()
	{
	 return this.contactEmail;
	}
	
	
}
