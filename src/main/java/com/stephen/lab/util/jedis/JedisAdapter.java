package com.stephen.lab.util.jedis;

import com.stephen.lab.util.LogRecod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by stephen on 2017/7/15.
 */
@Component
public class JedisAdapter {
    @Autowired
    private JedisPool jedisPool;

    public String set(final String key, final String value) {
        JedisTemplate template=new JedisTemplate();
        return  template.execute(new JedisCallBack<String>() {
            @Override
            public String handle(Jedis jedis) {
                return jedis.set(key,value);
            }
        });
    }
    public String get(final String key) {
        JedisTemplate template=new JedisTemplate();
        return  template.execute(new JedisCallBack<String>() {
            @Override
            public String handle(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }

    class JedisTemplate {
        private Jedis jedis;

        public JedisTemplate() {

        }

        public <T> T execute(JedisCallBack<T> callBack) {
            jedis = null;
            try {
                jedis = jedisPool.getResource();
                return callBack.handle(jedis);
            } catch (Exception e) {
                LogRecod.error(e.getMessage());
            } finally {
                jedis.close();
            }
            return null;
        }
    }

    interface JedisCallBack<T> {
        T handle(Jedis jedis);
    }
}
