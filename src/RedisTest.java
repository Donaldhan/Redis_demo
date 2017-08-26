import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entity.User;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

public class RedisTest {
    private static JedisPoolConfig jConfig = null;
    private static JedisPool pool = null;  
    private static Jedis jedis = null; 
    static {
    	jConfig = new JedisPoolConfig();
    } 
    public static void init() {  
        pool = new JedisPool(jConfig,"192.168.126.128",6379);
        jedis = pool.getResource();  
        jedis.auth("redis");  
        //测试是否连接成功
        System.out.println("Connecting redis......."+jedis.ping());
    }  
	  public static void main(String[] args) throws InterruptedException {
		init();
//		testBasicString();
//		testKeyTTL();
//		testHash();
//		testMap();
//		testSet();
//		testZSet();
//		testList();
//		testListSort();
		//手机验证码
		jedis.set("13697082168", "705634");
		jedis.expire("13697082168", 60);
	    //存储用户信息
		Map<String ,String> uMap = new HashMap<String ,String>();
		uMap.put("name", "donald");
		uMap.put("sex", "man");
		uMap.put("age", "23");
		jedis.hmset(uMap.get("name"), uMap);
		System.out.println(jedis.hgetAll(uMap.get("name")));
		
		//用list存储用户信息
		User user = new User();
		user.setAge(18);
		user.setName("lucy");
		user.setSex("female");
		String userJson = JsonUtil.toJson(user);
		jedis.lpush("usersInfo", userJson);
		String uInfo = jedis.rpop("usersInfo");
		System.out.println("=====userJson:"+uInfo);
		User userx = JsonUtil.fromJson(uInfo, User.class);
		System.out.println("=====userInfo:"+userx.getName()+","+userx.getSex()+","+userx.getAge());
		
		//List，可用于消息队列，生产者生产消息放进List，消费者消费消息，这里就不添加实例了
		
		//游戏排名
		jedis.zadd("plays", 500000, "战神");
    	jedis.zadd("plays", 489000, "射手");
    	jedis.zadd("plays", 300000, "巫师");
    	System.out.println(jedis.zrevrangeByScore("plays", 500000, 300000));
    	//返回有序集合中给定的分数区间的所有成员及分数，分数从高到低
    	Set<Tuple> rankPlay= jedis.zrevrangeByScoreWithScores("plays", 500000, 300000);
    	for(Tuple t : rankPlay){
    		System.out.print("ScoreS："+t.getScore()+","+"playsName:"+t.getElement()); 
    		System.out.print(";");
    	}
		
				
		//切换数据库
//		jedis.select(0);
		//删除当前数据库所有keys，慎用
		//将缓存写到AOP
//		jedis.bgrewriteaof();
		//dump 到rdb文件
//		jedis.bgsave();
//		System.out.println("====key sizes:"+jedis.dbSize());
		jedis.flushDB();
		//删除当前所有数据库所有keys，慎用
//		jedis.flushAll();
	}
    /** 
     * Redis存储初级的字符串 
     * CRUD 
     */  
    public static void testBasicString(){  
        //-----添加数据----------  
        jedis.set("name","donald");//向key-->name中放入了value-->donald  
        System.out.println(jedis.get("name"));//执行结果：donald  
        //-----修改数据-----------  
        //1、在原来基础上修改  
        jedis.append("name","han");   //很直观，将'han' append到已经有的value之后  
        System.out.println(jedis.get("name"));//执行结果:donaldhan  
        //2、直接覆盖原来的数据  
        jedis.set("name","rain");  
        System.out.println(jedis.get("name"));//执行结果：rain  
        //删除key对应的记录  
        jedis.del("name");  
        System.out.println(jedis.get("name"));//执行结果：null  
        /** 
         * mset相当于设置多个k-v值对
         * jedis.set("name","jamel"); 
         * jedis.set("sex","man"); 
         */  
        jedis.mset("name","jamel","sex","man");  
        System.out.println(jedis.mget("name","sex"));  
        //删除多个key
        jedis.del(new String[]{"name","sex"});
        System.out.println(jedis.mget("name","sex"));  
  
    }  
    /**
     * 测试key生存时间，获取指定模式的keys集合，
     * @throws InterruptedException
     */
    public static void testKeyTTL() throws InterruptedException {
    	jedis.del(new String[]{"name","sex"});
    	jedis.set("name","donald");
        jedis.set("sex","man");  
        System.out.println(jedis.get("name"));//执行结果：donald  
    	//keys中传入的可以用通配符  
        System.out.println(jedis.keys("*")); //返回当前库中所有的key
        System.out.println(jedis.keys("*name"));//返回的sname   [sname, name]  
        System.out.println(jedis.ttl("name"));//返回给定key的有效时间，如果是-1则表示永远有效  
//        jedis.expire("name", 10);
        jedis.setex("name", 10, "rain");//通过此方法，可以指定key的存活（有效时间） 时间为秒 ,并重新设值
        Thread.sleep(5000);//睡眠5秒后，剩余时间将为<=5  
        System.out.println(jedis.ttl("name"));   //输出结果为5 
        System.out.println(jedis.get("name"));//执行结果：rain  
        jedis.setex("name", 1, "jamel");        //设为1后，下面再看剩余时间就是1了  
        System.out.println(jedis.ttl("name"));  //输出结果为1 
        System.out.println(jedis.get("name"));//执行结果：jamel
        System.out.println(jedis.exists("name"));//检查key是否存在  
        System.out.println(jedis.rename("name","namex")); //重命名key
        System.out.println(jedis.get("name"));//因为移除，返回为null  
        System.out.println(jedis.get("namex")); //因为将name 重命名为namex 所以可以取得值 jamel  
        jedis.del(new String[]{"name","sex"});
        System.out.println(jedis.mget("name","sex"));
    }  
    /**
     * 哈希操作
     */
    public static void testHash(){
    	//设值hash field value
    	jedis.hset("user", "name", "donlad");
    	jedis.hset("user", "sex", "man");
    	jedis.hset("user", "age", "12");
    	System.out.println(jedis.hget("user", "name"));//获取user的name值
    	System.out.println(jedis.hexists("user", "name"));//判断user的name是否存在
    	System.out.println(jedis.hlen("user"));//判断user的field的数量
    	System.out.println(jedis.hgetAll("user"));//获取user的所有field
    	System.out.println(jedis.hkeys("user"));//获取user的所有keys
    	System.out.println(jedis.hvals("user"));//获取user的所有values
    	jedis.hdel("user","age");//删除user的age属性
    	System.out.println(jedis.hgetAll("user"));
    	jedis.hsetnx("user","age", "30");//如果user的属性存在，则重新设值
    	System.out.println(jedis.hgetAll("user"));
        jedis.del("user");
    }
    /** 
     * jedis操作Map 
     */  
    public static void testMap(){  
        Map<String,String> user = new HashMap<String,String>();  
        user.put("name","donald");  
        user.put("sex","man");  
        jedis.hmset("user",user);  
        //取出user中的name，执行结果:[minxr]-->注意结果是一个泛型的List  
        //第一个参数是存入redis中map对象的key，后面跟的是放入map中的对象的key，后面的key可以跟多个，是可变参数  
        List<String> rsmap = jedis.hmget("user", new String[]{"name","sex"});  
        System.out.println(rsmap);  
        //删除map中的某个键值  
        jedis.hdel("user","sex");  
        System.out.println(jedis.hmget("user", "sex")); //因为删除了，所以返回的是null  
        System.out.println(jedis.hlen("user")); //返回key为user的键中存放的值的个数1  
        System.out.println(jedis.exists("user"));//是否存在key为user的记录 返回true  
        System.out.println(jedis.hgetAll("user"));//获取user的所有k-v对 
        System.out.println(jedis.hkeys("user"));//返回map对象中的所有key 
        System.out.println(jedis.hvals("user"));//返回map对象中的所有value  
        Iterator<String> iter=jedis.hkeys("user").iterator();  
        while (iter.hasNext()){  
            String key = iter.next();  
            System.out.println(key+":"+jedis.hmget("user",key));  
        }  
        jedis.del("user");
  
    }    
    /** 
     * jedis操作Set 
     */  
    public static void testSet(){  
        //添加  
    	jedis.del("name");
        jedis.sadd("name","donald");  
        jedis.sadd("name","rain");  
        jedis.sadd("name","jamel");  
        System.out.println(jedis.sismember("name", "donald"));//判断 donald 是否是name集合的元素  
        System.out.println(jedis.scard("name"));//返回集合的元素个数  
        System.out.println(jedis.srandmember("name")); //从集合中，随机获取一个元素
        System.out.println(jedis.smembers("name"));//获取所有加入的value
        Set<String> nameSet = jedis.smembers("name");
        int i =0;
        int size = jedis.scard("name").intValue();
        for(String name : nameSet){
        	 System.out.print(name);
        	 if(i<(size-1)){
        		 System.out.print(",");
        	 }
        	 i++;
        }
        System.out.println();
        //移除noname  
        jedis.srem("name","donald");  
        System.out.println(jedis.scard("name"));//返回集合的元素个数 
        System.out.println(jedis.smembers("name"));//获取所有加入的value
        //新建集合name1
        jedis.sadd("name1","jamel");  
        jedis.sadd("name1","han");        
        //差集
        System.out.println(jedis.sdiff("name","name1"));//name 与name1的差集
        jedis.sdiffstore("name2", "name","name1");//将name 与name1的差集存储在name2
        System.out.println(jedis.smembers("name2"));
        //交集
        System.out.println(jedis.sinter("name","name1"));//name 与name1的交集
        jedis.sinterstore("name3","name","name1");//将name 与name1的交集存储在name3
        System.out.println(jedis.smembers("name3"));
        //合集
        System.out.println(jedis.sunion("name","name1"));
        jedis.sunionstore("name4","name","name1");
        System.out.println(jedis.smembers("name4"));
        jedis.del("name","name1","name2","name3","name4");
    }  
    /**
     * 测试Zset
     * 
     */
    public static void testZSet(){  
    	Map<Double,String> name = new HashMap<Double,String>();
    	name.put(1.0, "donald");
    	name.put(3.0, "rain");
    	name.put(1.5, "jamel");
    	jedis.zadd("name", name);
    	//获取集合中成员数
    	System.out.println(jedis.zcard("name"));
    	//获取集合中分数在1和2之间的成员数
    	System.out.println(jedis.zcount("name", "1", "2"));
    	//获取donald在集合中的分数（权重）
    	System.out.println(jedis.zscore("name", "donald"));
    	jedis.zincrby("name", 0.2, "donald");
    	System.out.println(jedis.zscore("name", "donald"));
    	//获取donald在集合中的索引
    	System.out.println(jedis.zrank("name", "donald"));
    	System.out.println(jedis.zrange("name", 0, -1));
    	//返回有序集合中给定的分数区间的所有成员，从低到高
    	System.out.println(jedis.zrangeByScore("name", 1, 2));
    	//返回有序集合中给定的分数区间的所有成员及分数，从低到高
    	Set<Tuple> rScores= jedis.zrangeByScoreWithScores("name", 1, 2);
    	for(Tuple t : rScores){
    		System.out.print("Score："+t.getScore()+","+"name:"+t.getElement()); 
    		System.out.print(";");
    	}
    	System.out.println();
    	//通过索引，返回有序集合中给定的索引区间的所有成员，分数从高到低
    	System.out.println(jedis.zrevrange("name", 0, 2));
    	System.out.println(jedis.zrevrangeByScore("name", 3, 1));
    	//返回有序集合中给定的分数区间的所有成员及分数，分数从高到低
    	Set<Tuple> rScoresx= jedis.zrevrangeByScoreWithScores("name", 3, 1);
    	for(Tuple t : rScoresx){
    		System.out.print("Score："+t.getScore()+","+"name:"+t.getElement()); 
    		System.out.print(";");
    	}
    	System.out.println();

    	//移除jamel成员
    	jedis.zrem("name", "jamel");
    	System.out.println(jedis.zrange("name", 0, -1));
    	//新建集合
    	jedis.zadd("name1", 1.0, "donald");
    	jedis.zadd("name1", 4.0, "han");
    	//将name与name1的交集存储到name2中
    	jedis.zinterstore("name2", "name","name1");
    	System.out.println(jedis.zrange("name2", 0, -1));
    	//将name与name1的并集存储到name3中
    	jedis.zunionstore("name3", "name","name1");
    	System.out.println(jedis.zrange("name3", 0, -1));
    	
    	jedis.zadd("name1", 2.0, "jack");
    	jedis.zadd("name1", 3.0, "mark");
    	jedis.zadd("name1", 5.0, "james");
    	
    	System.out.println(jedis.zrange("name1", 0, -1));
    	//根据索引移除成员
    	jedis.zremrangeByRank("name1", 0, 1);
    	System.out.println(jedis.zrange("name1", 0, -1));
    	//根据分数移除成员
        jedis.zremrangeByScore("name1",3 , 4);
    	System.out.println(jedis.zrange("name1", 0, -1));
    	jedis.del("name","name1","name2","name3");
    }
    /** 
     * jedis操作List 
     */  
    public static void testList(){  
        //开始前，先移除所有的内容  
       jedis.del("name");  
       //先向key name中存放三条数据  
       jedis.lpush("name","donald");  
       jedis.lpush("name","jamel");  
       jedis.lpush("name","rain");  
       jedis.lpush("name","mark"); 
       //再取出所有数据jedis.lrange是按范围取出，  
       // 第一个是key，第二个是起始位置，第三个是结束位置，jedis.llen获取长度 -1表示取得所有  
       System.out.println(jedis.lrange("name",0,-1));  
       System.out.println(jedis.lindex("name", 0));
       System.out.println(jedis.llen("name"));
       System.out.println(jedis.lpop("name"));
       System.out.println(jedis.lrange("name",0,-1)); 
       jedis.lset("name", 0, "mark");
       System.out.println(jedis.lrange("name",0,-1)); 
       jedis.rpush("name", "jack");
       System.out.println(jedis.lrange("name",0,-1));
       jedis.rpop("name");
       System.out.println(jedis.lrange("name",0,-1));
       jedis.blpop(5000, "name");
       System.out.println(jedis.lrange("name",0,-1));
       jedis.brpop(5000, "name");
       System.out.println(jedis.lrange("name",0,-1));
       jedis.del("name");  
    }  
    /**
     * 测试List排序
     * @throws InterruptedException
     */
    public static void testListSort() throws InterruptedException {
        //jedis 排序  
        //注意，此处的rpush和lpush是List的操作。是一个双向链表（但从表现来看的）  
        jedis.del("age");//先清除数据，再加入数据进行测试  
        jedis.rpush("age", "1");  
        jedis.lpush("age","6");  
        jedis.lpush("age","3");  
        jedis.lpush("age","9");  
        System.out.println(jedis.lrange("age",0,-1));// [9, 3, 6, 1]  
        System.out.println(jedis.sort("age")); //[1, 3, 6, 9]  //输入排序后结果  
        System.out.println(jedis.lrange("age",0,-1));  
        jedis.del("age");  
    }  
   
}
