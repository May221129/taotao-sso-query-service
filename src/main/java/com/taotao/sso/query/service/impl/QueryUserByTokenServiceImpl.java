package com.taotao.sso.query.service.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.sso.query.api.service.QueryUserByTokenService;
import com.taotao.sso.query.bean.User;

@Service
public class QueryUserByTokenServiceImpl implements QueryUserByTokenService{
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;// 用于操作Redis

	private static final long REDIS_SECONDS = 60 * 30;// token放入Redis后的存活时间，单位：秒

	private static final ObjectMapper MEPPER = new ObjectMapper();// 用于序列化和反序列化

	/**
	 * 根据token查询user对象：
	 * 完成token验证后，cookie中token的生存时间更新是在调用该远程服务的那方完成的。
	 */
	public User queryUserByToken(String token){
		if(StringUtils.isNotEmpty(token)){
			String jsonData = this.stringRedisTemplate.opsForValue().get(RedisKeyConstant.getToken(token));
			if (StringUtils.isNotEmpty(jsonData)) {
				try{
					//将redis中获取到的json数据转user对象：
					User user = MEPPER.readValue(jsonData, User.class);
					//更新redis中的token的生存时间
					this.stringRedisTemplate.expire(RedisKeyConstant.getToken(token), REDIS_SECONDS, TimeUnit.SECONDS);
					//更新redis中user值的生存时间：
					this.stringRedisTemplate.expire(RedisKeyConstant.getUserId(user.getId()), REDIS_SECONDS, TimeUnit.SECONDS);
					return user;
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
