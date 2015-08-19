package org.tyrannyofheaven.bukkit.zPermissions.uuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.tyrannyofheaven.bukkit.util.uuid.UuidDisplayName;
import org.tyrannyofheaven.bukkit.util.uuid.UuidResolver;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsPlugin;

import net.kaikk.mc.uuidprovider.UUIDFetcher;
import net.kaikk.mc.uuidprovider.UUIDProvider;

public class UUIDProvResolver implements UuidResolver {

	private UUIDProvider uuidprov = null;
	private ZPermissionsPlugin plugin = null;
	private boolean enabled = false;
	
	public UUIDProvResolver(UUIDProvider provider, ZPermissionsPlugin plug){
		uuidprov = provider;
		plugin = plug;
		if(uuidprov != null){
			enabled = true;
		}
	}
	
	@Override
	public UuidDisplayName resolve(String username) {
		if(!this.enabled){
			return null;
		}
		
		UUID uuid = UUIDProvider.retrieve(username);
		if(uuid == null)
			return null;
		else
			return new UuidDisplayName(uuid, username);
	}

	@Override
	public UuidDisplayName resolve(String username, boolean cacheOnly) {
		if(!this.enabled){
			return null;
		}
		UUID uuid;
		
		if(cacheOnly){
			uuid = UUIDProvider.getCachedPlayer(username);
			if(uuid == null)
				return null;
			else
				return new UuidDisplayName(uuid, username);
		} else {
			return resolve(username);
		}
	}

	@Override
	public Map<String, UuidDisplayName> resolve(Collection<String> usernames)
			throws Exception {
		
		if(!this.enabled){
			return null;
		}
		
		plugin.getLogger().info("UUIDProvResolver : Beginning bulk UUID Fetch - " + usernames.size() + "usernames to look up!");
		Map<String, UuidDisplayName> ret = new HashMap<String, UuidDisplayName>();
		ArrayList<String> fetch = new ArrayList<String>();
		//Sort out the usernames we already have in the cache.
		for(String n : usernames){
			UUID u = UUIDProvider.getCachedPlayer(n);
			if(u != null){
				ret.put(n, new UuidDisplayName(u,n));
			} else {
				//Player isn't in cache, add to fetch list for bulk retreival.
				fetch.add(n);
			}
		}
		long leftover = usernames.size() - ret.size();
		plugin.getLogger().info("UUIDProvResolver : Found " + ret.size() + " in local cache - fetching " + leftover + " from Mojang.");
		
		//Do a bulk UUID request from mojang.
		Map<String, UUID> uuidMap = new HashMap<String, UUID>();
		UUIDFetcher f = new UUIDFetcher(fetch,true);
		uuidMap = f.call();

		plugin.getLogger().info("UUIDProvResolver : fetched " + uuidMap.size() + " uuids from Mojang.");
		
		for(String s : uuidMap.keySet()){
			ret.put(s, new UuidDisplayName(uuidMap.get(s),s));
		}
		
		leftover = usernames.size() - ret.size();
		if(leftover == 0){
			plugin.getLogger().info("UUIDProvResolver : Successfully retreived all UUIDs!");
		} else {
			plugin.getLogger().warning("UUIDProvResolver : Failed to retreive " + leftover + " UUIDs!");
		}
		
		return ret;
	}

	@Override
	public void preload(String username, UUID uuid) {
		if(!this.enabled){
			return;
		}
		boolean pd = UUIDProvider.cacheAdd(uuid, username);
		if(pd == false){
			plugin.getLogger().warning("UUIDProvResolver : Failed to add user " + username + " to the cache!");
		}
		
	}

	@Override
	public void invalidate(String username) {
		if(!this.enabled){
			return;
		}
		UUIDProvider.cacheRemove(username);
	}

	@Override
	public void invalidateAll() {
		if(!this.enabled){
			return;
		}
		plugin.getLogger().warning("ZPerms UUIDProvider Resolver wants to clear the entire cache - Denying!");
		
	}

}
