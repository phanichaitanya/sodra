package org.hara.sodra.index;

import java.nio.ByteBuffer;
import java.util.Set;

import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;

/**
 * @author Phani Chaitanya Vempaty
 *
 */
public class SodraIndex extends PerRowSecondaryIndex {

	@Override
	public void index(ByteBuffer rowKey, ColumnFamily cf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(DecoratedKey key, Group opGroup) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void validateOptions() throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getIndexName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SecondaryIndexSearcher createSecondaryIndexSearcher(Set<ByteBuffer> columns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forceBlockingFlush() {
		// TODO Auto-generated method stub

	}

	@Override
	public ColumnFamilyStore getIndexCfs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeIndex(ByteBuffer columnName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void truncateBlocking(long truncatedAt) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean indexes(CellName name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long estimateResultRows() {
		// TODO Auto-generated method stub
		return 0;
	}

}
