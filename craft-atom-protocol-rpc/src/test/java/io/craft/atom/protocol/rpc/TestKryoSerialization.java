package io.craft.atom.protocol.rpc;


import io.craft.atom.protocol.rpc.model.RpcBody;
import io.craft.atom.protocol.rpc.model.RpcMethod;
import io.craft.atom.test.CaseCounter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@code KryoSerialization}
 * 
 * @author mindwind
 * @version 1.0, Jul 25, 2014
 */
public class TestKryoSerialization {
	
	
	private KryoSerialization ks = KryoSerialization.getInstance();
	private RpcBody           rb = new RpcBody(); 
	private SerialA           sa = new SerialA();
	private SerialB           sb = new SerialB();
	
	
	@Before
	public void before() {
		sa.setT("transient");
		sa.setB((byte) 1);
		sa.setI(2);
		sa.setL(1000L);
		sa.setF(2.0f);
		sa.setD(123.33);
		sa.setBool(true);
		sa.setS("foo.bar");
		sa.addList("1").addList("2").addList("3");
		sa.addSet("a").addSet("b").addSet("c");
		sa.putMap(1L, "a").putMap(2L, "b").putMap(3L, "c");
		sa.putNested("a", "a1", "a2", "a3");
		sa.putNested("b", "b1", "b2", "b3");
		sa.putNested("c", "c1", "c2", "c3");
		sa.setSenum(SerialEnum.A);
		sa.setSeb(sb);
		sb.setSea(sa);
		sb.setBytes(new byte[] { 1, 2, 3, 4, 5 });
		RpcMethod method = new RpcMethod();
		method.setParameterTypes(SerialA.class, SerialB.class);
		method.setParameters(sa, sb);
		rb.setRpcMethod(method);
	}
	
	@Test 
	public void testBasic() {
		byte[] bytes = ks.serialize(rb);
		RpcBody body = ks.deserialize(bytes);
		Class<?>[] parameterTypes = body.getRpcMethod().getParameterTypes();
		Object[] parameters = body.getRpcMethod().getParameters();
		
		Assert.assertEquals(2, parameterTypes.length);
		Assert.assertEquals(2, parameters.length);
		Assert.assertEquals(SerialA.class, parameterTypes[0]);
		Assert.assertEquals(SerialB.class, parameterTypes[1]);
		
		SerialA aa = (SerialA) parameters[0];
		SerialB ab = (SerialB) parameters[1];
		Assert.assertEquals(sa.getB(), aa.getB());
		Assert.assertEquals(sa.getI(), aa.getI());
		Assert.assertEquals(sa.getL(), aa.getL());
		Assert.assertEquals(sa.getF(), aa.getF(), 0.00001);
		Assert.assertEquals(sa.getD(), aa.getD(), 0.00001);
		Assert.assertEquals(sa.isBool(), aa.isBool());
		Assert.assertEquals(sa.getS(), aa.getS());
		Assert.assertEquals(sa.getDate(), aa.getDate());
		Assert.assertEquals(sa.getList(), aa.getList());
		Assert.assertEquals(sa.getSet(), aa.getSet());
		Assert.assertEquals(sa.getMap(), aa.getMap());
		Assert.assertEquals(sa.getNested(), aa.getNested());
		Assert.assertEquals(sa.getSenum().getCode(), aa.getSenum().getCode());
		Assert.assertEquals(sa.getSenum().getDesc(), aa.getSenum().getDesc());
		Assert.assertArrayEquals(sb.getBytes(), ab.getBytes());
		Assert.assertEquals(aa.getSeb(), ab);
		Assert.assertEquals(ab.getSea(), aa);
		System.out.format("[CRAFT-ATOM-PROTOCOL-RPC] (^_^)  <%s>  Case -> test kryo serialization basic.\n", CaseCounter.incr(21));
	}
	
	@Test
	public void testTransient() {
		byte[] bytes = ks.serialize(rb);
		RpcBody body = ks.deserialize(bytes);
		Object[] parameters = body.getRpcMethod().getParameters();
		SerialA aa = (SerialA) parameters[0];
		
		Assert.assertEquals(null, aa.getT());
		System.out.format("[CRAFT-ATOM-PROTOCOL-RPC] (^_^)  <%s>  Case -> test kryo serialization transient.\n", CaseCounter.incr(1));
	}
	
	
	@Test
	public void testCompatibility() throws Exception {
		// model has more fields; comment SerialA.t1 and generate bytes
		byte[] lessBytes = new byte[] {1, 0, 0, 0, 0, 0, 13, 1, 0, 1, 3, 1, 1, 0, 105, 111, 46, 99, 114, 97, 102, 116, 46, 97, 116, 111, 109, 46, 112, 114, 111, 116, 111, 99, 111, 108, 46, 114, 112, 99, 46, 83, 101, 114, 105, 97, 108, -63, 0, 1, 1, 1, 105, 111, 46, 99, 114, 97, 102, 116, 46, 97, 116, 111, 109, 46, 112, 114, 111, 116, 111, 99, 111, 108, 46, 114, 112, 99, 46, 83, 101, 114, 105, 97, 108, -62, 0, 1, 2, 91, 76, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 79, 98, 106, 101, 99, 116, -69, 1, 3, 1, 0, 1, 14, -126, 98, 98, 111, 111, -20, -126, 100, 100, 97, 116, -27, -126, 102, -126, 105, -126, 108, 108, 105, 115, -12, 109, 97, -16, 110, 101, 115, 116, 101, -28, -126, 115, 115, 101, -30, 115, 101, 110, 117, -19, 115, 101, -12, 1, 1, 0, 1, 1, 0, 8, 64, 94, -43, 30, -72, 81, -21, -123, 0, 23, 1, 3, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 68, 97, 116, -27, 1, -83, -54, -65, -49, -105, 41, 0, 4, 64, 0, 0, 0, 0, 1, 4, 0, 2, -48, 15, 0, 32, 1, 4, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 65, 114, 114, 97, 121, 76, 105, 115, -12, 1, 3, 1, -126, 49, 1, -126, 50, 1, -126, 51, 0, 36, 1, 5, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 77, 97, -16, 1, 3, 1, 2, 1, -126, 97, 1, 4, 1, -126, 98, 1, 6, 1, -126, 99, 0, 55, 1, 5, 1, 3, 16, 1, 4, 1, 3, 3, 1, 98, -79, 3, 1, 98, -78, 3, 1, 98, -77, 17, 1, 4, 1, 3, 3, 1, 99, -79, 3, 1, 99, -78, 3, 1, 99, -77, 15, 1, 4, 1, 3, 3, 1, 97, -79, 3, 1, 97, -78, 3, 1, 97, -77, 0, 8, 1, 102, 111, 111, 46, 98, 97, -14, 0, 26, 1, 1, 1, 2, 98, 121, 116, 101, -13, 115, 101, -31, 7, 1, 6, 1, 2, 3, 4, 5, 0, 3, 1, 0, 8, 0, 0, 2, 1, 1, 0, 24, 1, 6, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 83, 101, -12, 1, 3, 16, 17, 15, 0, 1, 1, 32, 0 };
		try {
			RpcBody body = ks.deserialize(lessBytes);
			Assert.assertNotNull(body);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		// model lack of one field; uncomment SerialA.t1 and SerialA.t2 and generate bytes
		
		byte[] moreBytes = new byte[] { 1, 0, 0, 0, 0, 0, 13, 1, 0, 1, 3, 1, 1, 0, 105, 111, 46, 99, 114, 97, 102, 116, 46, 97, 116, 111, 109, 46, 112, 114, 111, 116, 111, 99, 111, 108, 46, 114, 112, 99, 46, 83, 101, 114, 105, 97, 108, -63, 0, 1, 1, 1, 105, 111, 46, 99, 114, 97, 102, 116, 46, 97, 116, 111, 109, 46, 112, 114, 111, 116, 111, 99, 111, 108, 46, 114, 112, 99, 46, 83, 101, 114, 105, 97, 108, -62, 0, 1, 2, 91, 76, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 79, 98, 106, 101, 99, 116, -69, 1, 3, 1, 0, 1, 16, -126, 98, 98, 111, 111, -20, -126, 100, 100, 97, 116, -27, -126, 102, -126, 105, -126, 108, 108, 105, 115, -12, 109, 97, -16, 110, 101, 115, 116, 101, -28, -126, 115, 115, 101, -30, 115, 101, 110, 117, -19, 115, 101, -12, 116, -79, 116, -78, 1, 1, 0, 1, 1, 0, 8, 64, 94, -43, 30, -72, 81, -21, -123, 0, 23, 1, 3, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 68, 97, 116, -27, 1, -84, -7, -62, -49, -105, 41, 0, 4, 64, 0, 0, 0, 0, 1, 4, 0, 2, -48, 15, 0, 32, 1, 4, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 65, 114, 114, 97, 121, 76, 105, 115, -12, 1, 3, 1, -126, 49, 1, -126, 50, 1, -126, 51, 0, 36, 1, 5, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 77, 97, -16, 1, 3, 1, 2, 1, -126, 97, 1, 4, 1, -126, 98, 1, 6, 1, -126, 99, 0, 55, 1, 5, 1, 3, 16, 1, 4, 1, 3, 3, 1, 98, -79, 3, 1, 98, -78, 3, 1, 98, -77, 17, 1, 4, 1, 3, 3, 1, 99, -79, 3, 1, 99, -78, 3, 1, 99, -77, 15, 1, 4, 1, 3, 3, 1, 97, -79, 3, 1, 97, -78, 3, 1, 97, -77, 0, 8, 1, 102, 111, 111, 46, 98, 97, -14, 0, 26, 1, 1, 1, 2, 98, 121, 116, 101, -13, 115, 101, -31, 7, 1, 6, 1, 2, 3, 4, 5, 0, 3, 1, 0, 8, 0, 0, 2, 1, 1, 0, 24, 1, 6, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 83, 101, -12, 1, 3, 16, 17, 15, 0, 7, 1, 49, 49, 49, 49, 49, -79, 0, 7, 1, 50, 50, 50, 50, 50, -78, 0, 1, 1, 32, 0 };
		Assert.assertTrue(moreBytes.length > lessBytes.length);
		try {
			RpcBody body = ks.deserialize(moreBytes);
			Assert.assertNotNull(body);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		System.out.format("[CRAFT-ATOM-PROTOCOL-RPC] (^_^)  <%s>  Case -> test kryo serialization compatibility.\n", CaseCounter.incr(3));
	}
	
}
