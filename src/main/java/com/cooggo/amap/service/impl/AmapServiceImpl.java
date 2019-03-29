package com.cooggo.amap.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cooggo.amap.service.AmapService;
import com.cooggo.amap.utils.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 高德地图服务Service接口实现
 *
 * @author Aaric, created on 2019-03-29T17:27.
 * @since 0.0.1-SNAPSHOT
 */
public class AmapServiceImpl implements AmapService {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(AmapServiceImpl.class);

    /**
     * 无效位置返回默认地址
     */
    private static final String DEFAULT_INVALID_ADDRESS = "未知";

    /**
     * 高德地图授权Key
     */
    @Value("${amap.key}")
    private String restApiKey;

    /**
     * 高德地图REST接口地址
     */
    @Value("${amap.host}")
    private String restApiHost;

    /**
     * 查询坐标转换位置
     *
     * @param points 地理位置集合
     * @return
     */
    private String batchQueryAmapPoints(List<Point> points) throws Exception {
        // 检验参数合法性
        if (null == points || 0 == points.size()) {
            throw new IllegalArgumentException("points can't be empty");
        }

        // 构建REST API请求地址
        String restApiUrl = MessageFormat.format("https://{0}/v3/assistant/coordinate/convert", restApiHost);

        // 构建locations参数值
        String locationsString = points.stream()
                .map(object -> object.getLng() + "," + object.getLat())
                .collect(Collectors.joining("|"));

        // 构建高德API逆地理编码请求地址
        Map<String, Object> params = new HashMap<>();
        params.put("key", restApiKey);
        params.put("locations", locationsString);
        params.put("coordsys", "gps");
        params.put("output", "json");
        String result = HttpClientUtil.httpGetRequest(restApiUrl, params);

        // 解析json字符串
        if (StringUtils.isNotBlank(result)) {
            JSONObject root = JSONObject.parseObject(result);
            if ("1".equals(root.getString("status"))) {
                // 返回转换后的坐标信息
                return root.getString("locations").replaceAll(";", "\\|");
            } else {
                // 打印错误日志
                logger.error("batchQueryAmapPoints failure, input: {}, errorInfo: {}", locationsString, root.getString("info"));
            }
        }
        return null;
    }

    @Override
    public String queryAddress(Point point) throws Exception {
        // 检验参数合法性
        if (null == point) {
            throw new IllegalArgumentException("point can't be null");
        }

        // 判断经纬度new Point(0, 0)时，直接返回“未知”
        if (0 == point.getLat() && 0 == point.getLng()) {
            return DEFAULT_INVALID_ADDRESS;
        }

        // 构建REST API请求地址
        String restApiUrl = MessageFormat.format("https://{0}/v3/geocode/regeo", restApiHost);

        // 构建高德API逆地理编码请求地址
        Map<String, Object> params = new HashMap<>();
        params.put("key", restApiKey);
        params.put("location", batchQueryAmapPoints(Arrays.asList(point)));
        params.put("extensions", "base");
        params.put("output", "json");
        String result = HttpClientUtil.httpGetRequest(restApiUrl, params);

        // 解析json字符串
        if (StringUtils.isNotBlank(result)) {
            JSONObject root = JSONObject.parseObject(result);
            JSONObject regeocode = root.getJSONObject("regeocode");
            String address = regeocode.getString("formatted_address");
            return "[]".equals(address) ? DEFAULT_INVALID_ADDRESS : address;
        }
        return null;
    }

    @Override
    public List<String> batchQueryAddress(List<Point> points) throws Exception {
        // 检验参数合法性
        if (null == points || 0 == points.size()) {
            throw new IllegalArgumentException("point can't be null");
        }

        // 构建REST API请求地址
        String restApiUrl = MessageFormat.format("https://{0}/v3/geocode/regeo", restApiHost);

        // 构建location参数值
        List<Point> legalPoints = points.stream()
                .filter(object -> 0.0 != object.getLng() && 0.0 != object.getLat())
                .collect(Collectors.toList());

        // 有效点的数量必须大于0
        if (null != legalPoints && 0 != legalPoints.size()) {
            // 构建location字符串集合
            List<String> legalPointStrings = legalPoints.stream()
                    .map(object -> object.getLng() + "," + object.getLat()).collect(Collectors.toList());
            String legalPointsString = batchQueryAmapPoints(legalPoints);

            // 构建高德API逆地理编码请求地址
            Map<String, Object> params = new HashMap<>();
            params.put("key", restApiKey);
            params.put("location", legalPointsString);
            params.put("batch", "true");
            params.put("extensions", "base");
            params.put("output", "json");
            String result = HttpClientUtil.httpGetRequest(restApiUrl, params);

            // 解析json字符串
            if (StringUtils.isNotBlank(result)) {
                JSONObject root = JSONObject.parseObject(result);
                if ("1".equals(root.getString("status"))) {
                    // 返回地址信息
                    String address;
                    JSONArray regeocodes = root.getJSONArray("regeocodes");
                    if (null != regeocodes && 0 != regeocodes.size()) {
                        // 构建合法逆编码地址MAP集合
                        Map<String, String> legalAddressMap = new HashMap<>();
                        for (int i = 0; i < legalPointStrings.size(); i++) {
                            address = regeocodes.getJSONObject(i).getString("formatted_address");//error deal
                            legalAddressMap.put(legalPointStrings.get(i), address);
                            //System.out.println(legalPointStrings.get(i) + ":" + address);
                        }

                        // 构建返回正确的地址信息集合
                        List<String> returnAddressList = new ArrayList<>();
                        for (Point point : points) {
                            address = legalAddressMap.get(point.getLng() + "," + point.getLat());
                            if (StringUtils.isNotBlank(address)) {
                                returnAddressList.add(address);
                            } else {
                                returnAddressList.add(DEFAULT_INVALID_ADDRESS);
                            }
                        }
                        return returnAddressList;
                    }
                } else {
                    // 打印错误日志
                    String locationsString = legalPointStrings.stream()
                            .collect(Collectors.joining("|"));
                    logger.error("batchQueryAddress failure, input: {}, errorInfo: {}", locationsString, root.getString("info"));
                }
            }
        }

        // 其他情况：返回全部查询位置“未知”
        String[] returnAddresses = new String[points.size()];
        Arrays.fill(returnAddresses, DEFAULT_INVALID_ADDRESS);

        return Arrays.asList(returnAddresses);
    }
}
