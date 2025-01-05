package vip.xiaozhao.intern.baseUtil.intf.utils.redis;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import vip.xiaozhao.intern.baseUtil.intf.constant.RedisConstant;
import vip.xiaozhao.intern.baseUtil.intf.utils.JsonUtils;

import java.util.List;
import java.util.Set;


/**
 * 目前只有get set remove缓存操作，后续可以使用redis的string list map等特性;
 *
 * @author shuize
 */
@Component
@Slf4j
@DependsOn("redisConstant")
public class RedisUtils {



    private static int MAX_IDLE = 200;          // 设置最大空闲数
    private static int MAX_WAIT = 10000;        // 最大连接时间
    private static int TIMEOUT = 10000;         // 超时时间
    private static JedisPool pool = null;


    /**
     * redis过期时间,以秒为单位
     */
    public final static int EXRP_ONE_MINITE = 60;          //一分钟
    public final static int EXRP_ONE_HOUR = 60 * 60;          //一小时
    public final static int EXRP_ONE_DAY = 60 * 60 * 24;        //一天
    public final static int EXRP_HALF_AN_DAY = 60 * 60 * 24;        //一天
    public final static int EXRP_ONE_MONTH = 60 * 60 * 24 * 30;   //一个月

    /**
     * 初始化线程池
     */
    static {
        try {
            if (!StringUtils.equals(RedisConstant.envName, "dev")){
            //if (true){
                System.out.println("===============================");
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxIdle(MAX_IDLE);
                config.setMaxWaitMillis(MAX_WAIT);
                config.setTestOnBorrow(true);
                int databaseIndex = 1; // 例如，连接到数据库 0
                pool = new JedisPool(config, RedisConstant.hostName, RedisConstant.port, TIMEOUT, null, databaseIndex);
            }
        } catch (Exception e) {
            log.error("fail to init pool", e);
        }
    }

    /**
     * 获取连接
     */
    private static synchronized Jedis getJedis() {
        try {
            if (pool != null) {
                Jedis jedis = pool.getResource();
                return jedis;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("连接池连接异常", e);
            return null;
        }
    }

    /**
     * @param @param  key
     * @param @param  obj
     * @param @return
     * @return boolean 返回类型
     * @Description:插入对象
     */
    public static boolean set(String key, Object obj, int seconds) {
        Jedis jedis = null;
        String value = JsonUtils.toStr(obj);
        try {
            jedis = getJedis();
            String code = jedis.set(key, value);
//				String code = jedis.set(key, value, "NX", "EX", seconds);
//				Long pttl = jedis.pttl(key);
            log.info("redis insert == " + key + "==result:" + code + "===pttl:");
            if (code.equalsIgnoreCase("ok")) {
                Long expire = jedis.expire(key, seconds);
                log.info("redis === pttl : " + expire);
                return true;
            }
        } catch (Exception e) {
            log.debug("插入数据有异常.");
            return false;
        } finally {
            getClose(jedis);
        }
        return false;
    }
    public static boolean addToSet(String key, Object value, int seconds,boolean isPermanent) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Long result = jedis.sadd(key, String.valueOf(value)); // 使用sadd方法添加到集合
            if (result > 0) {
                if(!isPermanent){
                    jedis.expire(key, seconds); // 设置过期时间
                }
                return true;
            }
        } catch (Exception e) {
            log.debug("添加到 Set 发生异常.");
            return false;
        } finally {
            getClose(jedis);
        }
        return false;
    }


    /**
     * @param @param  key
     * @param @param  obj
     * @param @return
     * @return boolean 返回类型
     * @Description:插入String对象
     */
    public static boolean set(String key, String value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String code = jedis.set(key, value);
            if (code.equalsIgnoreCase("ok")) {
                jedis.expire(key, seconds);
                return true;
            }
        } catch (Exception e) {
            log.debug("插入数据有异常.");
            return false;
        } finally {
            getClose(jedis);
        }
        return false;
    }

    /**
     * @param @param  key
     * @param @return
     * @return boolean 返回类型
     * @Description:删除key
     */
    public static boolean remove(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Long code = jedis.del(key);
            if (code > 1) {
                return true;
            }
        } catch (Exception e) {
            log.debug("删除key异常.");
            return false;
        } finally {
            getClose(jedis);
        }
        return false;
    }

    /* ----------- object --------- */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String value = jedis.get(key);
            return (T) JsonUtils.convert(value, clazz);
        } catch (Exception e) {
            log.debug("get key异常.");
            return null;
        } finally {
            getClose(jedis);
        }
    }

    /* ----------- list<T> --------- */
    public static <T> List<T> getList(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String value = jedis.get(key);
            return JsonUtils.toList(value, clazz);
        } catch (Exception e) {
            log.debug("get key异常.");
            return null;
        } finally {
            getClose(jedis);
        }
    }

    public static String get(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            System.out.println("jedis获取的-----------------"  + jedis.get(key));
            return jedis.get(key);
        } catch (Exception e) {
            log.debug("get key异常.");
            return null;
        } finally {
            getClose(jedis);
        }
    }
    public static Set<String> getSetMembers(String key) {
        Jedis jedis = null;
        Set<String> members = null;
        try {
            jedis = getJedis();
            members = jedis.smembers(key); // 获取集合成员
        } catch (Exception e) {
            log.debug("获取集合成员发生异常.");
        } finally {
            getClose(jedis);
        }
        return members;
    }


    /**
     * @param @param jedis
     * @return void 返回类型
     * @Description: 关闭连接
     */

    private static void getClose(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static boolean deleteUserIdFromNovelId(int userId, int novelId) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            // 构造 Redis 的 key，例如 "novel:users:{novelId}"
            String key = RedisConstant.preNovelId + novelId;
            // 使用 srem 方法从集合中移除 userId
            Long result = jedis.srem(key, String.valueOf(userId));
            // 如果移除成功（result > 0），返回 true
            return result > 0;
        } catch (Exception e) {
            log.debug("从 Set 中删除 userId 发生异常.", e);
            return false;
        } finally {
            getClose(jedis);
        }
    }



    public static void main(String[] args) {
        try {
            set("111", "111", 1000);
            System.out.println(get("111"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
