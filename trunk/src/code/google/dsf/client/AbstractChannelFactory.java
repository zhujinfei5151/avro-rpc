package code.google.dsf.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

import org.jboss.netty.channel.Channel;

/**
 * 抽象通道创建工厂，模板对象
 * @author taohuifei
 *
 */
public abstract class AbstractChannelFactory {

	private static ConcurrentHashMap<String, FutureTask<List<Channel>>> channels = new ConcurrentHashMap<String, FutureTask<List<Channel>>>();

	public Channel get(final String targetIP, final int targetPort,
			final int connectTimeout, String... customKey) throws Exception {
		return get(targetIP, targetPort, connectTimeout, 1, customKey);
	}

	public Channel get(final String targetIP, final int targetPort,
			final int connectTimeout, final int clientNums, String... customKey)
			throws Exception {
		String key = targetIP + ":" + targetPort;
		if (customKey != null && customKey.length == 1) {
			key = customKey[0];
		}
		if (channels.containsKey(key)) {
			if (clientNums == 1) {
				return channels.get(key).get().get(0);
			} else {
				Random random = new Random();
				return channels.get(key).get().get(random.nextInt(clientNums));
			}
		} else {
			final String cacheKey = key;
			FutureTask<List<Channel>> task = new FutureTask<List<Channel>>(
					new Callable<List<Channel>>() {
						public List<Channel> call() throws Exception {
							List<Channel> clients = new ArrayList<Channel>(
									clientNums);
							for (int i = 0; i < clientNums; i++) {
								clients.add(createChannel(targetIP, targetPort,
										connectTimeout, cacheKey));
							}
							return clients;
						}
					});
			FutureTask<List<Channel>> currentTask = channels.putIfAbsent(key,
					task);
			if (currentTask == null) {
				task.run();
			} else {
				task = currentTask;
			}
			if (clientNums == 1)
				return task.get().get(0);
			else {
				Random random = new Random();
				return task.get().get(random.nextInt(clientNums));
			}
		}
	}

	public void removeClient(String key) {
		try {
			channels.remove(key);
		} catch (Exception e) {
		}
	}

	protected abstract Channel createChannel(String targetIP, int targetPort,
			int connectTimeout, String key) throws Exception;

}
