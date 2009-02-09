package uk.co.unclealex.music.core.service.filesystem;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import uk.co.unclealex.music.base.service.filesystem.ManagedRepository;
import uk.co.unclealex.music.base.service.filesystem.RepositoryManager;

public class ManagedRepositoryImpl extends RepositoryImpl implements ManagedRepository {

	private RepositoryManager i_repositoryManager;
	
	public ManagedRepositoryImpl(RepositoryConfig repConfig, RepositoryManager repositoryManager) throws RepositoryException {
		super(repConfig);
		i_repositoryManager = repositoryManager;
	}

	@Override
	public RepositoryManager getRepositoryManager() {
		return i_repositoryManager;
	}
}
