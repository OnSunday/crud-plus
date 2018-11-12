package com.jfeat.crud.plus;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Silent-Y on 2017/9/18.
 */
public class DemoTest {

    @Test
    public void testCast(){
        DemoTypeModel m = new DemoTypeModel();
        m.setName("Bob");
        m.setDesc("Good looking boy");

        Class<DemoType> clz = (Class<DemoType>) m.getClass().getSuperclass();

        String jsonString = JSON.toJSONString(m);


        DemoType t = JSON.parseObject(JSON.toJSONString(m),  clz);
        String s = t.toString();
    }

    //@Test
    public void test01(){
        List<DemoType> list = new ArrayList<>();
        list.add(new DemoType(1L,"top父类",null));
        list.add(new DemoType(2L,"top的子类",1L));
        list.add(new DemoType(3L,"top的子类2",1L));
        list.add(new DemoType(4L,"top的子类3",1L));
        list.add(new DemoType(5L,"2的子类1",2L));
        list.add(new DemoType(6L,"2的子类2",2L));
        list.add(new DemoType(7L,"3的子类",3L));
        list.add(new DemoType(8L,"4的子类1",4L));
        list.add(new DemoType(9L,"4的子类1",4L));
        list.add(new DemoType(10L,"4的子类1",4L));
        list.add(new DemoType(11L,"5的子类1",5L));
        list.add(new DemoType(12L,"5的子类2",5L));
        list.add(new DemoType(13L,"5的子类3",5L));
        list.add(new DemoType(14L,"5的子类4",5L));
        list.add(new DemoType(15L,"6的子类1",6L));
        list.add(new DemoType(16L,"6的子类2",6L));
        list.add(new DemoType(17L,"6的子类3",6L));
        list.add(new DemoType(18L,"6的子类4",6L));
        list.add(new DemoType(19L,"7的子类1",7L));
        list.add(new DemoType(20L,"7的子类2",7L));
        list.add(new DemoType(21L,"7的子类3",7L));
        list.add(new DemoType(22L,"7的子类4",7L));

        JSONObject object=GROUP.toJSONObject(list);
        String json =object.toJSONString();
        System.out.printf(json);

    }

    //@Test
    public void test02(){
        List<DemoType> list = new ArrayList<>();
        list.add(new DemoType(1L,"top父类",null));
        list.add(new DemoType(2L,"top的子类",1L));
        list.add(new DemoType(3L,"top的子类2",1L));
        list.add(new DemoType(5L,"2的子类1",2L));


        JSONObject object=GROUP.toJSONObject(list);
        String json =object.toJSONString();
        System.out.printf(json);
    }


    //@Test
    public void test04(){
        List<DemoType> list = new ArrayList<>();
        list.add(new DemoType(1L,"top父类",null));
        list.add(new DemoType(2L,"top的子类",1L));
        list.add(new DemoType(3L,"top的子类2",1L));
        list.add(new DemoType(33L,"top2父类",null));
        list.add(new DemoType(4L,"top的子类3",33L));
        list.add(new DemoType(5L,"2的子类1",2L));
        list.add(new DemoType(6L,"2的子类2",2L));
        list.add(new DemoType(7L,"3的子类",3L));
        list.add(new DemoType(8L,"4的子类1",4L));
        list.add(new DemoType(9L,"4的子类1",4L));
        list.add(new DemoType(10L,"4的子类1",4L));
        list.add(new DemoType(11L,"5的子类1",5L));
        list.add(new DemoType(12L,"5的子类2",5L));
        list.add(new DemoType(13L,"5的子类3",5L));
        list.add(new DemoType(14L,"5的子类4",5L));
        list.add(new DemoType(15L,"6的子类1",6L));
        list.add(new DemoType(16L,"6的子类2",6L));
        list.add(new DemoType(17L,"6的子类3",6L));
        list.add(new DemoType(18L,"6的子类4",6L));
        list.add(new DemoType(19L,"7的子类1",7L));
        list.add(new DemoType(20L,"7的子类2",7L));
        list.add(new DemoType(21L,"7的子类3",7L));
        list.add(new DemoType(22L,"7的子类4",7L));

        JSONObject object=GROUP.toJSONObject(list);
        String json =object.toJSONString();
        System.out.printf(json);

    }
}
