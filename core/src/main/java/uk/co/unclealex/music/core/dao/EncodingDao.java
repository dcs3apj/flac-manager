package uk.co.unclealex.music.core.dao;

import uk.co.unclealex.music.core.model.KeyedBean;

public interface EncodingDao<T extends KeyedBean<T>> extends KeyedDao<T> {

	public void store(T keyedBean);
	public void remove(T keyedBean);

}
