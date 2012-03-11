package uk.co.unclealex.music.legacy;

import uk.co.unclealex.music.legacy.sync.MountPointFinder;

public class IpodDevice extends AbstractFileSystemDevice {

	public IpodDevice(String name, String owner, Encoding encoding, MountPointFinder mountPointFinder) {
		super(name, owner, encoding, mountPointFinder, null, true);
	}

	@Override
	public <R> R accept(DeviceVisitor<R> deviceVisitor) {
		return deviceVisitor.visit(this);
	}
}