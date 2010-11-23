package uk.co.unclealex.music;

public interface Device extends Comparable<Device> {

	public String getName();
	
	public String getOwner();
	
	public Encoding getEncoding();
	
	public <R> R accept(DeviceVisitor<R> deviceVisitor);

	public boolean isConnected();
}