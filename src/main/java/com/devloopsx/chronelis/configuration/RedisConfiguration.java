package com.devloopsx.chronelis.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.UnifiedJedis;

import java.time.Duration;

@Configuration
@Slf4j
public class RedisConfiguration {

	@Value("${redis.host}")
	private String redisHost;

	@Value("${redis.port}")
	private int redisPort;

	@Value("${redis.username}")
	private String redisUsername;

	@Value("${redis.password}")
	private String redisPassword;

	@Value("${redis.ssl:true}")
	private boolean redisSsl;

	@Value("${redis.timeout:2000}")
	private int redisTimeout;

	@Value("${redis.jedis.pool.max-active:8}")
	private int maxActive;

	@Value("${redis.jedis.pool.max-idle:8}")
	private int maxIdle;

	@Value("${redis.jedis.pool.min-idle:0}")
	private int minIdle;

	@Value("${redis.jedis.pool.max-wait:-1}")
	private long maxWait;

	@Bean
	public JedisPoolConfig jedisPoolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxActive);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWait(Duration.ofMillis(maxWait));
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		return poolConfig;
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(redisHost);
		redisConfig.setPort(redisPort);
		if (StringUtils.hasText(redisPassword)) {
			redisConfig.setPassword(redisPassword);
		}

		JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration
				.builder();

		jedisClientConfiguration.connectTimeout(Duration.ofMillis(redisTimeout));
		jedisClientConfiguration.readTimeout(Duration.ofMillis(redisTimeout));
		jedisClientConfiguration.usePooling().poolConfig(jedisPoolConfig);

		JedisConnectionFactory factory = new JedisConnectionFactory(redisConfig, jedisClientConfiguration.build());

		factory.afterPropertiesSet();
		log.info("Redis connection factory configured successfully for host: {}", redisHost);
		return factory;
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringSerializer);
		template.setValueSerializer(stringSerializer);
		template.setHashKeySerializer(stringSerializer);
		template.setHashValueSerializer(stringSerializer);

		template.afterPropertiesSet();
		log.info("RedisTemplate configured successfully");
		return template;
	}

	@Bean
	public UnifiedJedis unifiedJedis() {
		DefaultJedisClientConfig.Builder builder = DefaultJedisClientConfig.builder();
		if (StringUtils.hasText(redisUsername)) {
			builder.user(redisUsername);
		}
		if (StringUtils.hasText(redisPassword)) {
			builder.password(redisPassword);
		}
		redis.clients.jedis.JedisClientConfig config = builder.build();

		UnifiedJedis jedis = new UnifiedJedis(new HostAndPort(redisHost, redisPort), config);

		log.info("UnifiedJedis client configured successfully");
		return jedis;
	}
}
