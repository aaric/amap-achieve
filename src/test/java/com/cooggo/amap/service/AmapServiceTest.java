package com.cooggo.amap.service;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * AmapServiceTest
 *
 * @author Aaric, created on 2019-03-29T17:37.
 * @since 0.0.1-SNAPSHOT
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AmapServiceTest {

    @Autowired
    private AmapService amapService;

    @Test
    @Ignore
    public void testQueryAddress() throws Exception {
        //String address = amapService.queryAddress(new Point(0, 0));
        String address = amapService.queryAddress(new AmapService.Point(114.406728, 30.477141));
        System.out.println(address);
        Assert.assertNotNull(address);
    }

    @Test
    @Ignore
    public void testBatchQueryAddress() throws Exception {
        // "116.310003,39.991957|114.406728,30.477141"
        List<AmapService.Point> points = new ArrayList<>();
        points.add(new AmapService.Point(114.403588, 30.475432));
        points.add(new AmapService.Point(116.310003, 39.991957));
        points.add(new AmapService.Point(0.0, 0.0));
        points.add(new AmapService.Point(0.0, 0.0));
        points.add(new AmapService.Point(114.406728, 30.477141));
        List<String> addressList = amapService.batchQueryAddress(points);
        addressList.forEach(object -> System.out.println(object));
        Assert.assertEquals(points.size(), addressList.size());
    }
}
