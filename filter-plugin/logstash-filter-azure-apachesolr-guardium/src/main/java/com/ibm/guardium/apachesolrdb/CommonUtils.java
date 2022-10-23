/*
 * � Copyright IBM Corp. 2021, 2022 All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.guardium.apachesolrdb;

/**
 * this class contains all the utility methods which contain the common
 * functionality
 *
 */
public class CommonUtils {
	/**
	 * this method removes the first and last occurance("") from string
	 * 
	 * @param str
	 * @return
	 */
	public static String formatFirstAndLastChar(String str) {
		str = str.substring(1, str.length() - 1);
		return str;
	}

	/**
	 * this method removes the last char from string
	 * 
	 * @param str
	 * @return
	 */
	public static String formatLastChar(String core) {
		core = core.substring(0, core.length() - 1);
		return core;
	}

	/**
	 * this method removes the first char from string
	 * 
	 * @param str
	 * @return
	 */
	public static String formatFirstChar(String str) {
		str = str.substring(1, str.length());
		return str;
	}
	
	public static Boolean ManageCollection(String collection) {
		return collection.contains(ApplicationConstant.CREATE_CORE)
				|| collection.contains(ApplicationConstant.UPDATE_CORE)
				|| collection.contains(ApplicationConstant.DELETE_CORE)
				|| collection.contains(ApplicationConstant.SWAP_CORE)
				|| (collection.contains(ApplicationConstant.LIST_COLLECTION) && !collection.contains("wt=json"))
				|| collection.contains(ApplicationConstant.DELETE_COLLECTION)
				|| collection.contains(ApplicationConstant.COLLECTION_PROP)
				|| collection.contains(ApplicationConstant.COL_STATUS)
				|| collection.contains(ApplicationConstant.MODIFY_COLLECTION)
				|| (collection.contains(ApplicationConstant.RELOAD) && !collection.contains("wt=javabin"))
				|| collection.contains(ApplicationConstant.MIGRATE_COLLECTION)
				|| (collection.contains(ApplicationConstant.REINDEX) && !collection.contains("wt=javabin"))
				|| (collection.contains(ApplicationConstant.BACKUP) && !collection.contains("wt=javabin"))
				|| collection.contains(ApplicationConstant.RESTORE)
				|| collection.contains(ApplicationConstant.REBALANCELEADERS)
				|| (collection.contains(ApplicationConstant.CORE_STATUS)&& !collection.contains("wt=json"))
				|| collection.contains(ApplicationConstant.SPLIT_CORE)
				|| collection.contains(ApplicationConstant.REQUESTRECOVERY)
				|| collection.contains(ApplicationConstant.MERGEINDEXES) ? Boolean.TRUE : Boolean.FALSE;
	}

	public static Boolean ManageCluster(String cluster) {
		return (cluster.contains(ApplicationConstant.CLUSTERSTATUS) && !cluster.contains("wt=json"))
				|| cluster.contains(ApplicationConstant.CLUSTERPROP)
				|| cluster.contains(ApplicationConstant.BALANCESHARDUNIQUE)
				|| cluster.contains(ApplicationConstant.ADDROLE) || cluster.contains(ApplicationConstant.OVERSEERSTATUS)
				|| cluster.contains(ApplicationConstant.MIGRATESTATEFORMAT)
				|| cluster.contains(ApplicationConstant.UTILIZENODE)
				|| cluster.contains(ApplicationConstant.REPLACENODE) || cluster.contains(ApplicationConstant.DELETENODE)
				|| cluster.contains(ApplicationConstant.REMOVEROLE) ? Boolean.TRUE : Boolean.FALSE;
	}

	public static Boolean ManageShard(String shard) {
		return shard.contains(ApplicationConstant.CREATESHARD) || shard.contains(ApplicationConstant.SPLITSHARD)
				|| shard.contains(ApplicationConstant.DELETESHARD) || shard.contains(ApplicationConstant.FORCELEADER)
				|| (shard.contains(ApplicationConstant.REQUESTSTATUS) && !shard.contains("wt=javabin")) ? Boolean.TRUE
						: Boolean.FALSE;
	}

	public static Boolean ManageReplica(String replica) {
		return replica.contains(ApplicationConstant.ADDREPLICA) || replica.contains(ApplicationConstant.MOVEREPLICA)
				|| replica.contains(ApplicationConstant.DELETEREPLICA)
				|| replica.contains(ApplicationConstant.ADDREPLICAPROP)
				|| replica.contains(ApplicationConstant.DELETEREPLICAPROP) ? Boolean.TRUE : Boolean.FALSE;
	}

	public static Boolean ManageAlias(String alias) {
		return alias.contains(ApplicationConstant.CREATEALIAS)
				|| (alias.contains(ApplicationConstant.LISTALIASES) && !alias.contains("wt=json"))
				|| alias.contains(ApplicationConstant.ALIASPROP) || alias.contains(ApplicationConstant.DELETEALIAS)
						? Boolean.TRUE
						: Boolean.FALSE;
	}
	
}
