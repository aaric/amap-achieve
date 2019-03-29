package com.cooggo.amap.service;

import java.util.List;

/**
 * 高德地图服务Service接口
 *
 * @author Aaric, created on 2019-03-29T17:24.
 * @since 0.0.1-SNAPSHOT
 */
public interface AmapService {

    /**
     * 查询逆地理编码地址
     *
     * @param point 地理位置
     * @return
     */
    String queryAddress(Point point) throws Exception;

    /**
     * 批量查询逆地理编码地址
     *
     * @param points 地理位置集合
     * @return
     */
    List<String> batchQueryAddress(List<Point> points) throws Exception;

    /**
     * Point
     */
    class Point {

        /**
         * 经度
         */
        private double lng;

        /**
         * 纬度
         */
        private double lat;

        public Point() {
        }

        public Point(double lng, double lat) {
            this.lng = lng;
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }
    }
}
