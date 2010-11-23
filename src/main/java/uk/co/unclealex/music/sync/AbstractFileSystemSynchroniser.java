package uk.co.unclealex.music.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import uk.co.unclealex.music.Device;

public abstract class AbstractFileSystemSynchroniser<D extends Device> extends AbstractSynchroniser<D> {

	private File i_deviceRoot;
	
	@Override
	protected Set<DeviceFile> listDeviceFiles() throws IOException {
		RelativePathFileFactory<DeviceFile> factory = new RelativePathFileFactory<DeviceFile>() {
			@Override
			public DeviceFile createRelativeFilePath(String relativePath, File f) throws IOException {
				return createDeviceFile(relativePath, f);
			}
		};
		return listRelativePathFiles(getDeviceRoot(), factory);
	}

	@Override
	protected void remove(DeviceFile deviceFile) throws IOException {
		File file = new File(getDeviceRoot(), deviceFile.getId());
		file.delete();
	}

	@Override
	protected void add(LocalFile localFile) throws IOException {
		File targetFile = new File(getDeviceRoot(), createRemoteRelativeFilePath(localFile));
		targetFile.getParentFile().mkdirs();
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			File sourceFile = localFile.getFile();
			in = new FileInputStream(sourceFile);
			out = new FileOutputStream(targetFile);
			in.getChannel().transferTo(0, sourceFile.length(), out.getChannel());
		}
		finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	@Override
	protected void closeDevice() throws IOException {
		try {
			removeEmptyDirectories(getDeviceRoot(), false, getDevice().getEncoding().getExtension());
		}
		finally {
			disconnect();
		}
	}

	protected void removeEmptyDirectories(File dir, boolean deleteDirectory, String extension) {
		for (File child : dir.listFiles()) {
			if (child.isDirectory()) {
				removeEmptyDirectories(child, true, extension);
			}
			else if (!extension.equals(FilenameUtils.getExtension(child.getName()))) {
				child.delete();
			}
		}
		if (dir.listFiles().length == 0) {
			dir.delete();
		}
	}

	protected abstract DeviceFile createDeviceFile(String relativePath, File f);

	protected abstract String createRemoteRelativeFilePath(LocalFile localFile);

	protected abstract void disconnect() throws IOException;

	public File getDeviceRoot() {
		return i_deviceRoot;
	}

	public void setDeviceRoot(File deviceRoot) {
		i_deviceRoot = deviceRoot;
	}

}