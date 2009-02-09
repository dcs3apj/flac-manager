package uk.co.unclealex.music.core.service.filesystem;

import java.io.IOException;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;


public interface RepositoryManager {

	String PROPERTY_ID = "objectId";
	String PROPERTY_LENGTH = "length";
	

	public void refresh();
	public Repository getRepository();
	
	public int getObjectId(Node node) throws IOException, RepositoryException;
	public long getLength(Node node) throws RepositoryException;
	public void add(int id) throws RepositoryException;
	public int remove(int id) throws RepositoryException;
	public Set<Node> find(int id) throws RepositoryException;
	public void addOrUpdate(int id) throws RepositoryException;
}